package com.modelguard.service;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.modelguard.configs.EvalEngineConfig;
import com.modelguard.entity.EvalRunEntity;
import com.modelguard.entity.TestCaseResultEntity;
import com.modelguard.model.EmailClassificationResult;
import com.modelguard.model.GoldenDataset;
import com.modelguard.model.GoldenTestCase;
import com.modelguard.model.RunComparisonResult;
import com.modelguard.repo.EvalRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EvaluationEngineService {
    private static final Logger log = LoggerFactory.getLogger(EvaluationEngineService.class);

    private static final String JUDGE_SYSTEM_PROMPT = """
        You are an expert AI Evaluation System auditing the output quality of a customer support email summarizer.
        Your job is to objectively compare the ACTUAL summary produced by the AI model against the EXPECTED ground-truth summary.

        CRITICAL EVALUATION CRITERIA:
        1. Relevance & Accuracy (1 to 5):
           - 5: Perfect summary capturing all key details, core intent, and sentiment.
           - 4: Good summary; misses minor trivial details but retains core meaning.
           - 3: Acceptable summary; misses major context or introduces slight vagueness.
           - 2: Poor summary; inaccurate, misleading, or fails to summarize key issue.
           - 1: Completely incorrect, irrelevant, or unintelligible.

        2. Hallucination Detection:
           - Flag true IF AND ONLY IF the ACTUAL summary contains factual claims, dates, numbers, or promises NOT present in the INPUT EMAIL.

        Provide strict, unbiased, and objective ratings.
        """;

    private static final String JUDGE_USER_PROMPT = """
        INPUT EMAIL:
        {inputEmail}

        EXPECTED SUMMARY (Ground Truth):
        {expectedSummary}

        ACTUAL SUMMARY (Model Output):
        {actualSummary}

        Evaluate the ACTUAL SUMMARY based on the provided instructions.
        """;

    private final EmailClassifierService classifierService;
    private final ChatClient judgeChatClient;
    private final EvalRunRepository evalRunRepository;
    private final EvalEngineConfig engineConfig;

    public EvaluationEngineService(EmailClassifierService classifierService,
                                   ChatClient.Builder chatClientBuilder,
                                   EvalRunRepository evalRunRepository,
                                   EvalEngineConfig engineConfig) {
        this.classifierService = classifierService;
        this.judgeChatClient = chatClientBuilder.defaultSystem(JUDGE_SYSTEM_PROMPT).build();
        this.evalRunRepository = evalRunRepository;
        this.engineConfig = engineConfig;
    }

    public record JudgeEvaluationResult(
            @JsonPropertyDescription("Score from 1 to 5 evaluating accuracy and summary relevance.")
            @JsonProperty(required = true) int relevanceScore,

            @JsonPropertyDescription("Boolean flag indicating whether output contains unverified claims.")
            @JsonProperty(required = true) boolean containsHallucination,

            @JsonPropertyDescription("Concise justification for assigned score and hallucination flag.")
            @JsonProperty(required = true) String reasoning
    ) {}

    @Transactional
    public EvalRunEntity executeEvaluation(GoldenDataset goldenDataset) {
        String promptVersion = classifierService.getActivePromptVersion();
        List<GoldenTestCase> testCases = goldenDataset.testCases();
        log.info("Executing evaluation runner for Prompt Version '{}' across {} test cases...", promptVersion, testCases.size());

        List<TestCaseResultEntity> results;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<TestCaseResultEntity>> futures = testCases.stream()
                    .map(tc -> executor.submit(() -> evaluateSingleTestCase(tc, promptVersion)))
                    .toList();

            results = futures.stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            log.error("Virtual thread execution failed for test case", e);
                            throw new RuntimeException("Evaluation failure", e);
                        }
                    })
                    .toList();
        }

        // Build and populate parent run entity
        EvalRunEntity run = new EvalRunEntity();
        run.setPromptVersion(promptVersion);
        run.setModelName("gpt-4o-mini");
        run.setCreatedAt(OffsetDateTime.now());
        run.setTotalCases(results.size());

        int passedCount = (int) results.stream().filter(TestCaseResultEntity::isPassed).count();
        run.setPassedCases(passedCount);
        run.setFailedCases(results.size() - passedCount);

        double passRate = results.isEmpty() ? 0.0 : ((double) passedCount / results.size()) * 100.0;
        run.setPassRatePercentage(passRate);

        double avgLatency = results.stream().mapToLong(TestCaseResultEntity::getLatencyMs).average().orElse(0.0);
        run.setAvgLatencyMs(avgLatency);

        results.forEach(run::addTestResult);

        // Fetch baseline run for diff calculation
        Optional<EvalRunEntity> baselineOpt = evalRunRepository.findFirstByOrderByCreatedAtDesc();
        RunComparisonResult comparison = calculateDiffAndThresholds(run, baselineOpt.orElse(null));

        run.setPassRateDelta(comparison.passRateDelta());
        run.setRegressionCount(comparison.totalRegressions());
        run.setStatus(comparison.runStatus());

        return evalRunRepository.save(run);
    }

    private TestCaseResultEntity evaluateSingleTestCase(GoldenTestCase tc, String promptVersion) {
        Instant start = Instant.now();

        // 1. Target LLM Execution
        EmailClassificationResult targetOutput = classifierService.classify(tc.inputText());
        long latencyMs = Duration.between(start, Instant.now()).toMillis();

        // 2. Binary Category Match Check
        boolean categoryMatched = tc.expectedCategory().equalsIgnoreCase(targetOutput.category().name());

        // 3. LLM-as-a-Judge Scoring
        JudgeEvaluationResult judgeResult = invokeLlmJudge(tc.inputText(), tc.expectedSummary(), targetOutput.summary());

        // 4. Multi-dimensional Pass Rule
        boolean passed = categoryMatched && judgeResult.relevanceScore() >= 4 && !judgeResult.containsHallucination();

        TestCaseResultEntity result = new TestCaseResultEntity();
        result.setTestCaseId(tc.id());
        result.setCategoryMatched(categoryMatched);
        result.setRelevanceScore(judgeResult.relevanceScore());
        result.setContainsHallucination(judgeResult.containsHallucination());
        result.setJudgeReasoning(judgeResult.reasoning());
        result.setLatencyMs(latencyMs);
        result.setPassed(passed);

        // Dummy token estimates (Spring AI ChatResponse metrics can be mapped here)
        result.setPromptTokens(150);
        result.setCompletionTokens(45);

        return result;
    }

    private JudgeEvaluationResult invokeLlmJudge(String inputEmail, String expectedSummary, String actualSummary) {
        PromptTemplate template = new PromptTemplate(JUDGE_USER_PROMPT);
        var message = template.createMessage(Map.of(
                "inputEmail", inputEmail,
                "expectedSummary", expectedSummary,
                "actualSummary", actualSummary
        ));

        try {
            return judgeChatClient.prompt()
                    .messages(message)
                    .call()
                    .entity(JudgeEvaluationResult.class);
        } catch (Exception e) {
            log.error("LLM Judge evaluation call failed: {}", e.getMessage());
            return new JudgeEvaluationResult(1, true, "EVALUATION_ERROR: " + e.getMessage());
        }
    }

    public RunComparisonResult calculateDiffAndThresholds(EvalRunEntity currentRun, EvalRunEntity baselineRun) {
        if (baselineRun == null) {
            return new RunComparisonResult(
                    currentRun.getPassRatePercentage(),
                    0.0,
                    0.0,
                    0,
                    0,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    EvalRunEntity.RunStatus.PASSED
            );
        }

        double delta = currentRun.getPassRatePercentage() - baselineRun.getPassRatePercentage();

        Map<String, TestCaseResultEntity> baselineMap = baselineRun.getTestResults().stream()
                .collect(Collectors.toMap(TestCaseResultEntity::getTestCaseId, Function.identity()));

        List<String> regressions = new ArrayList<>();
        List<String> improvements = new ArrayList<>();

        for (TestCaseResultEntity currentTc : currentRun.getTestResults()) {
            TestCaseResultEntity baselineTc = baselineMap.get(currentTc.getTestCaseId());
            if (baselineTc != null) {
                if (baselineTc.isPassed() && !currentTc.isPassed()) {
                    currentTc.setRegression(true);
                    regressions.add(currentTc.getTestCaseId());
                } else if (!baselineTc.isPassed() && currentTc.isPassed()) {
                    improvements.add(currentTc.getTestCaseId());
                }
            }
        }

        // Statistical Significance Threshold Assignment
        double dropPercentage = Math.abs(Math.min(0.0, delta));
        EvalRunEntity.RunStatus status;

        if (dropPercentage >= engineConfig.getCriticalThresholdDeltaPercent()) {
            status = EvalRunEntity.RunStatus.CRITICAL;
        } else if (dropPercentage >= engineConfig.getWarningThresholdDeltaPercent()) {
            status = EvalRunEntity.RunStatus.WARNING;
        } else {
            status = EvalRunEntity.RunStatus.PASSED;
        }

        return new RunComparisonResult(
                currentRun.getPassRatePercentage(),
                baselineRun.getPassRatePercentage(),
                delta,
                regressions.size(),
                improvements.size(),
                regressions,
                improvements,
                status
        );
    }
}

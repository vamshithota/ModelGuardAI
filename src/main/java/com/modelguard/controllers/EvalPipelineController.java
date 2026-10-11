package com.modelguard.controllers;

import com.modelguard.entity.EvalRunEntity;
import com.modelguard.model.GoldenDataset;
import com.modelguard.service.DriftDetectionService;
import com.modelguard.service.EvaluationEngineService;
import com.modelguard.service.GoldenDatasetLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eval")
public class EvalPipelineController {

    private final GoldenDatasetLoader datasetLoader;
    private final EvaluationEngineService evaluationEngineService;
    private final DriftDetectionService driftService;

    public EvalPipelineController(GoldenDatasetLoader datasetLoader,
                                  EvaluationEngineService evaluationEngineService,
                                  DriftDetectionService driftService) {
        this.datasetLoader = datasetLoader;
        this.evaluationEngineService = evaluationEngineService;
        this.driftService = driftService;
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> triggerEvalRun(
            @RequestParam(defaultValue = "data/golden-dataset-v1.0.json") String datasetPath) {


        GoldenDataset dataset = datasetLoader.loadDataset(datasetPath);
        EvalRunEntity runResult = evaluationEngineService.executeEvaluation(dataset);

        // Calculate rolling drift
        var drift = driftService.analyzeRollingDrift(runResult, 7, 5.0);

        // Return combined payload with testResults array and drift metrics
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", runResult.getId());
        response.put("promptVersion", runResult.getPromptVersion());
        response.put("modelName", runResult.getModelName());
        response.put("createdAt", runResult.getCreatedAt());
        response.put("systemPrompt", runResult.getSystemPrompt());
        response.put("userPromptTemplate", runResult.getUserPromptTemplate());
        response.put("promptHash", runResult.getPromptHash());
        response.put("totalCases", runResult.getTotalCases());
        response.put("passedCases", runResult.getPassedCases());
        response.put("failedCases", runResult.getFailedCases());
        response.put("passRatePercentage", runResult.getPassRatePercentage());
        response.put("passRateDelta", runResult.getPassRateDelta());
        response.put("regressionCount", runResult.getRegressionCount());
        response.put("avgLatencyMs", runResult.getAvgLatencyMs());
        response.put("status", runResult.getStatus());
        response.put("driftAnalysis", drift);
        response.put("testResults", runResult.getTestResults()); // <--- Exposes individual test outcomes

        return ResponseEntity.ok(response);
    }
}

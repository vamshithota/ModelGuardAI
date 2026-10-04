package com.modelguard.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "test_case_results")
public class TestCaseResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eval_run_id")
    @JsonIgnoreProperties("testResults")
    private EvalRunEntity evalRun;

    @Column(nullable = false)
    private String testCaseId;

    private boolean categoryMatched;
    private int relevanceScore;
    private boolean containsHallucination;

    @Column(length = 2000)
    private String judgeReasoning;

    private long latencyMs;
    private long promptTokens;
    private long completionTokens;
    private boolean passed;
    private boolean isRegression;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EvalRunEntity getEvalRun() { return evalRun; }
    public void setEvalRun(EvalRunEntity evalRun) { this.evalRun = evalRun; }
    public String getTestCaseId() { return testCaseId; }
    public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }
    public boolean isCategoryMatched() { return categoryMatched; }
    public void setCategoryMatched(boolean categoryMatched) { this.categoryMatched = categoryMatched; }
    public int getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(int relevanceScore) { this.relevanceScore = relevanceScore; }
    public boolean isContainsHallucination() { return containsHallucination; }
    public void setContainsHallucination(boolean containsHallucination) { this.containsHallucination = containsHallucination; }
    public String getJudgeReasoning() { return judgeReasoning; }
    public void setJudgeReasoning(String judgeReasoning) { this.judgeReasoning = judgeReasoning; }
    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }
    public long getPromptTokens() { return promptTokens; }
    public void setPromptTokens(long promptTokens) { this.promptTokens = promptTokens; }
    public long getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(long completionTokens) { this.completionTokens = completionTokens; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public boolean isRegression() { return isRegression; }
    public void setRegression(boolean regression) { isRegression = regression; }
}

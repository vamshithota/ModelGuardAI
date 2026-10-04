package com.modelguard.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eval_runs")
public class EvalRunEntity {

    public enum RunStatus { PASSED, WARNING, CRITICAL }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String promptVersion;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private int totalCases;
    private int passedCases;
    private int failedCases;
    private double passRatePercentage;
    private double passRateDelta;
    private int regressionCount;
    private double avgLatencyMs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunStatus status;
    @JsonBackReference
    @OneToMany(mappedBy = "evalRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TestCaseResultEntity> testResults = new ArrayList<>();

    public void addTestResult(TestCaseResultEntity result) {
        testResults.add(result);
        result.setEvalRun(this);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public int getTotalCases() { return totalCases; }
    public void setTotalCases(int totalCases) { this.totalCases = totalCases; }
    public int getPassedCases() { return passedCases; }
    public void setPassedCases(int passedCases) { this.passedCases = passedCases; }
    public int getFailedCases() { return failedCases; }
    public void setFailedCases(int failedCases) { this.failedCases = failedCases; }
    public double getPassRatePercentage() { return passRatePercentage; }
    public void setPassRatePercentage(double passRatePercentage) { this.passRatePercentage = passRatePercentage; }
    public double getPassRateDelta() { return passRateDelta; }
    public void setPassRateDelta(double passRateDelta) { this.passRateDelta = passRateDelta; }
    public int getRegressionCount() { return regressionCount; }
    public void setRegressionCount(int regressionCount) { this.regressionCount = regressionCount; }
    public double getAvgLatencyMs() { return avgLatencyMs; }
    public void setAvgLatencyMs(double avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }
    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus status) { this.status = status; }
    public List<TestCaseResultEntity> getTestResults() { return testResults; }
    public void setTestResults(List<TestCaseResultEntity> testResults) { this.testResults = testResults; }
}
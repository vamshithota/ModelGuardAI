package com.modelguard.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "eval.engine")
public class EvalEngineConfig {

    private double warningThresholdDeltaPercent = 3.0;
    private double criticalThresholdDeltaPercent = 8.0;

    public double getWarningThresholdDeltaPercent() { return warningThresholdDeltaPercent; }
    public void setWarningThresholdDeltaPercent(double warningThresholdDeltaPercent) { this.warningThresholdDeltaPercent = warningThresholdDeltaPercent; }

    public double getCriticalThresholdDeltaPercent() { return criticalThresholdDeltaPercent; }
    public void setCriticalThresholdDeltaPercent(double criticalThresholdDeltaPercent) { this.criticalThresholdDeltaPercent = criticalThresholdDeltaPercent; }

}

package com.modelguard.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "eval.engine")
public class EvalEngineConfig {
    private double warningThresholdDeltaPercent = 3.0;
    private String slackWebhookUrl;

    public String getSlackWebhookUrl() {
        return slackWebhookUrl;
    }
    public void setSlackWebhookUrl(String slackWebhookUrl) {
        this.slackWebhookUrl = slackWebhookUrl;
    }
    public double getWarningThresholdDeltaPercent() {
        return warningThresholdDeltaPercent;
    }

    public void setWarningThresholdDeltaPercent(double warningThresholdDeltaPercent) {
        this.warningThresholdDeltaPercent = warningThresholdDeltaPercent;
    }

    public double getCriticalThresholdDeltaPercent() {
        return criticalThresholdDeltaPercent;
    }

    public void setCriticalThresholdDeltaPercent(double criticalThresholdDeltaPercent) {
        this.criticalThresholdDeltaPercent = criticalThresholdDeltaPercent;
    }

    /**
     * Percentage drop in pass rate that triggers a critical alert or Slack webhook.
     */
    private double criticalThresholdDeltaPercent = 8.0;

    private Thresholds thresholds = new Thresholds();
    private Slack slack = new Slack();
    private Reports reports = new Reports();

    public static class Thresholds {
        private double warningDelta = -5.0;
        private double criticalDelta = -10.0;

        // getters and setters
        public double getWarningDelta() { return warningDelta; }
        public void setWarningDelta(double warningDelta) { this.warningDelta = warningDelta; }
        public double getCriticalDelta() { return criticalDelta; }
        public void setCriticalDelta(double criticalDelta) { this.criticalDelta = criticalDelta; }
    }

    public static class Slack {
        private boolean enabled;
        private String webhookUrl;

        // getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    }

    public static class Reports {
        private String outputDir;
        private boolean saveH2;

        // getters and setters
        public String getOutputDir() { return outputDir; }
        public void setOutputDir(String outputDir) { this.outputDir = outputDir; }
        public boolean isSaveH2() { return saveH2; }
        public void setSaveH2(boolean saveH2) { this.saveH2 = saveH2; }
    }

    // Top-level getters and setters
    public Thresholds getThresholds() { return thresholds; }
    public void setThresholds(Thresholds thresholds) { this.thresholds = thresholds; }
    public Slack getSlack() { return slack; }
    public void setSlack(Slack slack) { this.slack = slack; }
    public Reports getReports() { return reports; }
    public void setReports(Reports reports) { this.reports = reports; }
}
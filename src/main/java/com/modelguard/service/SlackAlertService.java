package com.modelguard.service;
import com.modelguard.configs.EvalEngineConfig;
import com.modelguard.entity.EvalRunEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class SlackAlertService {

    private static final Logger log = LoggerFactory.getLogger(SlackAlertService.class);
    private final RestClient restClient;
    private final EvalEngineConfig engineConfig;

    public SlackAlertService(EvalEngineConfig engineConfig) {
        this.restClient = RestClient.builder().build();
        this.engineConfig = engineConfig;
    }
    public void sendEvalRunAlert(EvalRunEntity run, String reportUrl) {
        String webhookUrl = engineConfig.getSlackWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.info("Slack webhook URL not configured. Skipping alert dispatch.");
            return;
        }

        String statusEmoji = switch (run.getStatus()) {
            case PASSED -> "✅";
            case WARNING -> "⚠️";
            case CRITICAL -> "🚨";
        };

        Map<String, Object> payload = Map.of(
                "blocks", List.of(
                        Map.of(
                                "type", "header",
                                "text", Map.of("type", "plain_text", "text", statusEmoji + " ModelGuard Regression Alert: " + run.getStatus(), "emoji", true)
                        ),
                        Map.of(
                                "type", "section",
                                "fields", List.of(
                                        Map.of("type", "mrkdwn", "text", "*Prompt Version:*\n`" + run.getPromptVersion() + "`"),
                                        Map.of("type", "mrkdwn", "text", "*Pass Rate:*\n" + String.format("%.1f%%", run.getPassRatePercentage()) + " (Δ " + String.format("%+.1f%%", run.getPassRateDelta()) + ")"),
                                        Map.of("type", "mrkdwn", "text", "*Total / Passed / Failed:*\n" + run.getTotalCases() + " / " + run.getPassedCases() + " / " + run.getFailedCases()),
                                        Map.of("type", "mrkdwn", "text", "*Regressions Detected:*\n" + run.getRegressionCount())
                                )
                        ),
                        Map.of(
                                "type", "section",
                                "text", Map.of("type", "mrkdwn", "text", "📊 <" + reportUrl + "|View Full Interactive HTML Diff Report>")
                        )
                )
        );
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully dispatched Slack alert for run ID: {}", run.getId());
        } catch (Exception e) {
            log.error("Failed to post Slack alert webhook: {}", e.getMessage(), e);
        }
    }
}

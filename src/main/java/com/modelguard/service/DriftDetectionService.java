package com.modelguard.service;
import com.modelguard.entity.EvalRunEntity;
import com.modelguard.repo.EvalRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DriftDetectionService {

    private static final Logger log = LoggerFactory.getLogger(DriftDetectionService.class);
    private final EvalRunRepository evalRunRepository;

    public DriftDetectionService(EvalRunRepository evalRunRepository) {
        this.evalRunRepository = evalRunRepository;
    }

    public record DriftAnalysis(
            double rollingAveragePassRate,
            double currentPassRate,
            boolean driftDetected,
            String message
    ) {}

    public DriftAnalysis analyzeRollingDrift(EvalRunEntity currentRun, int windowSize, double thresholdDropPercent) {
        List<EvalRunEntity> recentRuns = evalRunRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, windowSize));

        if (recentRuns.size() < windowSize) {
            return new DriftAnalysis(
                    currentRun.getPassRatePercentage(),
                    currentRun.getPassRatePercentage(),
                    false,
                    "Insufficient run history for drift analysis (requires " + windowSize + " runs)."
            );
        }
        double rollingAvg = recentRuns.stream()
                .mapToDouble(EvalRunEntity::getPassRatePercentage)
                .average()
                .orElse(currentRun.getPassRatePercentage());

        double dropFromAvg = rollingAvg - currentRun.getPassRatePercentage();
        boolean isDrifting = dropFromAvg >= thresholdDropPercent;

        String msg = isDrifting
                ? String.format("SLOW DRIFT DETECTED: Current pass rate (%.1f%%) dropped %.1f%% below the %d-run moving average (%.1f%%).",
                currentRun.getPassRatePercentage(), dropFromAvg, windowSize, rollingAvg)
                : String.format("No drift detected. Moving average: %.1f%%.", rollingAvg);

        if (isDrifting) {
            log.warn(msg);
        }

        return new DriftAnalysis(rollingAvg, currentRun.getPassRatePercentage(), isDrifting, msg);
    }

}

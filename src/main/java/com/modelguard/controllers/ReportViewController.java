package com.modelguard.controllers;
import com.modelguard.entity.EvalRunEntity;
import com.modelguard.repo.EvalRunRepository;
import com.modelguard.service.DriftDetectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
public class ReportViewController {

    private final EvalRunRepository evalRunRepository;
    private final DriftDetectionService driftService;

    public ReportViewController(EvalRunRepository evalRunRepository, DriftDetectionService driftService) {
        this.evalRunRepository = evalRunRepository;
        this.driftService = driftService;
    }

    @GetMapping("/run/{id:[a-f0-9\\-]{36}}")
    public String viewReport(@PathVariable("id") String id, Model model){ // Guard against template resolution sub-requests capturing 'diff-report' as the ID
        EvalRunEntity run = evalRunRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid evaluation run ID: " + id));

        DriftDetectionService.DriftAnalysis drift = driftService.analyzeRollingDrift(run, 7, 5.0);

        model.addAttribute("run", run);
        model.addAttribute("drift", drift);

        return "diff-report";
    }
}

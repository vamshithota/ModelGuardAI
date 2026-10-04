package com.modelguard.controllers;

import com.modelguard.entity.EvalRunEntity;
import com.modelguard.model.GoldenDataset;
import com.modelguard.service.EvaluationEngineService;
import com.modelguard.service.GoldenDatasetLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eval")
public class EvalPipelineController {

    private final GoldenDatasetLoader datasetLoader;
    private final EvaluationEngineService evaluationEngineService;

    public EvalPipelineController(GoldenDatasetLoader datasetLoader,
                                  EvaluationEngineService evaluationEngineService) {
        this.datasetLoader = datasetLoader;
        this.evaluationEngineService = evaluationEngineService;
    }

    @PostMapping("/run")
    public ResponseEntity<EvalRunEntity> triggerEvalRun(
            @RequestParam(defaultValue = "data/golden-dataset-v1.0.json") String datasetPath) {

        GoldenDataset dataset = datasetLoader.loadDataset(datasetPath);
        EvalRunEntity runResult = evaluationEngineService.executeEvaluation(dataset);

        return ResponseEntity.ok(runResult);
    }
}

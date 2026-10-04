package com.modelguard.model;

import com.modelguard.entity.EvalRunEntity;

import java.util.List;

public record RunComparisonResult(
        double currentPassRate,
        double baselinePassRate,
        double passRateDelta,
        int totalRegressions,
        int totalImprovements,
        List<String> regressedTestCaseIds,
        List<String> improvedTestCaseIds,
        EvalRunEntity.RunStatus runStatus
) {}

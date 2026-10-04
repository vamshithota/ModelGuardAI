package com.modelguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoldenTestCase(
                             @JsonProperty(value = "id", required = true)
                             String id,

                             @JsonProperty(value = "inputText", required = true)
                             String inputText,

                             @JsonProperty(value = "expectedCategory", required = true)
                             String expectedCategory,

                             @JsonProperty(value = "expectedSummary", required = true)
                             String expectedSummary,

                             @JsonProperty(value = "expectedDifficulty", required = true)
                             Difficulty expectedDifficulty,

                             @JsonProperty("edgeCaseCategory")
                             String edgeCaseCategory,

                             @JsonProperty("notes")
                             String notes
) {
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD,
        EDGE_CASE
    }
}
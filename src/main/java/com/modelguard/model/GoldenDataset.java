package com.modelguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GoldenDataset(
                            @JsonProperty(value = "datasetVersion", required = true)
                            String datasetVersion,

                            @JsonProperty("description")
                            String description,

                            @JsonProperty("lastUpdated")
                            String lastUpdated,

                            @JsonProperty(value = "testCases", required = true)
                            List<GoldenTestCase> testCases
) {}
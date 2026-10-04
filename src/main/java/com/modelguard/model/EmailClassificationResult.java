package com.modelguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.modelguard.enums.EmailCategory;

public record EmailClassificationResult(
        @JsonPropertyDescription("The primary category that best describes the customer's email intent.")
        @JsonProperty(required = true)
        EmailCategory category,

        @JsonPropertyDescription("A 1-2 sentence executive summary summarizing the core issue or request.")
        @JsonProperty(required = true)
        String summary
) {}
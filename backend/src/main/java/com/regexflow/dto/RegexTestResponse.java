package com.regexflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegexTestResponse {
    private boolean matched;
    private Map<String, String> extractedFields;
    private String errorMessage;
    private long executionTimeMs;
}

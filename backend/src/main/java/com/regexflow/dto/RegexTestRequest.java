package com.regexflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegexTestRequest {
    
    @NotBlank(message = "Regex pattern is required")
    private String regexPattern;
    
    @NotBlank(message = "Sample SMS is required")
    private String sampleSms;
}

package com.regexflow.dto;

import com.regexflow.enums.SmsType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegexTemplateRequest {
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;
    
    @NotBlank(message = "Regex pattern is required")
    @Size(max = 1000, message = "Regex pattern must not exceed 1000 characters")
    private String regexPattern;
    
    @NotNull(message = "SMS type is required")
    private SmsType smsType;
    
    private String sampleSms;
    
    private String description;
}

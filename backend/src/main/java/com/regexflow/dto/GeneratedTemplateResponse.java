package com.regexflow.dto;

import com.regexflow.enums.SmsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedTemplateResponse {
    private String regexPattern;
    private String bankName;
    private SmsType smsType;
    private String description;
    private String sampleSms;
    private boolean success;
    private String errorMessage;
}

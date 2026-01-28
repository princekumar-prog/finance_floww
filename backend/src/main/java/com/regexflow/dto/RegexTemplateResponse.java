package com.regexflow.dto;

import com.regexflow.enums.SmsType;
import com.regexflow.enums.TemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegexTemplateResponse {
    private Long id;
    private String bankName;
    private String regexPattern;
    private SmsType smsType;
    private TemplateStatus status;
    private String sampleSms;
    private String description;
    private String createdByUsername;
    private String approvedByUsername;
    private LocalDateTime approvedAt;
    private LocalDateTime deprecatedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

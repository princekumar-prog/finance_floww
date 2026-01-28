package com.regexflow.dto;

import com.regexflow.enums.TemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCheckResponse {
    private boolean exists;
    private TemplateStatus status;
    private String message;
}

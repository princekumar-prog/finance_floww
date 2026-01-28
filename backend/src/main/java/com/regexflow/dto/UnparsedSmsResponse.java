package com.regexflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnparsedSmsResponse {
    private Long id;
    private String rawSmsText;
    private String senderHeader;
    private String errorMessage;
    private String uploadedByUsername;
    private LocalDateTime createdAt;
}

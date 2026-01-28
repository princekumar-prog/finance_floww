package com.regexflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsParseRequest {
    
    @NotBlank(message = "SMS text is required")
    @Size(max = 500, message = "SMS text must not exceed 500 characters")
    private String smsText;
    
    private String senderHeader;
}

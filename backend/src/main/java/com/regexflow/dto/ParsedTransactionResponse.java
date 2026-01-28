package com.regexflow.dto;

import com.regexflow.enums.ParseStatus;
import com.regexflow.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedTransactionResponse {
    private Long id;
    private ParseStatus parseStatus;
    private String bankName;
    private BigDecimal amount;
    private BigDecimal balance;
    private TransactionType transactionType;
    private String accountId;
    private String merchantOrPayee;
    private String mode;
    private LocalDate transactionDate;
    private String referenceNumber;
    private Map<String, String> extractedData;
    private String rawSmsText;
    private String templateUsed;
    private Long templateId;
    private String errorMessage;
    private LocalDateTime createdAt;
}

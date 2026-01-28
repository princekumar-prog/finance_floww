package com.regexflow.parser;

import com.regexflow.entity.RegexTemplate;
import com.regexflow.entity.RawSmsLog;
import com.regexflow.enums.ParseStatus;
import com.regexflow.enums.TransactionType;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsParser {
    
    private final RegexParser regexParser;
    
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
        DateTimeFormatter.ofPattern("dd MMM yyyy"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    );
    
    @Data
    @Builder
    public static class ParseResult {
        private ParseStatus status;
        private Map<String, String> extractedFields;
        private BigDecimal amount;
        private BigDecimal balance;
        private String bankName;
        private String accountId;
        private TransactionType transactionType;
        private String merchantOrPayee;
        private String mode;
        private LocalDate transactionDate;
        private String referenceNumber;
        private String errorMessage;
        private RegexTemplate matchedTemplate;
    }
    
    /**
     * Parses SMS text using the provided template
     */
    public ParseResult parseSms(String smsText, RegexTemplate template) {
        try {
            Map<String, String> fields = regexParser.testPattern(
                template.getRegexPattern(), 
                smsText
            );
            
            if (fields.isEmpty()) {
                return ParseResult.builder()
                    .status(ParseStatus.NO_MATCH)
                    .errorMessage("SMS did not match the template pattern")
                    .build();
            }
            
            return buildParseResult(fields, template);
            
        } catch (Exception e) {
            log.error("Error parsing SMS", e);
            return ParseResult.builder()
                .status(ParseStatus.ERROR)
                .errorMessage("Error parsing SMS: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Finds the best matching template and parses the SMS
     */
    public ParseResult parseSmsWithBestMatch(String smsText, List<RegexTemplate> availableTemplates) {
        if (availableTemplates.isEmpty()) {
            return ParseResult.builder()
                .status(ParseStatus.NO_MATCH)
                .errorMessage("No active templates available")
                .build();
        }
        
        // Find best matching template
        RegexTemplate bestMatch = null;
        double bestScore = 0.0;
        
        for (RegexTemplate template : availableTemplates) {
            try {
                if (regexParser.matches(template.getRegexPattern(), smsText)) {
                    double score = regexParser.calculateMatchScore(template.getRegexPattern(), smsText);
                    if (score > bestScore) {
                        bestScore = score;
                        bestMatch = template;
                    }
                }
            } catch (Exception e) {
                log.warn("Template {} failed to match: {}", template.getId(), e.getMessage());
            }
        }
        
        if (bestMatch == null) {
            return ParseResult.builder()
                .status(ParseStatus.NO_MATCH)
                .errorMessage("No matching template found for this SMS")
                .build();
        }
        
        return parseSms(smsText, bestMatch);
    }
    
    /**
     * Builds a complete parse result from extracted fields
     */
    private ParseResult buildParseResult(Map<String, String> fields, RegexTemplate template) {
        ParseResult.ParseResultBuilder builder = ParseResult.builder()
            .status(ParseStatus.SUCCESS)
            .extractedFields(fields)
            .matchedTemplate(template);
        
        // Extract and convert amount
        BigDecimal amount = extractAmount(fields);
        builder.amount(amount);
        
        // Extract and convert balance
        BigDecimal balance = extractBalance(fields);
        builder.balance(balance);
        
        // Extract bank name
        String bankName = extractField(fields, "bank", "bankName", "bankname");
        builder.bankName(bankName != null ? bankName : template.getBankName());
        
        // Extract account ID
        String accountId = extractField(fields, "account", "accountId", "accountNumber", "accNo");
        builder.accountId(accountId);
        
        // Determine transaction type
        TransactionType transactionType = determineTransactionType(fields, template);
        builder.transactionType(transactionType);
        
        // Extract merchant/payee
        String merchant = extractField(fields, "merchant", "payee", "merchantName", "beneficiary", "to");
        builder.merchantOrPayee(merchant);
        
        // Extract mode
        String mode = extractField(fields, "mode", "transactionMode", "channel");
        builder.mode(mode);
        
        // Extract transaction date
        LocalDate date = extractDate(fields);
        builder.transactionDate(date);
        
        // Extract reference number
        String refNo = extractField(fields, "ref", "refNo", "referenceNumber", "refNum", "txnRef");
        builder.referenceNumber(refNo);
        
        // Determine if parse was complete or partial
        if (amount == null && balance == null) {
            builder.status(ParseStatus.PARTIAL);
            builder.errorMessage("Unable to extract financial amounts");
        }
        
        return builder.build();
    }
    
    private BigDecimal extractAmount(Map<String, String> fields) {
        String amountStr = extractField(fields, "amount", "amt", "value", "rupees", "rs");
        return parseAmount(amountStr);
    }
    
    private BigDecimal extractBalance(Map<String, String> fields) {
        String balanceStr = extractField(fields, "balance", "bal", "availBal", "availableBalance");
        return parseAmount(balanceStr);
    }
    
    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) {
            return null;
        }
        
        try {
            // Remove currency symbols and commas
            String cleaned = amountStr
                .replaceAll("[^0-9.]", "")
                .trim();
            
            if (cleaned.isEmpty()) {
                return null;
            }
            
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse amount: {}", amountStr);
            return null;
        }
    }
    
    private LocalDate extractDate(Map<String, String> fields) {
        String dateStr = extractField(fields, "date", "transactionDate", "txnDate", "dt");
        
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDate.now();
        }
        
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        
        log.warn("Failed to parse date: {}", dateStr);
        return LocalDate.now();
    }
    
    private TransactionType determineTransactionType(Map<String, String> fields, RegexTemplate template) {
        String typeStr = extractField(fields, "type", "transactionType", "txnType");
        
        if (typeStr != null) {
            typeStr = typeStr.toUpperCase();
            if (typeStr.contains("DEBIT") || typeStr.contains("DR")) {
                return TransactionType.DEBIT;
            } else if (typeStr.contains("CREDIT") || typeStr.contains("CR")) {
                return TransactionType.CREDIT;
            }
        }
        
        // Fallback to template SMS type
        return switch (template.getSmsType()) {
            case DEBIT -> TransactionType.DEBIT;
            case CREDIT -> TransactionType.CREDIT;
            case BILL -> TransactionType.BILL_PAYMENT;
        };
    }
    
    private String extractField(Map<String, String> fields, String... possibleKeys) {
        for (String key : possibleKeys) {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
}

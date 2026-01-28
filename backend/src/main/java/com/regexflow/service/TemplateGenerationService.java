package com.regexflow.service;

import com.regexflow.dto.GeneratedTemplateResponse;
import com.regexflow.enums.SmsType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TemplateGenerationService {
    
    /**
     * Generates a regex template from SMS text using pattern recognition
     */
    public GeneratedTemplateResponse generateTemplate(String smsText, String senderHeader) {
        log.info("Generating template for SMS from sender: {}", senderHeader);
        
        try {
            // Detect bank name from sender or content
            String bankName = detectBankName(smsText, senderHeader);
            
            // Detect SMS type
            SmsType smsType = detectSmsType(smsText);
            
            // Generate regex pattern based on SMS type
            String regexPattern = generateRegexPattern(smsText, smsType);
            
            // Generate description
            String description = generateDescription(bankName, smsType);
            
            return GeneratedTemplateResponse.builder()
                    .success(true)
                    .regexPattern(regexPattern)
                    .bankName(bankName)
                    .smsType(smsType)
                    .description(description)
                    .sampleSms(smsText)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to generate template", e);
            return GeneratedTemplateResponse.builder()
                    .success(false)
                    .errorMessage("Failed to generate template: " + e.getMessage())
                    .sampleSms(smsText)
                    .build();
        }
    }
    
    private String detectBankName(String smsText, String senderHeader) {
        // Try to detect bank name from sender header first
        if (senderHeader != null && !senderHeader.isEmpty()) {
            String cleaned = senderHeader.replaceAll("[^a-zA-Z]", "").toUpperCase();
            if (cleaned.length() >= 2) {
                return capitalizeFirstLetter(cleaned);
            }
        }
        
        // Common bank name patterns
        String[] bankKeywords = {"HDFC", "ICICI", "SBI", "AXIS", "KOTAK", "PNB", "BOB", 
                                  "CANARA", "UNION", "INDUSIND", "YES", "IDFC", "RBL"};
        
        for (String bank : bankKeywords) {
            if (smsText.toUpperCase().contains(bank)) {
                return bank;
            }
        }
        
        // Default to sender header or "Unknown Bank"
        return senderHeader != null && !senderHeader.isEmpty() ? 
               capitalizeFirstLetter(senderHeader.replaceAll("[^a-zA-Z]", "")) : 
               "UnknownBank";
    }
    
    private SmsType detectSmsType(String smsText) {
        String upperText = smsText.toUpperCase();
        
        // Credit detection (check first as it's more specific)
        if (upperText.contains("CREDITED") || upperText.contains("CREDIT") || 
            upperText.contains("RECEIVED") || upperText.contains("DEPOSITED")) {
            return SmsType.CREDIT;
        }
        
        // Debit detection
        if (upperText.contains("DEBITED") || upperText.contains("DEBIT") || 
            upperText.contains("PAID") || upperText.contains("PURCHASE") ||
            upperText.contains("WITHDRAWN") || upperText.contains("SPENT")) {
            return SmsType.DEBIT;
        }
        
        // Bill payment detection
        if (upperText.contains("BILL") || upperText.contains("PAYMENT") ||
            upperText.contains("UTILITY")) {
            return SmsType.BILL;
        }
        
        // Default to DEBIT (most common)
        return SmsType.DEBIT;
    }
    
    private String generateRegexPattern(String smsText, SmsType smsType) {
        StringBuilder pattern = new StringBuilder();
        
        // Start with flexible beginning
        pattern.append("(?i)"); // Case insensitive
        
        // Detect and replace amounts
        Pattern amountPattern = Pattern.compile("(?:Rs\\.?|INR)?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE);
        Matcher amountMatcher = amountPattern.matcher(smsText);
        
        String workingText = smsText;
        
        // Replace first amount with named group
        if (amountMatcher.find()) {
            String amountStr = amountMatcher.group(0);
            workingText = workingText.replace(amountStr, "AMOUNT_PLACEHOLDER");
        }
        
        // Replace second amount (balance) with named group if exists
        amountMatcher = amountPattern.matcher(workingText);
        if (amountMatcher.find()) {
            String balanceStr = amountMatcher.group(0);
            workingText = workingText.replace(balanceStr, "BALANCE_PLACEHOLDER");
        }
        
        // Replace dates
        Pattern datePattern = Pattern.compile("\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}");
        workingText = datePattern.matcher(workingText).replaceAll("DATE_PLACEHOLDER");
        
        // Replace account numbers (4 or more digits)
        Pattern accountPattern = Pattern.compile("\\*{2,}\\d{4,}");
        workingText = accountPattern.matcher(workingText).replaceAll("ACCOUNT_PLACEHOLDER");
        
        // Replace reference numbers (alphanumeric sequences)
        Pattern refPattern = Pattern.compile("\\b[A-Z0-9]{10,}\\b");
        workingText = refPattern.matcher(workingText).replaceAll("REF_PLACEHOLDER");
        
        // Escape special regex characters
        workingText = workingText.replaceAll("([.+*?^$()\\[\\]{}|\\\\])", "\\\\$1");
        
        // Replace placeholders with named groups
        workingText = workingText.replace("AMOUNT_PLACEHOLDER", 
                "(?:Rs\\\\.?|INR)?\\\\s*(?<amount>[0-9,]+\\\\.?[0-9]*)");
        workingText = workingText.replace("BALANCE_PLACEHOLDER", 
                "(?:Rs\\\\.?|INR)?\\\\s*(?<balance>[0-9,]+\\\\.?[0-9]*)");
        workingText = workingText.replace("DATE_PLACEHOLDER", 
                "(?<date>\\\\d{1,2}[-/]\\\\d{1,2}[-/]\\\\d{2,4})");
        workingText = workingText.replace("ACCOUNT_PLACEHOLDER", 
                "(?<accountId>\\\\*{2,}\\\\d{4,})");
        workingText = workingText.replace("REF_PLACEHOLDER", 
                "(?<referenceNumber>[A-Z0-9]{10,})");
        
        // Add flexible whitespace
        workingText = workingText.replaceAll("\\s+", "\\\\s+");
        
        // Look for merchant/payee info (to/at/from patterns)
        if (smsText.matches("(?i).*\\b(to|at|from)\\s+([A-Za-z0-9\\s]+?)\\s+(on|dated|ref).*")) {
            workingText = workingText.replaceFirst(
                    "\\\\s+(to|at|from)\\\\s+[^\\\\s]+",
                    "\\\\s+(?:to|at|from)\\\\s+(?<merchantOrPayee>[A-Za-z0-9\\\\s]+?)\\\\s+"
            );
        }
        
        pattern.append(workingText);
        
        return pattern.toString();
    }
    
    private String generateDescription(String bankName, SmsType smsType) {
        return String.format("Auto-generated template for %s %s transactions", 
                            bankName, smsType.toString().toLowerCase());
    }
    
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}

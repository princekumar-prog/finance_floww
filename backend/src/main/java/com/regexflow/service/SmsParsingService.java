package com.regexflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regexflow.dto.ParsedTransactionResponse;
import com.regexflow.dto.SmsParseRequest;
import com.regexflow.dto.UnparsedSmsResponse;
import com.regexflow.entity.ParsedTransaction;
import com.regexflow.entity.RawSmsLog;
import com.regexflow.entity.RegexTemplate;
import com.regexflow.entity.User;
import com.regexflow.enums.ParseStatus;
import com.regexflow.exception.ResourceNotFoundException;
import com.regexflow.parser.SmsParser;
import com.regexflow.repository.ParsedTransactionRepository;
import com.regexflow.repository.RawSmsLogRepository;
import com.regexflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsParsingService {
    
    private final RawSmsLogRepository rawSmsLogRepository;
    private final ParsedTransactionRepository parsedTransactionRepository;
    private final UserRepository userRepository;
    private final RegexTemplateService templateService;
    private final SmsParser smsParser;
    private final ObjectMapper objectMapper;
    
    /**
     * Parses an SMS and creates transaction record
     */
    @Transactional
    public ParsedTransactionResponse parseSms(SmsParseRequest request, String username) {
        log.info("Parsing SMS for user: {}", username);
        
        User user = findUserByUsername(username);
        
        // Check if this exact SMS has already been parsed by this user
        Optional<RawSmsLog> existingSms = rawSmsLogRepository.findByUploadedByAndRawSmsText(
            user, 
            request.getSmsText()
        );
        
        if (existingSms.isPresent()) {
            log.info("Duplicate SMS detected for user: {}. Skipping save.", username);
            RawSmsLog duplicateSms = existingSms.get();
            
            // If there's an existing transaction for this SMS, return it
            ParsedTransaction existingTransaction = parsedTransactionRepository
                .findBySmsLog(duplicateSms)
                .orElse(null);
            
            if (existingTransaction != null) {
                return mapToResponse(existingTransaction);
            }
            
            // If no transaction exists (e.g., SMS failed to parse), return the existing SMS status
            return ParsedTransactionResponse.builder()
                .parseStatus(duplicateSms.getParseStatus())
                .errorMessage("SMS already exists. " + 
                    (duplicateSms.getErrorMessage() != null ? duplicateSms.getErrorMessage() : ""))
                .rawSmsText(duplicateSms.getRawSmsText())
                .build();
        }
        
        // Get all active templates
        List<RegexTemplate> activeTemplates = templateService.getActiveTemplates();
        
        // Parse SMS with best matching template
        SmsParser.ParseResult parseResult = smsParser.parseSmsWithBestMatch(
            request.getSmsText(),
            activeTemplates
        );
        
        // Save raw SMS log
        RawSmsLog smsLog = RawSmsLog.builder()
            .rawSmsText(request.getSmsText())
            .senderHeader(request.getSenderHeader())
            .parseStatus(parseResult.getStatus())
            .template(parseResult.getMatchedTemplate())
            .uploadedBy(user)
            .errorMessage(parseResult.getErrorMessage())
            .build();
        
        smsLog = rawSmsLogRepository.save(smsLog);
        
        // If parsing was successful or partial, create transaction record
        if (parseResult.getStatus() == ParseStatus.SUCCESS || 
            parseResult.getStatus() == ParseStatus.PARTIAL) {
            
            ParsedTransaction transaction = ParsedTransaction.builder()
                .smsLog(smsLog)
                .user(user)
                .template(parseResult.getMatchedTemplate())
                .bankName(parseResult.getBankName())
                .amount(parseResult.getAmount())
                .balance(parseResult.getBalance())
                .transactionType(parseResult.getTransactionType())
                .accountId(parseResult.getAccountId())
                .merchantOrPayee(parseResult.getMerchantOrPayee())
                .mode(parseResult.getMode())
                .transactionDate(parseResult.getTransactionDate())
                .referenceNumber(parseResult.getReferenceNumber())
                .extractedData(mapToJson(parseResult.getExtractedFields()))
                .build();
            
            transaction = parsedTransactionRepository.save(transaction);
            
            return mapToResponse(transaction, smsLog, parseResult);
        }
        
        // Return error response
        return ParsedTransactionResponse.builder()
            .parseStatus(parseResult.getStatus())
            .errorMessage(parseResult.getErrorMessage())
            .rawSmsText(request.getSmsText())
            .build();
    }
    
    /**
     * Gets transaction history for a user
     */
    @Transactional(readOnly = true)
    public Page<ParsedTransactionResponse> getTransactionHistory(String username, Pageable pageable) {
        log.info("Fetching transaction history for user: {}", username);
        
        User user = findUserByUsername(username);
        Page<ParsedTransaction> transactions = parsedTransactionRepository
            .findByUserOrderByCreatedAtDesc(user, pageable);
        
        return transactions.map(this::mapToResponse);
    }
    
    /**
     * Gets transactions filtered by various criteria
     */
    @Transactional(readOnly = true)
    public Page<ParsedTransactionResponse> getFilteredTransactions(
            String username,
            String bankName,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable) {
        
        log.info("Fetching filtered transactions for user: {}", username);
        
        User user = findUserByUsername(username);
        
        Page<ParsedTransaction> transactions = parsedTransactionRepository.findByFilters(
            user, bankName, startDate, endDate, minAmount, maxAmount, pageable
        );
        
        return transactions.map(this::mapToResponse);
    }
    
    /**
     * Gets a single transaction by ID
     */
    @Transactional(readOnly = true)
    public ParsedTransactionResponse getTransactionById(Long id, String username) {
        User user = findUserByUsername(username);
        
        ParsedTransaction transaction = parsedTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
        
        // Verify transaction belongs to the user
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        
        return mapToResponse(transaction);
    }
    
    /**
     * Gets all SMS logs for a user
     */
    @Transactional(readOnly = true)
    public Page<RawSmsLog> getSmsLogs(String username, Pageable pageable) {
        User user = findUserByUsername(username);
        return rawSmsLogRepository.findByUploadedByOrderByCreatedAtDesc(user, pageable);
    }
    
    /**
     * Gets unparsed SMS (no match found)
     */
    @Transactional(readOnly = true)
    public List<RawSmsLog> getUnparsedSms(String username) {
        User user = findUserByUsername(username);
        return rawSmsLogRepository.findByUploadedByAndParseStatus(user, ParseStatus.NO_MATCH);
    }
    
    /**
     * Gets unparsed SMS for maker dashboard (converted to DTO)
     * Shows ALL unparsed SMS regardless of who uploaded them
     */
    @Transactional(readOnly = true)
    public List<UnparsedSmsResponse> getUnparsedSmsForMaker(String username) {
        log.info("Fetching all unparsed SMS for maker: {}", username);
        
        // Makers see ALL unparsed SMS so they can create templates for them
        List<RawSmsLog> unparsedSms = rawSmsLogRepository.findByParseStatus(ParseStatus.NO_MATCH);
        return unparsedSms.stream()
                .map(this::mapToUnparsedSmsResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Deletes an unparsed SMS
     * Makers can delete any unparsed SMS to manage the action needed list
     */
    @Transactional
    public void deleteUnparsedSms(Long id, String username) {
        log.info("Deleting unparsed SMS {} by maker: {}", id, username);
        
        RawSmsLog smsLog = rawSmsLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SMS not found with ID: " + id));
        
        // Only allow deleting unparsed SMS
        if (smsLog.getParseStatus() != ParseStatus.NO_MATCH) {
            throw new IllegalStateException("Can only delete unparsed SMS");
        }
        
        rawSmsLogRepository.delete(smsLog);
        log.info("Successfully deleted unparsed SMS {}", id);
    }
    
    private UnparsedSmsResponse mapToUnparsedSmsResponse(RawSmsLog smsLog) {
        return UnparsedSmsResponse.builder()
                .id(smsLog.getId())
                .rawSmsText(smsLog.getRawSmsText())
                .senderHeader(smsLog.getSenderHeader())
                .errorMessage(smsLog.getErrorMessage())
                .uploadedByUsername(smsLog.getUploadedBy().getUsername())
                .createdAt(smsLog.getCreatedAt())
                .build();
    }
    
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
    
    private ParsedTransactionResponse mapToResponse(ParsedTransaction transaction) {
        return ParsedTransactionResponse.builder()
            .id(transaction.getId())
            .parseStatus(ParseStatus.SUCCESS)
            .bankName(transaction.getBankName())
            .amount(transaction.getAmount())
            .balance(transaction.getBalance())
            .transactionType(transaction.getTransactionType())
            .accountId(transaction.getAccountId())
            .merchantOrPayee(transaction.getMerchantOrPayee())
            .mode(transaction.getMode())
            .transactionDate(transaction.getTransactionDate())
            .referenceNumber(transaction.getReferenceNumber())
            .extractedData(parseJson(transaction.getExtractedData()))
            .rawSmsText(transaction.getSmsLog().getRawSmsText())
            .templateUsed(transaction.getTemplate() != null ? 
                transaction.getTemplate().getBankName() + " - " + transaction.getTemplate().getSmsType() : null)
            .templateId(transaction.getTemplate() != null ? transaction.getTemplate().getId() : null)
            .createdAt(transaction.getCreatedAt())
            .build();
    }
    
    private ParsedTransactionResponse mapToResponse(
            ParsedTransaction transaction,
            RawSmsLog smsLog,
            SmsParser.ParseResult parseResult) {
        
        return ParsedTransactionResponse.builder()
            .id(transaction.getId())
            .parseStatus(parseResult.getStatus())
            .bankName(transaction.getBankName())
            .amount(transaction.getAmount())
            .balance(transaction.getBalance())
            .transactionType(transaction.getTransactionType())
            .accountId(transaction.getAccountId())
            .merchantOrPayee(transaction.getMerchantOrPayee())
            .mode(transaction.getMode())
            .transactionDate(transaction.getTransactionDate())
            .referenceNumber(transaction.getReferenceNumber())
            .extractedData(parseResult.getExtractedFields())
            .rawSmsText(smsLog.getRawSmsText())
            .templateUsed(parseResult.getMatchedTemplate() != null ? 
                parseResult.getMatchedTemplate().getBankName() + " - " + parseResult.getMatchedTemplate().getSmsType() : null)
            .templateId(parseResult.getMatchedTemplate() != null ? parseResult.getMatchedTemplate().getId() : null)
            .errorMessage(parseResult.getErrorMessage())
            .createdAt(transaction.getCreatedAt())
            .build();
    }
    
    private String mapToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize to JSON", e);
            return "{}";
        }
    }
    
    @SuppressWarnings("unchecked")
    private java.util.Map<String, String> parseJson(String json) {
        try {
            return objectMapper.readValue(json, java.util.Map.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON", e);
            return new java.util.HashMap<>();
        }
    }
}

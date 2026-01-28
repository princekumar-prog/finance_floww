package com.regexflow.controller;

import com.regexflow.dto.ApiResponse;
import com.regexflow.dto.ParsedTransactionResponse;
import com.regexflow.dto.SmsParseRequest;
import com.regexflow.service.SmsParsingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final SmsParsingService smsParsingService;
    
    /**
     * Parse an SMS message
     */
    @PostMapping("/sms/parse")
    public ResponseEntity<ApiResponse<ParsedTransactionResponse>> parseSms(
            @Valid @RequestBody SmsParseRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        ParsedTransactionResponse response = smsParsingService.parseSms(request, username);
        
        return ResponseEntity.ok(ApiResponse.success(response, "SMS parsed successfully"));
    }
    
    /**
     * Get transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<ParsedTransactionResponse>>> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<ParsedTransactionResponse> transactions = smsParsingService.getTransactionHistory(username, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(transactions, "Transactions retrieved successfully"));
    }
    
    /**
     * Get filtered transactions
     */
    @GetMapping("/transactions/filter")
    public ResponseEntity<ApiResponse<Page<ParsedTransactionResponse>>> getFilteredTransactions(
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<ParsedTransactionResponse> transactions = smsParsingService.getFilteredTransactions(
            username, bankName, startDate, endDate, minAmount, maxAmount, pageable
        );
        
        return ResponseEntity.ok(ApiResponse.success(transactions, "Filtered transactions retrieved successfully"));
    }
    
    /**
     * Get a specific transaction by ID
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<ParsedTransactionResponse>> getTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        
        String username = authentication.getName();
        ParsedTransactionResponse transaction = smsParsingService.getTransactionById(id, username);
        
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }
}

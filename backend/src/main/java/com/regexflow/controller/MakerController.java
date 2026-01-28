package com.regexflow.controller;

import com.regexflow.dto.*;
import com.regexflow.service.RegexTemplateService;
import com.regexflow.service.SmsParsingService;
import com.regexflow.service.TemplateGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maker")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MakerController {
    
    private final RegexTemplateService templateService;
    private final SmsParsingService smsParsingService;
    private final TemplateGenerationService templateGenerationService;
    
    /**
     * Create a new regex template
     */
    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<RegexTemplateResponse>> createTemplate(
            @Valid @RequestBody RegexTemplateRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        RegexTemplateResponse response = templateService.createTemplate(request, username);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Template created successfully"));
    }
    
    /**
     * Test a regex pattern against sample SMS
     */
    @PostMapping("/templates/test")
    public ResponseEntity<ApiResponse<RegexTestResponse>> testRegex(
            @Valid @RequestBody RegexTestRequest request) {
        
        RegexTestResponse response = templateService.testRegex(request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Regex tested successfully"));
    }
    
    /**
     * Update an existing template (only DRAFT status)
     */
    @PutMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<RegexTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody RegexTemplateRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        RegexTemplateResponse response = templateService.updateTemplate(id, request, username);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Template updated successfully"));
    }
    
    /**
     * Submit a template for approval
     */
    @PostMapping("/templates/{id}/submit")
    public ResponseEntity<ApiResponse<RegexTemplateResponse>> submitForApproval(
            @PathVariable Long id,
            Authentication authentication) {
        
        String username = authentication.getName();
        RegexTemplateResponse response = templateService.submitForApproval(id, username);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Template submitted for approval"));
    }
    
    /**
     * Get all templates created by the maker
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<RegexTemplateResponse>>> getMyTemplates(
            Authentication authentication) {
        
        String username = authentication.getName();
        List<RegexTemplateResponse> templates = templateService.getMyTemplates(username);
        
        return ResponseEntity.ok(ApiResponse.success(templates, "Templates retrieved successfully"));
    }
    
    /**
     * Get a specific template by ID
     */
    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<RegexTemplateResponse>> getTemplate(@PathVariable Long id) {
        RegexTemplateResponse template = templateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(template));
    }
    
    /**
     * Check if a duplicate regex pattern exists
     */
    @GetMapping("/templates/check-duplicate")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkDuplicate(
            @RequestParam String regexPattern,
            @RequestParam(required = false) Long excludeId) {
        
        DuplicateCheckResponse response = templateService.checkDuplicate(regexPattern, excludeId);
        
        return ResponseEntity.ok(ApiResponse.success(response, 
            response.isExists() ? "Duplicate regex pattern found" : "No duplicate found"));
    }
    
    /**
     * Get all unparsed SMS (action needed items)
     */
    @GetMapping("/unparsed-sms")
    public ResponseEntity<ApiResponse<List<UnparsedSmsResponse>>> getUnparsedSms(
            Authentication authentication) {
        
        String username = authentication.getName();
        List<UnparsedSmsResponse> unparsedSms = smsParsingService.getUnparsedSmsForMaker(username);
        
        return ResponseEntity.ok(ApiResponse.success(unparsedSms, "Unparsed SMS retrieved successfully"));
    }
    
    /**
     * Delete an unparsed SMS
     */
    @DeleteMapping("/unparsed-sms/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUnparsedSms(
            @PathVariable Long id,
            Authentication authentication) {
        
        String username = authentication.getName();
        smsParsingService.deleteUnparsedSms(id, username);
        
        return ResponseEntity.ok(ApiResponse.success(null, "SMS deleted successfully"));
    }
    
    /**
     * Generate regex template from SMS
     */
    @PostMapping("/generate-template")
    public ResponseEntity<ApiResponse<GeneratedTemplateResponse>> generateTemplate(
            @Valid @RequestBody GenerateTemplateRequest request) {
        
        GeneratedTemplateResponse response = templateGenerationService.generateTemplate(
                request.getSmsText(), 
                request.getSenderHeader()
        );
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response, "Template generated successfully"));
        } else {
            return ResponseEntity.ok(ApiResponse.success(response, response.getErrorMessage()));
        }
    }
}

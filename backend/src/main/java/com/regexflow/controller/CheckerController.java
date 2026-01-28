package com.regexflow.controller;

import com.regexflow.dto.ApiResponse;
import com.regexflow.dto.RegexTemplateResponse;
import com.regexflow.dto.RegexTestRequest;
import com.regexflow.dto.RegexTestResponse;
import com.regexflow.dto.TemplateApprovalRequest;
import com.regexflow.dto.TemplateRejectionRequest;
import com.regexflow.service.RegexTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checker")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CheckerController {
    
    private final RegexTemplateService templateService;
    
    /**
     * Get all pending templates waiting for approval
     */
    @GetMapping("/templates/pending")
    public ResponseEntity<ApiResponse<List<RegexTemplateResponse>>> getPendingTemplates() {
        List<RegexTemplateResponse> templates = templateService.getPendingTemplates();
        return ResponseEntity.ok(ApiResponse.success(templates, "Pending templates retrieved successfully"));
    }
    
    /**
     * Get all templates reviewed (approved/rejected) by the current checker or all reviewed templates
     * @param all - if true, returns all reviewed templates; if false or not provided, returns only current checker's reviews
     */
    @GetMapping("/templates/reviewed")
    public ResponseEntity<ApiResponse<List<RegexTemplateResponse>>> getReviewedTemplates(
            @RequestParam(required = false, defaultValue = "true") boolean all,
            Authentication authentication) {
        
        List<RegexTemplateResponse> templates;
        if (all) {
            templates = templateService.getAllReviewedTemplates();
        } else {
            String username = authentication.getName();
            templates = templateService.getReviewedTemplates(username);
        }
        
        return ResponseEntity.ok(ApiResponse.success(templates, "Reviewed templates retrieved successfully"));
    }
    
    /**
     * Get a specific template by ID for review
     */
    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<RegexTemplateResponse>> getTemplate(@PathVariable Long id) {
        RegexTemplateResponse template = templateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(template));
    }
    
    /**
     * Approve a template
     */
    @PostMapping("/templates/{id}/approve")
    public ResponseEntity<ApiResponse<RegexTemplateResponse>> approveTemplate(
            @PathVariable Long id,
            @RequestBody TemplateApprovalRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        RegexTemplateResponse response = templateService.approveTemplate(id, request, username);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Template approved successfully"));
    }
    
    /**
     * Reject a template
     */
    @PostMapping("/templates/{id}/reject")
    public ResponseEntity<ApiResponse<RegexTemplateResponse>> rejectTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateRejectionRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        RegexTemplateResponse response = templateService.rejectTemplate(id, request, username);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Template rejected"));
    }
    
    /**
     * Deprecate an active template
     */
    @PostMapping("/templates/{id}/deprecate")
    public ResponseEntity<ApiResponse<RegexTemplateResponse>> deprecateTemplate(
            @PathVariable Long id,
            @RequestBody(required = false) TemplateApprovalRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        String comments = request != null ? request.getComments() : "Template deprecated";
        RegexTemplateResponse response = templateService.deprecateTemplate(id, comments, username);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Template deprecated successfully"));
    }
    
    /**
     * Test a regex pattern against sample SMS
     * Checkers can use this to test templates during review
     */
    @PostMapping("/templates/test")
    public ResponseEntity<ApiResponse<RegexTestResponse>> testRegex(
            @Valid @RequestBody RegexTestRequest request) {
        
        RegexTestResponse response = templateService.testRegex(request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Regex tested successfully"));
    }
}

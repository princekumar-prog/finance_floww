package com.regexflow.service;

import com.regexflow.dto.*;
import com.regexflow.entity.RegexTemplate;
import com.regexflow.entity.User;
import com.regexflow.enums.TemplateStatus;
import com.regexflow.exception.ResourceNotFoundException;
import com.regexflow.parser.RegexParser;
import com.regexflow.repository.RegexTemplateRepository;
import com.regexflow.repository.UserRepository;
import com.regexflow.workflow.TemplateWorkflowManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegexTemplateService {
    
    private final RegexTemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final RegexParser regexParser;
    private final TemplateWorkflowManager workflowManager;
    
    /**
     * Creates a new regex template in DRAFT status
     */
    @Transactional
    public RegexTemplateResponse createTemplate(RegexTemplateRequest request, String username) {
        log.info("Creating new template by user: {}", username);
        
        User user = findUserByUsername(username);
        
        // Validate regex pattern
        regexParser.validatePattern(request.getRegexPattern());
        
        RegexTemplate template = RegexTemplate.builder()
            .bankName(request.getBankName())
            .regexPattern(request.getRegexPattern())
            .smsType(request.getSmsType())
            .sampleSms(request.getSampleSms())
            .description(request.getDescription())
            .status(TemplateStatus.DRAFT)
            .createdBy(user)
            .build();
        
        template = templateRepository.save(template);
        
        log.info("Template created with ID: {}", template.getId());
        
        return mapToResponse(template);
    }
    
    /**
     * Tests a regex pattern against sample SMS
     */
    public RegexTestResponse testRegex(RegexTestRequest request) {
        log.info("Testing regex pattern");
        
        long startTime = System.currentTimeMillis();
        
        try {
            Map<String, String> fields = regexParser.testPattern(
                request.getRegexPattern(),
                request.getSampleSms()
            );
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            return RegexTestResponse.builder()
                .matched(!fields.isEmpty())
                .extractedFields(fields)
                .executionTimeMs(executionTime)
                .build();
                
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            return RegexTestResponse.builder()
                .matched(false)
                .errorMessage(e.getMessage())
                .executionTimeMs(executionTime)
                .build();
        }
    }
    
    /**
     * Updates a template (only if in DRAFT status)
     */
    @Transactional
    public RegexTemplateResponse updateTemplate(Long id, RegexTemplateRequest request, String username) {
        log.info("Updating template ID: {} by user: {}", id, username);
        
        User user = findUserByUsername(username);
        RegexTemplate template = findTemplateById(id);
        
        if (!workflowManager.canEdit(template, user)) {
            throw new IllegalStateException("Template cannot be edited in current status or by this user");
        }
        
        // Validate new regex pattern
        regexParser.validatePattern(request.getRegexPattern());
        
        template.setBankName(request.getBankName());
        template.setRegexPattern(request.getRegexPattern());
        template.setSmsType(request.getSmsType());
        template.setSampleSms(request.getSampleSms());
        template.setDescription(request.getDescription());
        
        template = templateRepository.save(template);
        
        return mapToResponse(template);
    }
    
    /**
     * Submits a template for approval
     */
    @Transactional
    public RegexTemplateResponse submitForApproval(Long id, String username) {
        log.info("Submitting template ID: {} for approval by user: {}", id, username);
        
        User user = findUserByUsername(username);
        RegexTemplate template = findTemplateById(id);
        
        workflowManager.submitForApproval(template, user);
        template = templateRepository.save(template);
        
        return mapToResponse(template);
    }
    
    /**
     * Gets all pending templates for checker review
     */
    @Transactional(readOnly = true)
    public List<RegexTemplateResponse> getPendingTemplates() {
        log.info("Fetching pending templates");
        
        List<RegexTemplate> templates = templateRepository.findPendingTemplates(TemplateStatus.PENDING_APPROVAL);
        return templates.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Approves a template
     */
    @Transactional
    public RegexTemplateResponse approveTemplate(Long id, TemplateApprovalRequest request, String username) {
        log.info("Approving template ID: {} by user: {}", id, username);
        
        User checker = findUserByUsername(username);
        RegexTemplate template = findTemplateById(id);
        
        workflowManager.approveTemplate(template, checker, request.getComments());
        template = templateRepository.save(template);
        
        return mapToResponse(template);
    }
    
    /**
     * Rejects a template
     */
    @Transactional
    public RegexTemplateResponse rejectTemplate(Long id, TemplateRejectionRequest request, String username) {
        log.info("Rejecting template ID: {} by user: {}", id, username);
        
        User checker = findUserByUsername(username);
        RegexTemplate template = findTemplateById(id);
        
        workflowManager.rejectTemplate(template, checker, request.getReason());
        template = templateRepository.save(template);
        
        return mapToResponse(template);
    }
    
    /**
     * Deprecates a template
     */
    @Transactional
    public RegexTemplateResponse deprecateTemplate(Long id, String comments, String username) {
        log.info("Deprecating template ID: {} by user: {}", id, username);
        
        User checker = findUserByUsername(username);
        RegexTemplate template = findTemplateById(id);
        
        workflowManager.deprecateTemplate(template, checker, comments);
        template = templateRepository.save(template);
        
        return mapToResponse(template);
    }
    
    /**
     * Gets templates by status
     */
    @Transactional(readOnly = true)
    public List<RegexTemplateResponse> getTemplatesByStatus(TemplateStatus status) {
        List<RegexTemplate> templates = templateRepository.findByStatus(status);
        return templates.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets templates created by a specific user
     */
    @Transactional(readOnly = true)
    public List<RegexTemplateResponse> getMyTemplates(String username) {
        User user = findUserByUsername(username);
        List<RegexTemplate> templates = templateRepository.findByCreatedBy(user);
        return templates.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all active templates
     */
    @Transactional(readOnly = true)
    public List<RegexTemplate> getActiveTemplates() {
        return templateRepository.findAllActiveTemplates();
    }
    
    /**
     * Gets active templates by bank name
     */
    @Transactional(readOnly = true)
    public List<RegexTemplate> getActiveTemplatesByBank(String bankName) {
        return templateRepository.findActiveTemplatesByBank(bankName);
    }
    
    /**
     * Gets a template by ID
     */
    @Transactional(readOnly = true)
    public RegexTemplateResponse getTemplateById(Long id) {
        RegexTemplate template = findTemplateById(id);
        return mapToResponse(template);
    }
    
    /**
     * Gets templates reviewed (approved/rejected) by a specific checker
     */
    @Transactional(readOnly = true)
    public List<RegexTemplateResponse> getReviewedTemplates(String username) {
        log.info("Fetching reviewed templates by checker: {}", username);
        
        User checker = findUserByUsername(username);
        List<RegexTemplate> templates = templateRepository.findReviewedTemplatesByChecker(checker);
        return templates.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all reviewed templates (approved/rejected/deprecated) regardless of checker
     */
    @Transactional(readOnly = true)
    public List<RegexTemplateResponse> getAllReviewedTemplates() {
        log.info("Fetching all reviewed templates");
        
        List<RegexTemplate> templates = templateRepository.findAllReviewedTemplates();
        return templates.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Checks if a duplicate template exists based on regex pattern (PENDING_APPROVAL or ACTIVE status)
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkDuplicate(String regexPattern, Long excludeId) {
        log.info("Checking for duplicate regex pattern, excludeId={}", excludeId);
        
        List<RegexTemplate> duplicates = templateRepository.findDuplicateTemplates(regexPattern, excludeId);
        
        if (!duplicates.isEmpty()) {
            RegexTemplate duplicate = duplicates.get(0);
            return DuplicateCheckResponse.builder()
                .exists(true)
                .status(duplicate.getStatus())
                .message(String.format("This regex pattern already exists for %s (%s) with status: %s", 
                                      duplicate.getBankName(), duplicate.getSmsType(), duplicate.getStatus()))
                .build();
        }
        
        return DuplicateCheckResponse.builder()
            .exists(false)
            .message("No duplicate regex pattern found")
            .build();
    }
    
    private RegexTemplate findTemplateById(Long id) {
        return templateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + id));
    }
    
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
    
    private RegexTemplateResponse mapToResponse(RegexTemplate template) {
        return RegexTemplateResponse.builder()
            .id(template.getId())
            .bankName(template.getBankName())
            .regexPattern(template.getRegexPattern())
            .smsType(template.getSmsType())
            .status(template.getStatus())
            .sampleSms(template.getSampleSms())
            .description(template.getDescription())
            .createdByUsername(template.getCreatedBy().getUsername())
            .approvedByUsername(template.getApprovedBy() != null ? template.getApprovedBy().getUsername() : null)
            .approvedAt(template.getApprovedAt())
            .deprecatedAt(template.getDeprecatedAt())
            .rejectionReason(template.getRejectionReason())
            .createdAt(template.getCreatedAt())
            .updatedAt(template.getUpdatedAt())
            .build();
    }
}

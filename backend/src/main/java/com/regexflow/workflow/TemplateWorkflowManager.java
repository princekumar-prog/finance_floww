package com.regexflow.workflow;

import com.regexflow.entity.RegexAuditTrail;
import com.regexflow.entity.RegexTemplate;
import com.regexflow.entity.User;
import com.regexflow.enums.TemplateStatus;
import com.regexflow.exception.InvalidStateTransitionException;
import com.regexflow.repository.RegexAuditTrailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Manages the Maker-Checker workflow for regex templates
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateWorkflowManager {
    
    private final RegexAuditTrailRepository auditTrailRepository;
    
    /**
     * Validates and performs state transition
     */
    @Transactional
    public void transitionState(
            RegexTemplate template,
            TemplateStatus newStatus,
            User performedBy,
            String comments) {
        
        TemplateStatus currentStatus = template.getStatus();
        
        // Validate transition
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException(
                String.format("Cannot transition from %s to %s", currentStatus, newStatus)
            );
        }
        
        // Perform transition
        TemplateStatus previousStatus = template.getStatus();
        template.setStatus(newStatus);
        
        // Update additional fields based on new status
        switch (newStatus) {
            case ACTIVE:
                template.setApprovedBy(performedBy);
                template.setApprovedAt(LocalDateTime.now());
                break;
            case DEPRECATED:
                template.setDeprecatedAt(LocalDateTime.now());
                break;
            case REJECTED:
                template.setRejectionReason(comments);
                break;
        }
        
        // Create audit trail
        createAuditTrail(template, previousStatus, newStatus, performedBy, comments);
        
        log.info("Template {} transitioned from {} to {} by user {}",
            template.getId(), previousStatus, newStatus, performedBy.getUsername());
    }
    
    /**
     * Submit template for approval (DRAFT -> PENDING_APPROVAL)
     */
    @Transactional
    public void submitForApproval(RegexTemplate template, User maker) {
        validateMakerAction(template, maker);
        transitionState(template, TemplateStatus.PENDING_APPROVAL, maker, "Submitted for approval");
    }
    
    /**
     * Approve template (PENDING_APPROVAL -> ACTIVE)
     */
    @Transactional
    public void approveTemplate(RegexTemplate template, User checker, String comments) {
        validateCheckerAction(template, checker);
        transitionState(template, TemplateStatus.ACTIVE, checker, comments);
    }
    
    /**
     * Reject template (PENDING_APPROVAL -> REJECTED)
     */
    @Transactional
    public void rejectTemplate(RegexTemplate template, User checker, String reason) {
        validateCheckerAction(template, checker);
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        transitionState(template, TemplateStatus.REJECTED, checker, reason);
    }
    
    /**
     * Deprecate template (ACTIVE -> DEPRECATED)
     */
    @Transactional
    public void deprecateTemplate(RegexTemplate template, User checker, String comments) {
        validateCheckerAction(template, checker);
        transitionState(template, TemplateStatus.DEPRECATED, checker, comments);
    }
    
    /**
     * Validate that the user is the maker of the template
     */
    private void validateMakerAction(RegexTemplate template, User maker) {
        if (!template.getCreatedBy().getId().equals(maker.getId())) {
            throw new InvalidStateTransitionException("Only the template creator can perform this action");
        }
    }
    
    /**
     * Validate that checker is not the same as maker
     */
    private void validateCheckerAction(RegexTemplate template, User checker) {
        if (template.getCreatedBy().getId().equals(checker.getId())) {
            throw new InvalidStateTransitionException("Checker cannot be the same as Maker");
        }
    }
    
    /**
     * Creates an audit trail entry
     */
    private void createAuditTrail(
            RegexTemplate template,
            TemplateStatus previousStatus,
            TemplateStatus newStatus,
            User performedBy,
            String comments) {
        
        RegexAuditTrail audit = RegexAuditTrail.builder()
            .template(template)
            .previousStatus(previousStatus)
            .newStatus(newStatus)
            .action(getActionName(previousStatus, newStatus))
            .comments(comments)
            .performedBy(performedBy)
            .build();
        
        auditTrailRepository.save(audit);
    }
    
    /**
     * Gets human-readable action name
     */
    private String getActionName(TemplateStatus from, TemplateStatus to) {
        return switch (to) {
            case DRAFT -> "Created";
            case PENDING_APPROVAL -> "Submitted for Approval";
            case ACTIVE -> "Approved";
            case REJECTED -> "Rejected";
            case DEPRECATED -> "Deprecated";
        };
    }
    
    /**
     * Checks if a template can be edited
     */
    public boolean canEdit(RegexTemplate template, User user) {
        return template.getStatus() == TemplateStatus.DRAFT 
            && template.getCreatedBy().getId().equals(user.getId());
    }
    
    /**
     * Checks if a template can be submitted
     */
    public boolean canSubmit(RegexTemplate template, User user) {
        return template.getStatus() == TemplateStatus.DRAFT 
            && template.getCreatedBy().getId().equals(user.getId());
    }
    
    /**
     * Checks if a template can be approved/rejected
     */
    public boolean canApproveOrReject(RegexTemplate template, User user) {
        return template.getStatus() == TemplateStatus.PENDING_APPROVAL 
            && !template.getCreatedBy().getId().equals(user.getId());
    }
    
    /**
     * Checks if a template can be deprecated
     */
    public boolean canDeprecate(RegexTemplate template) {
        return template.getStatus() == TemplateStatus.ACTIVE;
    }
}

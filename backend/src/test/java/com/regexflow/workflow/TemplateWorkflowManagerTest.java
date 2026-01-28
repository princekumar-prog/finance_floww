package com.regexflow.workflow;

import com.regexflow.entity.RegexTemplate;
import com.regexflow.entity.User;
import com.regexflow.enums.SmsType;
import com.regexflow.enums.TemplateStatus;
import com.regexflow.enums.UserRole;
import com.regexflow.exception.InvalidStateTransitionException;
import com.regexflow.repository.RegexAuditTrailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateWorkflowManagerTest {
    
    @Mock
    private RegexAuditTrailRepository auditTrailRepository;
    
    @InjectMocks
    private TemplateWorkflowManager workflowManager;
    
    private User maker;
    private User checker;
    private RegexTemplate template;
    
    @BeforeEach
    void setUp() {
        maker = User.builder()
            .id(1L)
            .username("maker1")
            .role(UserRole.MAKER)
            .build();
        
        checker = User.builder()
            .id(2L)
            .username("checker1")
            .role(UserRole.CHECKER)
            .build();
        
        template = RegexTemplate.builder()
            .id(1L)
            .bankName("HDFC Bank")
            .regexPattern("test pattern")
            .smsType(SmsType.DEBIT)
            .status(TemplateStatus.DRAFT)
            .createdBy(maker)
            .build();
    }
    
    @Test
    void testTransitionState_validTransition() {
        template.setStatus(TemplateStatus.DRAFT);
        
        assertDoesNotThrow(() -> 
            workflowManager.transitionState(template, TemplateStatus.PENDING_APPROVAL, maker, "test")
        );
        
        assertEquals(TemplateStatus.PENDING_APPROVAL, template.getStatus());
        verify(auditTrailRepository, times(1)).save(any());
    }
    
    @Test
    void testTransitionState_invalidTransition() {
        template.setStatus(TemplateStatus.DEPRECATED);
        
        assertThrows(InvalidStateTransitionException.class, () -> 
            workflowManager.transitionState(template, TemplateStatus.ACTIVE, checker, "test")
        );
    }
    
    @Test
    void testSubmitForApproval_success() {
        template.setStatus(TemplateStatus.DRAFT);
        
        assertDoesNotThrow(() -> workflowManager.submitForApproval(template, maker));
        
        assertEquals(TemplateStatus.PENDING_APPROVAL, template.getStatus());
    }
    
    @Test
    void testApproveTemplate_success() {
        template.setStatus(TemplateStatus.PENDING_APPROVAL);
        
        assertDoesNotThrow(() -> workflowManager.approveTemplate(template, checker, "Approved"));
        
        assertEquals(TemplateStatus.ACTIVE, template.getStatus());
        assertNotNull(template.getApprovedBy());
        assertNotNull(template.getApprovedAt());
    }
    
    @Test
    void testApproveTemplate_sameUserAsChecker() {
        template.setStatus(TemplateStatus.PENDING_APPROVAL);
        
        assertThrows(InvalidStateTransitionException.class, () -> 
            workflowManager.approveTemplate(template, maker, "Approved")
        );
    }
    
    @Test
    void testRejectTemplate_success() {
        template.setStatus(TemplateStatus.PENDING_APPROVAL);
        
        assertDoesNotThrow(() -> workflowManager.rejectTemplate(template, checker, "Not correct"));
        
        assertEquals(TemplateStatus.REJECTED, template.getStatus());
        assertEquals("Not correct", template.getRejectionReason());
    }
    
    @Test
    void testRejectTemplate_emptyReason() {
        template.setStatus(TemplateStatus.PENDING_APPROVAL);
        
        assertThrows(IllegalArgumentException.class, () -> 
            workflowManager.rejectTemplate(template, checker, "")
        );
    }
    
    @Test
    void testDeprecateTemplate_success() {
        template.setStatus(TemplateStatus.ACTIVE);
        
        assertDoesNotThrow(() -> workflowManager.deprecateTemplate(template, checker, "Outdated"));
        
        assertEquals(TemplateStatus.DEPRECATED, template.getStatus());
        assertNotNull(template.getDeprecatedAt());
    }
    
    @Test
    void testCanEdit_draft() {
        template.setStatus(TemplateStatus.DRAFT);
        
        assertTrue(workflowManager.canEdit(template, maker));
        assertFalse(workflowManager.canEdit(template, checker));
    }
    
    @Test
    void testCanEdit_nonDraft() {
        template.setStatus(TemplateStatus.PENDING_APPROVAL);
        
        assertFalse(workflowManager.canEdit(template, maker));
    }
    
    @Test
    void testCanApproveOrReject() {
        template.setStatus(TemplateStatus.PENDING_APPROVAL);
        
        assertTrue(workflowManager.canApproveOrReject(template, checker));
        assertFalse(workflowManager.canApproveOrReject(template, maker));
    }
}

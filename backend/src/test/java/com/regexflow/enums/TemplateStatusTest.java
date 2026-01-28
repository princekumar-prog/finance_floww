package com.regexflow.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateStatusTest {
    
    @Test
    void testCanTransitionTo_draftToPendingApproval() {
        assertTrue(TemplateStatus.DRAFT.canTransitionTo(TemplateStatus.PENDING_APPROVAL));
    }
    
    @Test
    void testCanTransitionTo_draftToActive_invalid() {
        assertFalse(TemplateStatus.DRAFT.canTransitionTo(TemplateStatus.ACTIVE));
    }
    
    @Test
    void testCanTransitionTo_pendingApprovalToActive() {
        assertTrue(TemplateStatus.PENDING_APPROVAL.canTransitionTo(TemplateStatus.ACTIVE));
    }
    
    @Test
    void testCanTransitionTo_pendingApprovalToRejected() {
        assertTrue(TemplateStatus.PENDING_APPROVAL.canTransitionTo(TemplateStatus.REJECTED));
    }
    
    @Test
    void testCanTransitionTo_activeToDeprecated() {
        assertTrue(TemplateStatus.ACTIVE.canTransitionTo(TemplateStatus.DEPRECATED));
    }
    
    @Test
    void testCanTransitionTo_deprecatedToAny_invalid() {
        assertFalse(TemplateStatus.DEPRECATED.canTransitionTo(TemplateStatus.ACTIVE));
        assertFalse(TemplateStatus.DEPRECATED.canTransitionTo(TemplateStatus.DRAFT));
    }
    
    @Test
    void testCanTransitionTo_rejectedToDraft() {
        assertTrue(TemplateStatus.REJECTED.canTransitionTo(TemplateStatus.DRAFT));
    }
}

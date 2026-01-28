package com.regexflow.enums;

/**
 * Lifecycle status of a regex template
 */
public enum TemplateStatus {
    DRAFT("Template is being created"),
    PENDING_APPROVAL("Template submitted for checker approval"),
    ACTIVE("Template is approved and active for parsing"),
    REJECTED("Template was rejected by checker"),
    DEPRECATED("Template is no longer active");

    private final String description;

    TemplateStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Validates if transition from current status to next status is allowed
     */
    public boolean canTransitionTo(TemplateStatus nextStatus) {
        return switch (this) {
            case DRAFT -> nextStatus == PENDING_APPROVAL || nextStatus == DRAFT;
            case PENDING_APPROVAL -> nextStatus == ACTIVE || nextStatus == REJECTED;
            case ACTIVE -> nextStatus == DEPRECATED;
            case REJECTED -> nextStatus == DRAFT;
            case DEPRECATED -> false; // Cannot transition from deprecated
        };
    }
}

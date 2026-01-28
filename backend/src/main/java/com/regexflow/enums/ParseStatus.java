package com.regexflow.enums;

/**
 * Status of SMS parsing operation
 */
public enum ParseStatus {
    SUCCESS("SMS successfully parsed"),
    PARTIAL("SMS partially parsed with missing fields"),
    FAILED("SMS parsing failed"),
    NO_MATCH("No matching template found"),
    ERROR("Error occurred during parsing");

    private final String description;

    ParseStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

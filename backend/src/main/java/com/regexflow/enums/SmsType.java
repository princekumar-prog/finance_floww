package com.regexflow.enums;

/**
 * Types of SMS transactions
 */
public enum SmsType {
    DEBIT("Money debited from account"),
    CREDIT("Money credited to account"),
    BILL("Bill payment transaction");

    private final String description;

    SmsType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

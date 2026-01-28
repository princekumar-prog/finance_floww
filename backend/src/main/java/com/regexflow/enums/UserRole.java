package com.regexflow.enums;

/**
 * User roles in the RegexFlow system
 */
public enum UserRole {
    MAKER("Maker - Creates and tests regex templates"),
    CHECKER("Checker - Reviews and approves templates"),
    NORMAL_USER("Normal User - Uploads SMS and views transactions");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

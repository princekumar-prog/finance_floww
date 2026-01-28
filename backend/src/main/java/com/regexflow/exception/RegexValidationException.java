package com.regexflow.exception;

public class RegexValidationException extends RuntimeException {
    public RegexValidationException(String message) {
        super(message);
    }
    
    public RegexValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

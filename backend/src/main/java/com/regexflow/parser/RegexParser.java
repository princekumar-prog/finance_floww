package com.regexflow.parser;

import com.regexflow.exception.RegexValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
@Slf4j
public class RegexParser {
    
    private static final long DEFAULT_TIMEOUT_MS = 5000;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    /**
     * Validates a regex pattern for syntax and potential catastrophic backtracking
     */
    public void validatePattern(String regexPattern) {
        if (regexPattern == null || regexPattern.isBlank()) {
            throw new RegexValidationException("Regex pattern cannot be empty");
        }
        
        try {
            Pattern.compile(regexPattern);
        } catch (PatternSyntaxException e) {
            throw new RegexValidationException("Invalid regex pattern: " + e.getMessage(), e);
        }
        
        // Check for potentially dangerous patterns
        checkForCatastrophicBacktracking(regexPattern);
    }
    
    /**
     * Checks for patterns that might cause catastrophic backtracking
     */
    private void checkForCatastrophicBacktracking(String pattern) {
        // Common patterns that can cause catastrophic backtracking
        String[] dangerousPatterns = {
            "(.*)*",
            "(.+)+",
            "(a*)*",
            "(a+)+",
            "(a|a)*",
            "(a|ab)*"
        };
        
        for (String dangerous : dangerousPatterns) {
            if (pattern.contains(dangerous)) {
                log.warn("Potentially dangerous pattern detected: {}", dangerous);
                throw new RegexValidationException(
                    "Pattern contains potentially dangerous construct that may cause catastrophic backtracking: " + dangerous
                );
            }
        }
    }
    
    /**
     * Tests a regex pattern against sample text with timeout protection
     */
    public Map<String, String> testPattern(String regexPattern, String text) {
        return testPattern(regexPattern, text, DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * Tests a regex pattern against sample text with specified timeout
     */
    public Map<String, String> testPattern(String regexPattern, String text, long timeoutMs) {
        validatePattern(regexPattern);
        
        Future<Map<String, String>> future = executor.submit(() -> extractFields(regexPattern, text));
        
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RegexValidationException("Regex execution timed out - possible catastrophic backtracking");
        } catch (InterruptedException | ExecutionException e) {
            throw new RegexValidationException("Error executing regex: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extracts named groups from text using the provided regex pattern
     * Java 17 compatible implementation - extracts ALL named groups from the pattern
     */
    private Map<String, String> extractFields(String regexPattern, String text) {
        Map<String, String> extractedFields = new HashMap<>();
        
        try {
            Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            
            if (matcher.find()) {
                // Extract all named group names from the regex pattern
                java.util.Set<String> namedGroups = extractNamedGroupNames(regexPattern);
                
                // Try to extract all named groups found in the pattern
                for (String groupName : namedGroups) {
                    try {
                        String value = matcher.group(groupName);
                        if (value != null && !value.isEmpty()) {
                            extractedFields.put(groupName, value.trim());
                        }
                    } catch (IllegalArgumentException e) {
                        // Named group doesn't exist or error accessing it, skip
                        log.debug("Could not extract named group '{}': {}", groupName, e.getMessage());
                    }
                }
                
                // If no named groups found, extract numbered groups
                if (extractedFields.isEmpty() && matcher.groupCount() > 0) {
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        String value = matcher.group(i);
                        if (value != null && !value.trim().isEmpty()) {
                            extractedFields.put("group" + i, value.trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting fields from text", e);
            throw new RegexValidationException("Failed to extract fields: " + e.getMessage(), e);
        }
        
        return extractedFields;
    }
    
    /**
     * Extracts all named group names from a regex pattern
     * Looks for (?<name>...) syntax
     */
    private java.util.Set<String> extractNamedGroupNames(String regexPattern) {
        java.util.Set<String> groupNames = new java.util.HashSet<>();
        
        // Pattern to match named groups: (?<name>...)
        Pattern namedGroupPattern = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");
        Matcher matcher = namedGroupPattern.matcher(regexPattern);
        
        while (matcher.find()) {
            groupNames.add(matcher.group(1));
        }
        
        log.debug("Found {} named groups in pattern: {}", groupNames.size(), groupNames);
        return groupNames;
    }
    
    /**
     * Checks if a pattern matches the given text
     */
    public boolean matches(String regexPattern, String text) {
        try {
            Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            return pattern.matcher(text).find();
        } catch (Exception e) {
            log.error("Error matching pattern", e);
            return false;
        }
    }
    
    /**
     * Calculates a match score for template selection (higher is better)
     */
    public double calculateMatchScore(String regexPattern, String text) {
        try {
            Map<String, String> fields = extractFields(regexPattern, text);
            
            // Score based on:
            // 1. Number of fields extracted
            // 2. Total length of extracted content
            // 3. Presence of key fields
            
            int fieldCount = fields.size();
            int totalLength = fields.values().stream()
                    .mapToInt(String::length)
                    .sum();
            
            double score = fieldCount * 10.0 + totalLength * 0.1;
            
            // Bonus for key financial fields
            if (fields.containsKey("amount")) score += 20;
            if (fields.containsKey("balance")) score += 15;
            if (fields.containsKey("bank")) score += 10;
            if (fields.containsKey("date")) score += 5;
            
            return score;
        } catch (Exception e) {
            return 0.0;
        }
    }
}

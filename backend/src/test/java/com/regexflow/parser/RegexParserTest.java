package com.regexflow.parser;

import com.regexflow.exception.RegexValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegexParserTest {
    
    private RegexParser regexParser;
    
    @BeforeEach
    void setUp() {
        regexParser = new RegexParser();
    }
    
    @Test
    void testValidatePattern_validPattern() {
        String pattern = "Rs\\.(?<amount>[\\d,]+)";
        assertDoesNotThrow(() -> regexParser.validatePattern(pattern));
    }
    
    @Test
    void testValidatePattern_invalidPattern() {
        String pattern = "Rs\\.(unclosed";
        assertThrows(RegexValidationException.class, () -> regexParser.validatePattern(pattern));
    }
    
    @Test
    void testValidatePattern_emptyPattern() {
        assertThrows(RegexValidationException.class, () -> regexParser.validatePattern(""));
    }
    
    @Test
    void testValidatePattern_catastrophicBacktracking() {
        String pattern = "(.*)*";
        assertThrows(RegexValidationException.class, () -> regexParser.validatePattern(pattern));
    }
    
    @Test
    void testTestPattern_simpleExtraction() {
        String pattern = "Rs\\.(?<amount>[\\d,]+)";
        String text = "Debited Rs.5000 from your account";
        
        Map<String, String> result = regexParser.testPattern(pattern, text);
        
        assertNotNull(result);
        assertTrue(result.containsKey("amount"));
        assertEquals("5000", result.get("amount"));
    }
    
    @Test
    void testTestPattern_multipleNamedGroups() {
        String pattern = "(?<type>Debited|Credited).*Rs\\.(?<amount>[\\d,]+).*Bal.*Rs\\.(?<balance>[\\d,]+)";
        String text = "Debited Rs.5,000 from A/c. Avl Bal Rs.45,000";
        
        Map<String, String> result = regexParser.testPattern(pattern, text);
        
        assertEquals("Debited", result.get("type"));
        assertEquals("5,000", result.get("amount"));
        assertEquals("45,000", result.get("balance"));
    }
    
    @Test
    void testTestPattern_noMatch() {
        String pattern = "Rs\\.(?<amount>[\\d,]+)";
        String text = "Hello World";
        
        Map<String, String> result = regexParser.testPattern(pattern, text);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testMatches_success() {
        String pattern = "Rs\\.\\d+";
        String text = "Amount Rs.5000";
        
        assertTrue(regexParser.matches(pattern, text));
    }
    
    @Test
    void testMatches_failure() {
        String pattern = "Rs\\.\\d+";
        String text = "No amount here";
        
        assertFalse(regexParser.matches(pattern, text));
    }
    
    @Test
    void testCalculateMatchScore() {
        String pattern = "(?<amount>\\d+).*(?<balance>\\d+).*(?<bank>HDFC)";
        String text = "Rs.5000 debited. Balance: 10000. HDFC Bank";
        
        double score = regexParser.calculateMatchScore(pattern, text);
        
        assertTrue(score > 0);
    }
}

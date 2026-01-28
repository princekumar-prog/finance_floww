package com.regexflow.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify the fix for custom named groups
 * This test verifies that RegexParser can extract ALL named groups,
 * not just those in a predefined list
 */
class RegexParserIntegrationTest {
    
    private RegexParser regexParser;
    
    @BeforeEach
    void setUp() {
        regexParser = new RegexParser();
    }
    
    @Test
    void testCustomNamedGroups_notInPredefinedList() {
        // This pattern uses custom field names that were NOT in the original predefined list
        String pattern = "Transaction of Rs\\.(?<transactionAmount>\\d+) at (?<storeName>[A-Z]+) on (?<transactionDate>[\\d-]+) using card ending (?<cardLastDigits>\\d+)";
        String text = "Transaction of Rs.1500 at STARBUCKS on 2024-01-20 using card ending 4567";
        
        Map<String, String> result = regexParser.testPattern(pattern, text);
        
        // Verify all custom named groups are extracted
        assertFalse(result.isEmpty(), "Result should not be empty");
        assertEquals("1500", result.get("transactionAmount"), "Should extract transactionAmount");
        assertEquals("STARBUCKS", result.get("storeName"), "Should extract storeName");
        assertEquals("2024-01-20", result.get("transactionDate"), "Should extract transactionDate");
        assertEquals("4567", result.get("cardLastDigits"), "Should extract cardLastDigits");
    }
    
    @Test
    void testStandardNamedGroups_stillWork() {
        // Standard pattern with common field names should still work
        String pattern = "Rs\\.(?<amount>[\\d,]+) credited to A/c (?<account>\\w+) on (?<date>[\\d-A-Za-z]+)";
        String text = "Rs.10,000 credited to A/c XX1234 on 16-Jan-24";
        
        Map<String, String> result = regexParser.testPattern(pattern, text);
        
        assertFalse(result.isEmpty());
        assertEquals("10,000", result.get("amount"));
        assertEquals("XX1234", result.get("account"));
        assertEquals("16-Jan-24", result.get("date"));
    }
    
    @Test
    void testMixOfStandardAndCustomGroups() {
        // Mix of standard and custom named groups
        String pattern = "(?<customPrefix>[A-Z]+)-BANK: (?<amount>[\\d,]+) (?<customAction>debited|credited) to (?<account>\\w+)";
        String text = "HDFC-BANK: 5,000 debited to XX1234";
        
        Map<String, String> result = regexParser.testPattern(pattern, text);
        
        assertFalse(result.isEmpty());
        assertEquals("HDFC", result.get("customPrefix"), "Should extract custom field");
        assertEquals("5,000", result.get("amount"), "Should extract standard field");
        assertEquals("debited", result.get("customAction"), "Should extract custom field");
        assertEquals("XX1234", result.get("account"), "Should extract standard field");
    }
    
    @Test
    void testNumberedGroupsFallback_whenNoNamedGroups() {
        // Pattern without named groups should fall back to numbered groups
        String pattern = "Amount: (\\d+), Account: (\\w+)";
        String text = "Amount: 1000, Account: XX5678";
        
        Map<String, String> result = regexParser.testPattern(pattern, text);
        
        assertFalse(result.isEmpty());
        assertEquals("1000", result.get("group1"));
        assertEquals("XX5678", result.get("group2"));
    }
    
    @Test
    void testEmptyResult_whenNoMatch() {
        String pattern = "Rs\\.(?<amount>\\d+)";
        String text = "No amount here";
        
        Map<String, String> result = regexParser.testPattern(pattern, text);
        
        assertTrue(result.isEmpty(), "Should return empty map when pattern doesn't match");
    }
    
    @Test
    void testCaseInsensitiveMatching() {
        // Verify case-insensitive matching still works
        String pattern = "(?<bank>hdfc|icici|sbi) bank";
        String text = "HDFC BANK transaction completed";
        
        Map<String, String> result = regexParser.testPattern(pattern, text);
        
        assertFalse(result.isEmpty());
        assertEquals("HDFC", result.get("bank"));
    }
    
    @Test
    void testComplexRealWorldPattern() {
        // Real-world pattern for UPI transaction
        String pattern = "(?<upiApp>[A-Z]+) UPI payment of Rs\\.(?<txnAmount>[\\d,]+\\.?\\d*) to (?<recipientName>[A-Za-z\\s]+) \\((?<upiId>[a-z0-9@\\.]+)\\) on (?<txnDate>[\\d-A-Za-z]+) (?<txnTime>[\\d:]+) successful\\. Ref: (?<refNum>\\d+)";
        String text = "PHONEPE UPI payment of Rs.2,500.50 to John Doe (john@paytm) on 20-Jan-24 14:30:45 successful. Ref: 123456789012";
        
        Map<String, String> result = regexParser.testPattern(pattern, text);
        
        assertFalse(result.isEmpty());
        assertEquals("PHONEPE", result.get("upiApp"));
        assertEquals("2,500.50", result.get("txnAmount"));
        assertEquals("John Doe", result.get("recipientName"));
        assertEquals("john@paytm", result.get("upiId"));
        assertEquals("20-Jan-24", result.get("txnDate"));
        assertEquals("14:30:45", result.get("txnTime"));
        assertEquals("123456789012", result.get("refNum"));
    }
}

package com.regexflow.parser;

import java.util.Map;

/**
 * Manual test to verify regex parsing with actual SMS samples using RegexParser
 */
public class RegexParserManualTest {
    
    public static void main(String[] args) {
        RegexParser parser = new RegexParser();
        
        System.out.println("=".repeat(80));
        System.out.println("Testing RegexParser with Bank SMS Samples");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Test case 1: HDFC Bank Debit
        String sms1 = "Your A/c XX1234 debited for Rs.5,000.00 on 15-Jan-24 at POS 423156XXXXXX9876 at AMAZON RETAIL. Avl Bal: Rs.45,230.50. Not you? Call 18002586161";
        String pattern1 = "Your A/c (?<account>\\w+) debited for Rs\\.(?<amount>[\\d,]+\\.?\\d*) on (?<date>[\\d-A-Za-z]+).*?at (?<merchant>[A-Z\\s]+)\\. Avl Bal: Rs\\.(?<balance>[\\d,]+\\.?\\d*)";
        
        System.out.println("Test 1: HDFC Bank Debit");
        System.out.println("SMS: " + sms1);
        System.out.println("Pattern: " + pattern1);
        testWithParser(parser, pattern1, sms1);
        System.out.println();
        
        // Test case 2: HDFC Bank Credit
        String sms2 = "Rs.10,000.00 credited to A/c XX1234 on 16-Jan-24 by A/c linked to VPA john@paytm (UPI Ref No 401234567890). Avl Bal Rs.55,230.50";
        String pattern2 = "Rs\\.(?<amount>[\\d,]+\\.?\\d*) credited to A/c (?<account>\\w+) on (?<date>[\\d-A-Za-z]+).*?Avl Bal Rs\\.(?<balance>[\\d,]+\\.?\\d*)";
        
        System.out.println("Test 2: HDFC Bank Credit");
        System.out.println("SMS: " + sms2);
        System.out.println("Pattern: " + pattern2);
        testWithParser(parser, pattern2, sms2);
        System.out.println();
        
        // Test case 3: SBI Debit
        String sms3 = "Dear Customer, INR 3,500.00 debited from A/c **9876 on 17-Jan-24 to VPA paytm@paytm Ref No 123456789012. If not done by you, please call 1800112211";
        String pattern3 = "INR (?<amount>[\\d,]+\\.?\\d*) debited from A/c (?<account>[*\\d]+) on (?<date>[\\d-A-Za-z]+).*?Ref No (?<ref>\\d+)";
        
        System.out.println("Test 3: SBI Debit");
        System.out.println("SMS: " + sms3);
        System.out.println("Pattern: " + pattern3);
        testWithParser(parser, pattern3, sms3);
        System.out.println();
        
        // Test case 4: Custom named groups (not in predefined list)
        String sms4 = "Transaction of Rs.1500 at STARBUCKS on 2024-01-20 using card ending 4567";
        String pattern4 = "Transaction of Rs\\.(?<transactionAmount>\\d+) at (?<storeName>[A-Z]+) on (?<transactionDate>[\\d-]+) using card ending (?<cardLastDigits>\\d+)";
        
        System.out.println("Test 4: Custom Named Groups (not in predefined list)");
        System.out.println("SMS: " + sms4);
        System.out.println("Pattern: " + pattern4);
        testWithParser(parser, pattern4, sms4);
        
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("All tests completed!");
        System.out.println("=".repeat(80));
    }
    
    private static void testWithParser(RegexParser parser, String pattern, String text) {
        try {
            Map<String, String> result = parser.testPattern(pattern, text);
            
            if (result.isEmpty()) {
                System.out.println("✗ No match found or no fields extracted");
            } else {
                System.out.println("✓ Pattern matched! Extracted fields:");
                result.forEach((key, value) -> 
                    System.out.println("  " + key + " = " + value)
                );
            }
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

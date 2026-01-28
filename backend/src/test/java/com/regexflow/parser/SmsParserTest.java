package com.regexflow.parser;

import com.regexflow.entity.RegexTemplate;
import com.regexflow.entity.User;
import com.regexflow.enums.SmsType;
import com.regexflow.enums.TemplateStatus;
import com.regexflow.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsParserTest {
    
    @Mock
    private RegexParser regexParser;
    
    @InjectMocks
    private SmsParser smsParser;
    
    private RegexTemplate template;
    private User user;
    
    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .username("maker1")
            .email("maker1@test.com")
            .fullName("Test Maker")
            .role(UserRole.MAKER)
            .build();
        
        template = RegexTemplate.builder()
            .id(1L)
            .bankName("HDFC Bank")
            .regexPattern("(?<type>Debited|Credited).*Rs\\.(?<amount>[\\d,]+).*Bal.*Rs\\.(?<balance>[\\d,]+)")
            .smsType(SmsType.DEBIT)
            .status(TemplateStatus.ACTIVE)
            .createdBy(user)
            .build();
    }
    
    @Test
    void testParseSms_success() {
        String smsText = "Debited Rs.5,000 from A/c XX1234. Avl Bal Rs.45,000";
        
        when(regexParser.testPattern(anyString(), anyString()))
            .thenReturn(Map.of(
                "type", "Debited",
                "amount", "5,000",
                "balance", "45,000"
            ));
        
        SmsParser.ParseResult result = smsParser.parseSms(smsText, template);
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus().name());
        assertNotNull(result.getAmount());
        assertNotNull(result.getBalance());
    }
    
    @Test
    void testParseSmsWithBestMatch_success() {
        String smsText = "Debited Rs.5,000 from A/c XX1234. Avl Bal Rs.45,000";
        List<RegexTemplate> templates = Arrays.asList(template);
        
        when(regexParser.matches(anyString(), anyString())).thenReturn(true);
        when(regexParser.calculateMatchScore(anyString(), anyString())).thenReturn(50.0);
        when(regexParser.testPattern(anyString(), anyString()))
            .thenReturn(Map.of(
                "type", "Debited",
                "amount", "5,000",
                "balance", "45,000"
            ));
        
        SmsParser.ParseResult result = smsParser.parseSmsWithBestMatch(smsText, templates);
        
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus().name());
    }
    
    @Test
    void testParseSmsWithBestMatch_noMatch() {
        String smsText = "Random text without transaction details";
        List<RegexTemplate> templates = Arrays.asList(template);
        
        when(regexParser.matches(anyString(), anyString())).thenReturn(false);
        
        SmsParser.ParseResult result = smsParser.parseSmsWithBestMatch(smsText, templates);
        
        assertNotNull(result);
        assertEquals("NO_MATCH", result.getStatus().name());
    }
}

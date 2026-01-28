package com.regexflow.entity;

import com.regexflow.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "parsed_transactions", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_bank_name", columnList = "bankName"),
    @Index(name = "idx_transaction_date", columnList = "transactionDate"),
    @Index(name = "idx_transaction_type", columnList = "transactionType"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sms_log_id", nullable = false)
    private RawSmsLog smsLog;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private RegexTemplate template;
    
    @Column(nullable = false, length = 100)
    private String bankName;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal balance;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType transactionType;
    
    @Column(length = 50)
    private String accountId;
    
    @Column(length = 200)
    private String merchantOrPayee;
    
    @Column(length = 100)
    private String mode;
    
    @Column
    private LocalDate transactionDate;
    
    @Column(length = 100)
    private String referenceNumber;
    
    @Column(columnDefinition = "TEXT")
    private String extractedData;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

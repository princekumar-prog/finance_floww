package com.regexflow.entity;

import com.regexflow.enums.SmsType;
import com.regexflow.enums.TemplateStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "regex_templates", indexes = {
    @Index(name = "idx_bank_name", columnList = "bankName"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_bank_sms_type", columnList = "bankName, smsType"),
    @Index(name = "idx_created_by", columnList = "createdBy")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegexTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String bankName;
    
    @Column(nullable = false, length = 1000)
    private String regexPattern;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SmsType smsType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateStatus status;
    
    @Column(length = 500)
    private String sampleSms;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @Column
    private LocalDateTime approvedAt;
    
    @Column
    private LocalDateTime deprecatedAt;
    
    @Column(length = 500)
    private String rejectionReason;
    
    @Version
    private Long version;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

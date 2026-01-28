package com.regexflow.entity;

import com.regexflow.enums.TemplateStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "regex_audit_trail", indexes = {
    @Index(name = "idx_template_id", columnList = "templateId"),
    @Index(name = "idx_performed_by", columnList = "performedBy"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegexAuditTrail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private RegexTemplate template;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateStatus previousStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateStatus newStatus;
    
    @Column(nullable = false, length = 50)
    private String action;
    
    @Column(columnDefinition = "TEXT")
    private String comments;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

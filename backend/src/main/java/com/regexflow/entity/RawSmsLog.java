package com.regexflow.entity;

import com.regexflow.enums.ParseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "raw_sms_logs", indexes = {
    @Index(name = "idx_uploaded_by", columnList = "uploadedBy"),
    @Index(name = "idx_parse_status", columnList = "parseStatus"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_sender_header", columnList = "senderHeader")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawSmsLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500, columnDefinition = "TEXT")
    private String rawSmsText;
    
    @Column(length = 50)
    private String senderHeader;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParseStatus parseStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private RegexTemplate template;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

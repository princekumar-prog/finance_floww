package com.regexflow.repository;

import com.regexflow.entity.RegexTemplate;
import com.regexflow.entity.User;
import com.regexflow.enums.SmsType;
import com.regexflow.enums.TemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegexTemplateRepository extends JpaRepository<RegexTemplate, Long> {
    
    List<RegexTemplate> findByStatus(TemplateStatus status);
    
    List<RegexTemplate> findByBankName(String bankName);
    
    List<RegexTemplate> findByBankNameAndSmsType(String bankName, SmsType smsType);
    
    List<RegexTemplate> findByBankNameAndSmsTypeAndStatus(
        String bankName, SmsType smsType, TemplateStatus status);
    
    List<RegexTemplate> findByCreatedBy(User user);
    
    @Query("SELECT t FROM RegexTemplate t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<RegexTemplate> findPendingTemplates(@Param("status") TemplateStatus status);
    
    @Query("SELECT t FROM RegexTemplate t WHERE t.status = 'ACTIVE' " +
           "ORDER BY t.updatedAt DESC")
    List<RegexTemplate> findAllActiveTemplates();
    
    @Query("SELECT t FROM RegexTemplate t WHERE t.bankName = :bankName " +
           "AND t.status = 'ACTIVE' ORDER BY t.updatedAt DESC")
    List<RegexTemplate> findActiveTemplatesByBank(@Param("bankName") String bankName);
    
    @Query("SELECT t FROM RegexTemplate t WHERE t.approvedBy = :checker " +
           "AND (t.status = 'ACTIVE' OR t.status = 'REJECTED' OR t.status = 'DEPRECATED') " +
           "ORDER BY t.updatedAt DESC")
    List<RegexTemplate> findReviewedTemplatesByChecker(@Param("checker") User checker);
    
    @Query("SELECT t FROM RegexTemplate t WHERE " +
           "(t.status = 'ACTIVE' OR t.status = 'REJECTED' OR t.status = 'DEPRECATED') " +
           "ORDER BY t.updatedAt DESC")
    List<RegexTemplate> findAllReviewedTemplates();
    
    @Query("SELECT t FROM RegexTemplate t WHERE " +
           "t.regexPattern = :regexPattern " +
           "AND (t.status = 'PENDING_APPROVAL' OR t.status = 'ACTIVE') " +
           "AND (:excludeId IS NULL OR t.id != :excludeId) " +
           "ORDER BY t.updatedAt DESC")
    List<RegexTemplate> findDuplicateTemplates(
        @Param("regexPattern") String regexPattern, 
        @Param("excludeId") Long excludeId);
}

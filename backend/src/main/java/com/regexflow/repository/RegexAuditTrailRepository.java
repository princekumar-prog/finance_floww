package com.regexflow.repository;

import com.regexflow.entity.RegexAuditTrail;
import com.regexflow.entity.RegexTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegexAuditTrailRepository extends JpaRepository<RegexAuditTrail, Long> {
    
    List<RegexAuditTrail> findByTemplateOrderByCreatedAtDesc(RegexTemplate template);
    
    List<RegexAuditTrail> findByTemplate_IdOrderByCreatedAtDesc(Long templateId);
}

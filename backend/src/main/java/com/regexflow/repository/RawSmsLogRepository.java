package com.regexflow.repository;

import com.regexflow.entity.RawSmsLog;
import com.regexflow.entity.User;
import com.regexflow.enums.ParseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RawSmsLogRepository extends JpaRepository<RawSmsLog, Long> {
    
    List<RawSmsLog> findByUploadedBy(User user);
    
    Page<RawSmsLog> findByUploadedBy(User user, Pageable pageable);
    
    List<RawSmsLog> findByParseStatus(ParseStatus status);
    
    List<RawSmsLog> findByUploadedByAndParseStatus(User user, ParseStatus status);
    
    Page<RawSmsLog> findByUploadedByOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Check if a raw SMS with the exact same text already exists for a user
     */
    Optional<RawSmsLog> findByUploadedByAndRawSmsText(User user, String rawSmsText);
    
    /**
     * Check if a raw SMS with the exact same text already exists (for any user)
     */
    boolean existsByRawSmsText(String rawSmsText);
}

package com.regexflow.repository;

import com.regexflow.entity.ParsedTransaction;
import com.regexflow.entity.RawSmsLog;
import com.regexflow.entity.User;
import com.regexflow.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParsedTransactionRepository extends JpaRepository<ParsedTransaction, Long> {
    
    List<ParsedTransaction> findByUser(User user);
    
    Page<ParsedTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<ParsedTransaction> findByUserAndBankName(User user, String bankName);
    
    List<ParsedTransaction> findByUserAndTransactionType(User user, TransactionType type);
    
    Optional<ParsedTransaction> findBySmsLog(RawSmsLog smsLog);
    
    @Query("SELECT t FROM ParsedTransaction t WHERE t.user = :user " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<ParsedTransaction> findByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT t FROM ParsedTransaction t WHERE t.user = :user " +
           "AND (:bankName IS NULL OR t.bankName = :bankName) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) " +
           "ORDER BY t.createdAt DESC")
    Page<ParsedTransaction> findByFilters(
        @Param("user") User user,
        @Param("bankName") String bankName,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        Pageable pageable
    );
}

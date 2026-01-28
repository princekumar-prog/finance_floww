-- RegexFlow Database Schema
-- MySQL Database Schema for Fintech SMS-to-Ledger Engine

-- Drop tables if they exist (for clean reinstall)
DROP TABLE IF EXISTS parsed_transactions;
DROP TABLE IF EXISTS raw_sms_logs;
DROP TABLE IF EXISTS regex_audit_trail;
DROP TABLE IF EXISTS regex_templates;
DROP TABLE IF EXISTS users;

-- ============================================
-- Users Table
-- ============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_username (username),
    CONSTRAINT chk_users_role CHECK (role IN ('MAKER', 'CHECKER', 'NORMAL_USER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Regex Templates Table
-- ============================================
CREATE TABLE regex_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_name VARCHAR(100) NOT NULL,
    regex_pattern VARCHAR(1000) NOT NULL,
    sms_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sample_sms VARCHAR(500),
    description TEXT,
    created_by BIGINT NOT NULL,
    approved_by BIGINT,
    approved_at TIMESTAMP NULL,
    deprecated_at TIMESTAMP NULL,
    rejection_reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_regex_templates_bank_name (bank_name),
    INDEX idx_regex_templates_status (status),
    INDEX idx_regex_templates_bank_sms_type (bank_name, sms_type),
    INDEX idx_regex_templates_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT chk_templates_sms_type CHECK (sms_type IN ('DEBIT', 'CREDIT', 'BILL')),
    CONSTRAINT chk_templates_status CHECK (status IN ('DRAFT', 'PENDING_APPROVAL', 'ACTIVE', 'REJECTED', 'DEPRECATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Regex Audit Trail Table
-- ============================================
CREATE TABLE regex_audit_trail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL,
    comments TEXT,
    performed_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_trail_template_id (template_id),
    INDEX idx_audit_trail_performed_by (performed_by),
    INDEX idx_audit_trail_created_at (created_at),
    FOREIGN KEY (template_id) REFERENCES regex_templates(id),
    FOREIGN KEY (performed_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Raw SMS Logs Table
-- ============================================
CREATE TABLE raw_sms_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    raw_sms_text TEXT NOT NULL,
    sender_header VARCHAR(50),
    parse_status VARCHAR(20) NOT NULL,
    template_id BIGINT,
    uploaded_by BIGINT NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_raw_sms_logs_uploaded_by (uploaded_by),
    INDEX idx_raw_sms_logs_parse_status (parse_status),
    INDEX idx_raw_sms_logs_created_at (created_at),
    INDEX idx_raw_sms_logs_sender_header (sender_header),
    INDEX idx_raw_sms_logs_duplicate_check (uploaded_by, raw_sms_text(500)),
    FOREIGN KEY (template_id) REFERENCES regex_templates(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    CONSTRAINT chk_sms_logs_parse_status CHECK (parse_status IN ('SUCCESS', 'PARTIAL', 'FAILED', 'NO_MATCH', 'ERROR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Parsed Transactions Table
-- ============================================
CREATE TABLE parsed_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sms_log_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    template_id BIGINT,
    bank_name VARCHAR(100) NOT NULL,
    amount DECIMAL(19, 2),
    balance DECIMAL(19, 2),
    transaction_type VARCHAR(30) NOT NULL,
    account_id VARCHAR(50),
    merchant_or_payee VARCHAR(200),
    mode VARCHAR(100),
    transaction_date DATE,
    reference_number VARCHAR(100),
    extracted_data TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_parsed_transactions_user_id (user_id),
    INDEX idx_parsed_transactions_bank_name (bank_name),
    INDEX idx_parsed_transactions_transaction_date (transaction_date),
    INDEX idx_parsed_transactions_transaction_type (transaction_type),
    INDEX idx_parsed_transactions_created_at (created_at),
    FOREIGN KEY (sms_log_id) REFERENCES raw_sms_logs(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (template_id) REFERENCES regex_templates(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Sample Data Insertion
-- ============================================

-- Note: Passwords should be BCrypt hashed by the application
-- The examples below are placeholders - actual hashes will be generated on registration

-- Example of how to insert users (uncomment and update with actual BCrypt hashes):
-- INSERT INTO users (username, email, password, full_name, role) VALUES
-- ('maker1', 'maker1@regexflow.com', '$2a$10$...', 'John Maker', 'MAKER'),
-- ('checker1', 'checker1@regexflow.com', '$2a$10$...', 'Jane Checker', 'CHECKER'),
-- ('user1', 'user1@regexflow.com', '$2a$10$...', 'Bob User', 'NORMAL_USER');

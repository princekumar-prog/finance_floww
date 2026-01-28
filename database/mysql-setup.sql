-- MySQL Database Setup for RegexFlow
-- Run this file as MySQL root user

-- Create database
CREATE DATABASE IF NOT EXISTS regexflow 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER IF NOT EXISTS 'regexflow_user'@'localhost' 
    IDENTIFIED BY 'regexflow_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON regexflow.* TO 'regexflow_user'@'localhost';

-- Apply privileges
FLUSH PRIVILEGES;

-- Display success message
SELECT 'Database regexflow created successfully!' AS Status;
SELECT 'User regexflow_user created with all privileges!' AS Status;
SELECT 'Now run: mysql -u regexflow_user -p regexflow < schema.sql' AS NextStep;

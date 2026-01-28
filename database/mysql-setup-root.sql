-- MySQL Database Setup for RegexFlow (Using Root User)
-- Run this file as MySQL root user

-- Create database
CREATE DATABASE IF NOT EXISTS regexflow 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Display success message
SELECT 'Database regexflow created successfully!' AS Status;
SELECT 'Using root user for connection' AS Note;
SELECT 'Update application.yml with your MySQL root password' AS Important;
SELECT 'Now run: mysql -u root -p regexflow < schema.sql' AS NextStep;

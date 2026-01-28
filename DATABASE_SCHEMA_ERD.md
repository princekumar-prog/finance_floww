# RegexFlow Database Schema - Entity Relationship Diagram

## Overview
This document provides a comprehensive view of the RegexFlow database schema for the Fintech SMS-to-Ledger Engine.

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         REGEXFLOW DATABASE SCHEMA                        │
│                      (Fintech SMS-to-Ledger Engine)                     │
└─────────────────────────────────────────────────────────────────────────┘


┌──────────────────────────────────┐
│          USERS                   │
├──────────────────────────────────┤
│ PK  id                BIGINT     │
│     username          VARCHAR    │
│     email             VARCHAR    │
│     password          VARCHAR    │
│     full_name         VARCHAR    │
│     role              ENUM       │ ◄── MAKER, CHECKER, NORMAL_USER
│     active            BOOLEAN    │
│     created_at        TIMESTAMP  │
│     updated_at        TIMESTAMP  │
└──────────────────────┬───────────┘
                       │
         ┌─────────────┼─────────────────────────┐
         │             │                         │
         │             │                         │
         ▼             ▼                         ▼
┌────────────────────────────────┐    ┌──────────────────────────────────┐
│    REGEX_TEMPLATES             │    │     RAW_SMS_LOGS                 │
├────────────────────────────────┤    ├──────────────────────────────────┤
│ PK  id              BIGINT     │    │ PK  id                BIGINT     │
│     bank_name       VARCHAR    │    │     raw_sms_text      TEXT       │
│     regex_pattern   VARCHAR    │    │     sender_header     VARCHAR    │
│     sms_type        ENUM       │◄───┼─FK  template_id       BIGINT     │
│     status          ENUM       │    │     parse_status      ENUM       │
│     sample_sms      VARCHAR    │    │ FK  uploaded_by       BIGINT     │───┐
│     description     TEXT       │    │     error_message     TEXT       │   │
│ FK  created_by      BIGINT     │───┐│     created_at        TIMESTAMP  │   │
│ FK  approved_by     BIGINT     │   │└──────────────┬───────────────────┘   │
│     approved_at     TIMESTAMP  │   │               │                       │
│     deprecated_at   TIMESTAMP  │   │               │                       │
│     rejection_reason VARCHAR   │   │               │                       │
│     version         BIGINT     │   │               │                       │
│     created_at      TIMESTAMP  │   │               ▼                       │
│     updated_at      TIMESTAMP  │   │    ┌──────────────────────────────┐  │
└────────────────┬───────────────┘   │    │  PARSED_TRANSACTIONS         │  │
                 │                   │    ├──────────────────────────────┤  │
                 │                   │    │ PK  id             BIGINT    │  │
                 ▼                   │    │ FK  sms_log_id     BIGINT    │◄─┘
┌──────────────────────────────┐    │    │ FK  user_id        BIGINT    │◄───┐
│   REGEX_AUDIT_TRAIL          │    │    │ FK  template_id    BIGINT    │◄─┐ │
├──────────────────────────────┤    │    │     bank_name      VARCHAR   │  │ │
│ PK  id           BIGINT      │    │    │     amount         DECIMAL   │  │ │
│ FK  template_id  BIGINT      │◄───┘    │     balance        DECIMAL   │  │ │
│     previous_status ENUM     │         │     transaction_type ENUM    │  │ │
│     new_status   ENUM        │         │     account_id     VARCHAR   │  │ │
│     action       VARCHAR     │         │     merchant_or_payee VARCHAR│  │ │
│     comments     TEXT        │         │     mode           VARCHAR   │  │ │
│ FK  performed_by BIGINT      │─────────│     transaction_date DATE    │  │ │
│     created_at   TIMESTAMP   │         │     reference_number VARCHAR │  │ │
└──────────────────────────────┘         │     extracted_data TEXT      │  │ │
                                         │     created_at     TIMESTAMP │  │ │
                                         └──────────────────────────────┘  │ │
                                                                            │ │
                                         ┌──────────────────────────────────┘ │
                                         └────────────────────────────────────┘
```

## Tables

### 1. USERS
**Purpose**: Central authentication and authorization table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique user identifier |
| username | VARCHAR(100) | NOT NULL, UNIQUE | Username for login |
| email | VARCHAR(100) | NOT NULL, UNIQUE | User email address |
| password | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| full_name | VARCHAR(100) | NOT NULL | User's full name |
| role | ENUM | NOT NULL | User role (MAKER, CHECKER, NORMAL_USER) |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE | Account status |
| created_at | TIMESTAMP | NOT NULL | Account creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |

**Indexes**:
- `idx_users_email` on `email`
- `idx_users_username` on `username`

---

### 2. REGEX_TEMPLATES
**Purpose**: Stores regex patterns for SMS parsing with approval workflow

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique template identifier |
| bank_name | VARCHAR(100) | NOT NULL | Bank name (e.g., HDFC, SBI) |
| regex_pattern | VARCHAR(1000) | NOT NULL | Regex pattern with named groups |
| sms_type | ENUM | NOT NULL | SMS type (DEBIT, CREDIT, BILL) |
| status | ENUM | NOT NULL | Template status (DRAFT, PENDING_APPROVAL, ACTIVE, REJECTED, DEPRECATED) |
| sample_sms | VARCHAR(500) | NULLABLE | Sample SMS for testing |
| description | TEXT | NULLABLE | Template description |
| created_by | BIGINT | FK → users(id) | Maker who created the template |
| approved_by | BIGINT | FK → users(id), NULLABLE | Checker who approved/rejected |
| approved_at | TIMESTAMP | NULLABLE | Approval timestamp |
| deprecated_at | TIMESTAMP | NULLABLE | Deprecation timestamp |
| rejection_reason | VARCHAR(500) | NULLABLE | Reason for rejection |
| version | BIGINT | NOT NULL, DEFAULT 0 | Optimistic locking version |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |

**Indexes**:
- `idx_regex_templates_bank_name` on `bank_name`
- `idx_regex_templates_status` on `status`
- `idx_regex_templates_bank_sms_type` on `(bank_name, sms_type)` (composite)
- `idx_regex_templates_created_by` on `created_by`

---

### 3. REGEX_AUDIT_TRAIL
**Purpose**: Immutable audit log for template changes

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique audit record identifier |
| template_id | BIGINT | FK → regex_templates(id) | Template being audited |
| previous_status | ENUM | NOT NULL | Status before change |
| new_status | ENUM | NOT NULL | Status after change |
| action | VARCHAR(50) | NOT NULL | Action performed (CREATED, SUBMITTED, APPROVED, etc.) |
| comments | TEXT | NULLABLE | Comments or reason for action |
| performed_by | BIGINT | FK → users(id) | User who performed the action |
| created_at | TIMESTAMP | NOT NULL | Action timestamp |

**Indexes**:
- `idx_audit_trail_template_id` on `template_id`
- `idx_audit_trail_performed_by` on `performed_by`
- `idx_audit_trail_created_at` on `created_at`

---

### 4. RAW_SMS_LOGS
**Purpose**: Stores raw SMS messages before parsing

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique SMS log identifier |
| raw_sms_text | TEXT | NOT NULL | Original SMS text |
| sender_header | VARCHAR(50) | NULLABLE | SMS sender ID (e.g., HDFCBK) |
| parse_status | ENUM | NOT NULL | Parse status (SUCCESS, PARTIAL, FAILED, NO_MATCH, ERROR) |
| template_id | BIGINT | FK → regex_templates(id), NULLABLE | Template used for parsing |
| uploaded_by | BIGINT | FK → users(id) | User who uploaded the SMS |
| error_message | TEXT | NULLABLE | Error details if parsing failed |
| created_at | TIMESTAMP | NOT NULL | Upload timestamp |

**Indexes**:
- `idx_raw_sms_logs_uploaded_by` on `uploaded_by`
- `idx_raw_sms_logs_parse_status` on `parse_status`
- `idx_raw_sms_logs_created_at` on `created_at`
- `idx_raw_sms_logs_sender_header` on `sender_header`

---

### 5. PARSED_TRANSACTIONS
**Purpose**: Stores extracted transaction data from parsed SMS

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique transaction identifier |
| sms_log_id | BIGINT | FK → raw_sms_logs(id) | Source SMS log |
| user_id | BIGINT | FK → users(id) | Transaction owner |
| template_id | BIGINT | FK → regex_templates(id), NULLABLE | Template used for parsing |
| bank_name | VARCHAR(100) | NOT NULL | Bank name |
| amount | DECIMAL(19,2) | NULLABLE | Transaction amount |
| balance | DECIMAL(19,2) | NULLABLE | Account balance after transaction |
| transaction_type | ENUM | NOT NULL | Transaction type (DEBIT, CREDIT, BILL_PAYMENT) |
| account_id | VARCHAR(50) | NULLABLE | Masked account number |
| merchant_or_payee | VARCHAR(200) | NULLABLE | Merchant or payee name |
| mode | VARCHAR(100) | NULLABLE | Payment mode (UPI, NEFT, Card, etc.) |
| transaction_date | DATE | NULLABLE | Transaction date |
| reference_number | VARCHAR(100) | NULLABLE | Transaction reference number |
| extracted_data | TEXT | NULLABLE | Full JSON of extracted fields |
| created_at | TIMESTAMP | NOT NULL | Record creation timestamp |

**Indexes**:
- `idx_parsed_transactions_user_id` on `user_id`
- `idx_parsed_transactions_bank_name` on `bank_name`
- `idx_parsed_transactions_transaction_date` on `transaction_date`
- `idx_parsed_transactions_transaction_type` on `transaction_type`
- `idx_parsed_transactions_created_at` on `created_at`

---

## Relationships

### Foreign Key Relationships

| From Table | Column | References | Relationship | Description |
|------------|--------|------------|--------------|-------------|
| regex_templates | created_by | users(id) | Many-to-One | Maker who created template |
| regex_templates | approved_by | users(id) | Many-to-One | Checker who approved/rejected |
| regex_audit_trail | template_id | regex_templates(id) | Many-to-One | Template being audited |
| regex_audit_trail | performed_by | users(id) | Many-to-One | User who performed action |
| raw_sms_logs | template_id | regex_templates(id) | Many-to-One | Template used for parsing |
| raw_sms_logs | uploaded_by | users(id) | Many-to-One | User who uploaded SMS |
| parsed_transactions | sms_log_id | raw_sms_logs(id) | One-to-One | Source SMS log |
| parsed_transactions | user_id | users(id) | Many-to-One | Transaction owner |
| parsed_transactions | template_id | regex_templates(id) | Many-to-One | Template used for extraction |

---

## Enumerations

### UserRole
- `MAKER` - Can create and edit templates
- `CHECKER` - Can approve or reject templates
- `NORMAL_USER` - Can upload SMS for parsing

### TemplateStatus
- `DRAFT` - Template created, not yet submitted
- `PENDING_APPROVAL` - Submitted, waiting for checker review
- `ACTIVE` - Approved and active for use
- `REJECTED` - Rejected by checker
- `DEPRECATED` - Previously active, now obsolete

### SmsType
- `DEBIT` - Debit transaction SMS
- `CREDIT` - Credit transaction SMS
- `BILL` - Bill payment SMS

### ParseStatus
- `SUCCESS` - SMS parsed successfully
- `PARTIAL` - Some fields extracted
- `FAILED` - Parsing failed
- `NO_MATCH` - No matching template found
- `ERROR` - System error during parsing

### TransactionType
- `DEBIT` - Money debited from account
- `CREDIT` - Money credited to account
- `BILL_PAYMENT` - Bill payment transaction

---

## Data Flow Diagrams

### 1. Template Creation & Approval Workflow

```
┌────────┐        ┌──────────────────┐        ┌─────────┐
│ MAKER  │───────>│ REGEX_TEMPLATES  │◄───────│ CHECKER │
│ (User) │ create │   (DRAFT)        │ approve│ (User)  │
└────────┘        └────────┬─────────┘        └─────────┘
                           │
                           ▼
                  ┌──────────────────┐
                  │ REGEX_AUDIT_TRAIL│  ◄── Tracks all changes
                  └──────────────────┘

Flow:
1. Maker creates template (status: DRAFT)
2. Maker submits for approval (status: PENDING_APPROVAL)
3. Checker reviews and approves/rejects (status: ACTIVE/REJECTED)
4. All changes logged in audit trail
```

### 2. SMS Parsing Workflow

```
┌──────────────┐        ┌──────────────┐        ┌────────────────────┐
│ NORMAL_USER  │───────>│ RAW_SMS_LOGS │───────>│ PARSED_TRANSACTIONS│
│   uploads    │ create │   (stored)   │ parse  │   (extracted)      │
└──────────────┘        └──────┬───────┘        └────────────────────┘
                               │                         ▲
                               │                         │
                        ┌──────▼─────────┐               │
                        │ REGEX_TEMPLATES│───────────────┘
                        │   (ACTIVE)     │  used for parsing
                        └────────────────┘

Flow:
1. User uploads raw SMS (stored in raw_sms_logs)
2. System finds matching ACTIVE template based on bank/sender
3. Regex pattern extracts fields (stored in parsed_transactions)
4. Parse status tracked: SUCCESS, PARTIAL, FAILED, etc.
```

---

## Business Logic & Constraints

### Maker-Checker Pattern
- **MAKERs**: Create and edit templates (DRAFT → PENDING_APPROVAL)
- **CHECKERs**: Approve or reject templates (PENDING_APPROVAL → ACTIVE/REJECTED)
- **NORMAL_USERs**: Only upload SMS for parsing

### Template Lifecycle
```
DRAFT → PENDING_APPROVAL → ACTIVE/REJECTED → DEPRECATED
└────────────────────────────────────────────┬──────────┘
             (All tracked in REGEX_AUDIT_TRAIL)
```

### SMS Processing
1. User uploads raw SMS → stored in `raw_sms_logs`
2. System finds matching ACTIVE template by bank_name/sms_type
3. Regex pattern extracts named groups
4. Extracted data stored in `parsed_transactions`
5. Parse status tracked for monitoring

### Audit Trail
- Every template status change is logged
- Includes who performed action, when, and why (comments)
- Immutable audit log for compliance
- Cannot be deleted or modified

### Optimistic Locking
- `regex_templates` uses `version` column
- Prevents concurrent modification conflicts
- Version incremented on each update

---

## Performance Considerations

### Indexing Strategy
- **Composite Index**: `(bank_name, sms_type)` for fast template lookup during SMS parsing
- **Timestamp Indexes**: For date-range queries and reporting
- **Foreign Key Indexes**: For efficient JOIN operations
- **Status Indexes**: For filtering by workflow state

### Query Patterns
- Template lookup: Uses composite index on `(bank_name, sms_type, status='ACTIVE')`
- User transactions: Uses index on `user_id` and `transaction_date`
- Audit history: Uses index on `template_id` and `created_at`

### Data Retention
- Raw SMS logs retained for compliance/debugging
- Parsed transactions retained indefinitely for financial records
- Audit trail is immutable and permanent

---

## Security Considerations

### Authentication & Authorization
- Passwords stored as BCrypt hashes (never plain text)
- Role-based access control (RBAC) via `UserRole` enum
- JWT tokens for stateless authentication

### Data Privacy
- Account numbers masked in SMS (e.g., XX1234, **9876)
- Full account details never stored
- User data segregated by `user_id`

### Audit Compliance
- Complete audit trail for regulatory compliance
- All template changes tracked with user and timestamp
- Immutable logs prevent tampering

---

## Future Enhancements

### Potential Schema Extensions
1. **SMS Categories**: Add category classification (Shopping, Bills, Salary, etc.)
2. **User Preferences**: Store parsing preferences per user
3. **Template Versioning**: Track multiple versions of same template
4. **Batch Processing**: Support bulk SMS uploads
5. **Analytics Tables**: Pre-aggregated data for dashboards
6. **Notification Settings**: User notification preferences

### Performance Optimizations
1. **Partitioning**: Partition `parsed_transactions` by date
2. **Archival**: Move old SMS logs to archival tables
3. **Caching**: Cache active templates in Redis
4. **Read Replicas**: Separate read/write database instances

---

## Database Configuration

### Engine & Charset
```sql
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
```
- **InnoDB**: ACID compliance, foreign key support, row-level locking
- **utf8mb4**: Full Unicode support including emojis
- **unicode_ci**: Case-insensitive collation

### Timestamps
- All tables use `TIMESTAMP` with `DEFAULT CURRENT_TIMESTAMP`
- `updated_at` columns use `ON UPDATE CURRENT_TIMESTAMP`
- JPA auditing (`@CreatedDate`, `@LastModifiedDate`) in entities

---

## Related Documentation
- [Project Summary](PROJECT_SUMMARY.md)
- [Quick Start Guide](QUICKSTART.md)
- [MySQL Setup](MYSQL_SETUP.md)
- [Sample SMS Data](sample-data/bank-sms-samples.json)

---

**Last Updated**: January 24, 2026  
**Database Version**: MySQL 8.0+  
**Project**: RegexFlow - Fintech SMS-to-Ledger Engine

# PostgreSQL to MySQL Migration Notes

## What Changed

RegexFlow has been updated to use **MySQL 8.0+** instead of PostgreSQL.

### Files Modified

1. **backend/pom.xml**
   - Removed PostgreSQL driver dependency
   - Updated to use `mysql-connector-j` version 8.2.0

2. **backend/src/main/resources/application.yml**
   - Changed JDBC URL from PostgreSQL to MySQL format
   - Updated driver class to `com.mysql.cj.jdbc.Driver`
   - Changed Hibernate dialect to `MySQLDialect`
   - Updated default port from 5432 to 3306

3. **database/schema.sql**
   - Converted PostgreSQL syntax to MySQL syntax:
     - `SERIAL` → `AUTO_INCREMENT`
     - `BIGSERIAL` → `BIGINT AUTO_INCREMENT`
     - `VARCHAR(n)` remains the same
     - Added `ENGINE=InnoDB` for all tables
     - Added `CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci`
     - Changed `CHECK` constraints to inline constraints
     - Modified `TIMESTAMP` defaults for MySQL compatibility

4. **Documentation Files**
   - README.md - Updated all PostgreSQL references to MySQL
   - QUICKSTART.md - Updated setup instructions for MySQL
   - scripts/setup.sh - Updated database creation commands
   - PROJECT_SUMMARY.md - Updated database references

### Files Added

1. **database/mysql-setup.sql**
   - Quick script to create database and user
   - Can be run as: `mysql -u root -p < database/mysql-setup.sql`

2. **MYSQL_SETUP.md**
   - Comprehensive MySQL setup guide
   - Troubleshooting section
   - Production considerations
   - Backup strategies

## Database Schema Changes

### Table Creation Syntax

**Before (PostgreSQL):**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**After (MySQL):**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Key Differences

| Feature | PostgreSQL | MySQL |
|---------|-----------|--------|
| Auto-increment | SERIAL/BIGSERIAL | AUTO_INCREMENT |
| Sequences | CREATE SEQUENCE | Built into AUTO_INCREMENT |
| Boolean | BOOLEAN | BOOLEAN (stored as TINYINT(1)) |
| Text | TEXT | TEXT |
| Indexes | CREATE INDEX separately | Inline in CREATE TABLE |
| Check Constraints | CONSTRAINT CHECK | Inline CHECK (MySQL 8.0+) |
| Character Set | UTF8 default | Explicit utf8mb4 |
| Storage Engine | N/A | InnoDB |

## Connection Configuration

### PostgreSQL (Old)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/regexflow
    username: regexflow_user
    password: regexflow_password
    driver-class-name: org.postgresql.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### MySQL (New)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/regexflow?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: regexflow_user
    password: regexflow_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

## Application Code Changes

### No Java Code Changes Required!

Thanks to Hibernate/JPA abstraction, **no changes** were needed in:
- Entity classes
- Repository interfaces
- Service classes
- Controllers
- DTOs
- Parsers

The same Java code works with both PostgreSQL and MySQL!

## Migration Steps (If Migrating Existing Data)

If you have existing PostgreSQL data to migrate:

### 1. Export from PostgreSQL
```bash
pg_dump -U regexflow_user -d regexflow --data-only --column-inserts > postgres_data.sql
```

### 2. Create MySQL Database
```bash
mysql -u root -p < database/mysql-setup.sql
mysql -u regexflow_user -p regexflow < database/schema.sql
```

### 3. Convert SQL Syntax

Create a conversion script (`convert.sh`):
```bash
#!/bin/bash
# Convert PostgreSQL dump to MySQL format

sed -i '' 's/public\.//g' postgres_data.sql
sed -i '' 's/true/1/g' postgres_data.sql
sed -i '' 's/false/0/g' postgres_data.sql
sed -i '' 's/::timestamp//g' postgres_data.sql
sed -i '' 's/::date//g' postgres_data.sql
```

### 4. Import to MySQL
```bash
mysql -u regexflow_user -p regexflow < postgres_data.sql
```

## Testing After Migration

### 1. Verify Connection
```bash
cd backend
mvn spring-boot:run

# Look for:
# "HikariPool-1 - Start completed"
# "Started RegexFlowApplication"
```

### 2. Test Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@test.com",
    "password": "test123",
    "fullName": "Test User",
    "role": "MAKER"
  }'
```

### 3. Run All Tests
```bash
cd backend
mvn clean test

# Verify all tests pass
```

### 4. Check Data Integrity
```sql
-- Login to MySQL
mysql -u regexflow_user -p regexflow

-- Verify tables
SHOW TABLES;

-- Check counts
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM regex_templates;

-- Verify indexes
SHOW INDEX FROM users;
SHOW INDEX FROM regex_templates;
```

## Performance Comparison

Both databases perform well for RegexFlow use case:

| Operation | PostgreSQL | MySQL |
|-----------|-----------|-------|
| Connection Pool | ✓ Supported | ✓ Supported |
| Transactions | ✓ Full ACID | ✓ Full ACID (InnoDB) |
| Indexes | ✓ B-tree | ✓ B-tree |
| Text Search | ✓ Strong | ✓ Full-text |
| JSON Support | ✓ Native | ✓ Native (5.7+) |
| Concurrent Writes | ✓ MVCC | ✓ Row-level locking |

For RegexFlow's workload (10,000 SMS batch), both perform similarly.

## Rollback to PostgreSQL

If you need to switch back to PostgreSQL:

1. Change `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. Update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/regexflow
    driver-class-name: org.postgresql.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

3. Use PostgreSQL schema (available in git history)

4. Rebuild: `mvn clean install`

## Support

For MySQL-specific issues, see [MYSQL_SETUP.md](MYSQL_SETUP.md)

For general setup, see [QUICKSTART.md](QUICKSTART.md)

---

**Migration to MySQL complete! All features work identically.** ✅

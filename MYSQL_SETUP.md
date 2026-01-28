# RegexFlow - MySQL Setup Guide

This guide walks you through setting up MySQL for RegexFlow.

## Prerequisites

- MySQL 8.0 or higher installed
- MySQL server running

## Quick Setup (5 minutes)

### Step 1: Verify MySQL Installation

```bash
# Check MySQL version
mysql --version

# Should show: mysql Ver 8.0.x or higher

# Check if MySQL is running
# macOS:
brew services list | grep mysql

# Linux:
sudo systemctl status mysql

# Windows:
# Check Services app for MySQL service
```

### Step 2: Start MySQL (if not running)

```bash
# macOS (Homebrew):
brew services start mysql

# Linux:
sudo systemctl start mysql

# Windows:
# Start MySQL service from Services app
# Or run: net start MySQL80
```

### Step 3: Create Database and User

**Option A: Using provided script (Recommended)**

```bash
cd regexflow_project
mysql -u root -p < database/mysql-setup.sql
```

**Option B: Manual setup**

```bash
# Login to MySQL as root
mysql -u root -p

# Enter your root password when prompted
```

Then run these commands in MySQL:

```sql
-- Create database with UTF-8 support
CREATE DATABASE regexflow 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'regexflow_user'@'localhost' 
    IDENTIFIED BY 'regexflow_password';

-- Grant all privileges on regexflow database
GRANT ALL PRIVILEGES ON regexflow.* TO 'regexflow_user'@'localhost';

-- Apply privileges
FLUSH PRIVILEGES;

-- Verify database was created
SHOW DATABASES;

-- Exit MySQL
exit;
```

### Step 4: Import Database Schema

```bash
# Import the schema
mysql -u regexflow_user -p regexflow < database/schema.sql

# When prompted, enter password: regexflow_password
```

### Step 5: Verify Setup

```bash
# Login to verify
mysql -u regexflow_user -p regexflow

# Check tables were created
SHOW TABLES;

# Should show:
# +--------------------+
# | Tables_in_regexflow|
# +--------------------+
# | parsed_transactions|
# | raw_sms_logs       |
# | regex_audit_trail  |
# | regex_templates    |
# | users              |
# +--------------------+

# Check table structure
DESCRIBE users;

# Exit
exit;
```

## Configuration

The backend is configured in `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/regexflow?useSSL=false&serverTimezone=UTC
    username: regexflow_user
    password: regexflow_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Customize Database Credentials

If you want to use different credentials:

1. **Update MySQL user/password:**
```sql
CREATE USER 'your_username'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON regexflow.* TO 'your_username'@'localhost';
FLUSH PRIVILEGES;
```

2. **Update application.yml:**
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

## Troubleshooting

### Error: "Access denied for user"

```bash
# Reset MySQL root password (if needed)
# macOS/Linux:
sudo mysql_secure_installation

# Or manually:
mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
exit;
```

### Error: "Unknown database 'regexflow'"

```bash
# Database wasn't created. Run:
mysql -u root -p
CREATE DATABASE regexflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

### Error: "Table doesn't exist"

```bash
# Schema wasn't imported. Run:
mysql -u regexflow_user -p regexflow < database/schema.sql
```

### Connection refused / Can't connect

```bash
# Check if MySQL is running
# macOS:
brew services list

# Linux:
sudo systemctl status mysql

# Check MySQL port (should be 3306)
mysql -u root -p -e "SHOW VARIABLES LIKE 'port';"
```

### Error: "Public Key Retrieval is not allowed"

Update your connection URL in application.yml:
```yaml
url: jdbc:mysql://localhost:3306/regexflow?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

## Production Considerations

### Security

1. **Strong password:**
```sql
ALTER USER 'regexflow_user'@'localhost' 
IDENTIFIED BY 'StrongP@ssw0rd!2024';
```

2. **Limit privileges:**
```sql
-- For production, don't use ALL PRIVILEGES
GRANT SELECT, INSERT, UPDATE, DELETE ON regexflow.* TO 'regexflow_user'@'localhost';
```

3. **Remote access (if needed):**
```sql
-- Allow from specific IP
CREATE USER 'regexflow_user'@'192.168.1.100' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON regexflow.* TO 'regexflow_user'@'192.168.1.100';
```

### Performance

1. **Enable slow query log:**
```sql
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

2. **Monitor connections:**
```sql
SHOW PROCESSLIST;
SHOW STATUS LIKE 'Threads_connected';
```

3. **Optimize tables:**
```sql
OPTIMIZE TABLE users, regex_templates, parsed_transactions;
```

### Backup

**Regular backup:**
```bash
# Backup
mysqldump -u regexflow_user -p regexflow > regexflow_backup_$(date +%Y%m%d).sql

# Restore
mysql -u regexflow_user -p regexflow < regexflow_backup_20240123.sql
```

**Automated backup script:**
```bash
#!/bin/bash
# Save as: backup_mysql.sh

BACKUP_DIR="/path/to/backups"
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u regexflow_user -pregexflow_password regexflow > "$BACKUP_DIR/regexflow_$DATE.sql"

# Keep only last 7 days
find "$BACKUP_DIR" -name "regexflow_*.sql" -mtime +7 -delete
```

## Next Steps

After MySQL setup is complete:

1. Start the backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. Check logs for successful connection:
   ```
   HikariPool-1 - Start completed.
   Started RegexFlowApplication in X seconds
   ```

3. Test with a simple API call:
   ```bash
   curl http://localhost:8080/api/auth/register -X POST \
     -H "Content-Type: application/json" \
     -d '{"username":"test","email":"test@test.com","password":"test123","fullName":"Test User","role":"MAKER"}'
   ```

4. Continue with frontend setup in [QUICKSTART.md](QUICKSTART.md)

## MySQL vs PostgreSQL

RegexFlow now uses MySQL instead of PostgreSQL. Key differences handled:

| Feature | PostgreSQL | MySQL |
|---------|-----------|-------|
| Auto Increment | SERIAL | AUTO_INCREMENT |
| Timestamp | TIMESTAMP | TIMESTAMP |
| Boolean | BOOLEAN | BOOLEAN/TINYINT(1) |
| Text | TEXT | TEXT |
| Dialect | PostgreSQLDialect | MySQLDialect |
| Default Port | 5432 | 3306 |

All queries and entities are compatible with both databases through Hibernate abstraction.

## Support

If you encounter any MySQL-specific issues:

1. Check MySQL error log:
   - macOS: `/usr/local/var/mysql/*.err`
   - Linux: `/var/log/mysql/error.log`
   - Windows: `C:\ProgramData\MySQL\MySQL Server 8.0\Data\*.err`

2. Verify MySQL configuration:
   ```bash
   mysql -u root -p -e "SHOW VARIABLES LIKE '%version%';"
   mysql -u root -p -e "SHOW VARIABLES LIKE '%char%';"
   ```

3. Test connection from Java:
   ```bash
   cd backend
   mvn spring-boot:run
   # Watch for connection errors in logs
   ```

---

**MySQL setup complete! Your RegexFlow application is ready to use MySQL database.** 🎉

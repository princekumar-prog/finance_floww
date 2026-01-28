# Installing MySQL Server (You Have Workbench Already)

Since you already have MySQL Workbench, you just need to install the MySQL Server!

---

## Quick Installation Guide

### For macOS (Recommended - Easiest)

#### Option 1: Using Homebrew (Easiest - No Admin)

```bash
# Install Homebrew if you don't have it (no admin needed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install MySQL
brew install mysql

# Start MySQL server
brew services start mysql

# Secure installation (set root password)
mysql_secure_installation
# - Press Y for all prompts
# - Set a root password when asked
# - Remember this password!
```

**MySQL will now be running on port 3306!** ✅

#### Option 2: Official MySQL Installer (Requires Admin)

1. **Download MySQL Server:**
   - Go to: https://dev.mysql.com/downloads/mysql/
   - Select: "macOS 12 (x86, 64-bit), DMG Archive"
   - Click "Download" (No login needed - click "No thanks, just start my download")

2. **Install:**
   - Open the downloaded `.dmg` file
   - Double-click the `.pkg` file
   - Follow installation wizard
   - **IMPORTANT:** Save the temporary root password shown at the end!

3. **Start MySQL:**
   ```bash
   # Start MySQL
   sudo /usr/local/mysql/support-files/mysql.server start
   
   # Or from System Preferences:
   # Open System Preferences → MySQL → Start MySQL Server
   ```

4. **Set Root Password:**
   ```bash
   # Login with temporary password
   mysql -u root -p
   # Enter temporary password
   
   # Change password
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'your_new_password';
   FLUSH PRIVILEGES;
   exit;
   ```

---

### For Windows

#### Option 1: MySQL Installer (Recommended)

1. **Download MySQL Installer:**
   - Go to: https://dev.mysql.com/downloads/installer/
   - Download: "mysql-installer-community-8.0.x.msi" (smaller web installer)
   - No login required

2. **Run Installer:**
   - Double-click the downloaded file
   - Choose: **"Server only"** (we already have Workbench)
   - Click "Next" → "Execute" → "Next"

3. **Configure MySQL:**
   - **Type and Networking:**
     - Port: 3306 (default)
     - Click "Next"
   
   - **Authentication Method:**
     - Choose: "Use Strong Password Encryption"
     - Click "Next"
   
   - **Accounts and Roles:**
     - Set MySQL Root Password (REMEMBER THIS!)
     - Click "Next"
   
   - **Windows Service:**
     - Service Name: MySQL80
     - Start at System Startup: ✅
     - Click "Next" → "Execute" → "Finish"

4. **Verify Installation:**
   ```bash
   # Open Command Prompt and run:
   mysql -u root -p
   # Enter your password
   ```

#### Option 2: Portable ZIP (No Installer)

1. Download MySQL ZIP from: https://dev.mysql.com/downloads/mysql/
2. Extract to `C:\mysql`
3. Create `C:\mysql\my.ini` with:
   ```ini
   [mysqld]
   basedir=C:/mysql
   datadir=C:/mysql/data
   port=3306
   ```
4. Initialize: `C:\mysql\bin\mysqld --initialize-insecure`
5. Start: `C:\mysql\bin\mysqld --console`

---

### For Linux (Ubuntu/Debian)

```bash
# Update package list
sudo apt update

# Install MySQL Server
sudo apt install mysql-server

# Start MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# Secure installation
sudo mysql_secure_installation
# Set root password when prompted

# Verify it's running
sudo systemctl status mysql
```

---

## After Installation - Connect to Workbench

### 1. Open MySQL Workbench

### 2. Create Connection

1. Click the **"+"** icon next to "MySQL Connections"
2. Fill in details:
   - **Connection Name:** Local MySQL Server
   - **Hostname:** 127.0.0.1
   - **Port:** 3306
   - **Username:** root
   - **Password:** Click "Store in Vault" → Enter your password

3. Click **"Test Connection"** - Should show "Successfully connected"
4. Click **"OK"**

### 3. Connect and Create Database

1. Double-click your new connection
2. In the query window, paste and run:

```sql
-- Create database
CREATE DATABASE regexflow 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Verify
SHOW DATABASES;
```

You should see `regexflow` in the list!

### 4. Import Schema via Workbench

1. Go to: **Server** → **Data Import**
2. Select: **"Import from Self-Contained File"**
3. Browse to: `regexflow_project/database/schema.sql`
4. Under "Default Target Schema", select: **regexflow**
5. Click **"Start Import"**
6. Verify tables created:
   ```sql
   USE regexflow;
   SHOW TABLES;
   ```

---

## Update Your Application Configuration

Since you have no password (or set one), update:

**File:** `backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/regexflow?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password:   # If no password, leave empty
    # OR
    password: your_password  # If you set a password
```

---

## Verify MySQL is Running

### macOS:
```bash
# Check if MySQL is running
brew services list | grep mysql
# Should show: mysql started

# Or check processes
ps aux | grep mysql

# Connect to verify
mysql -u root -p
# If no password, just press Enter
```

### Windows:
```bash
# Check service status
sc query MySQL80

# Or in Services app (Win + R → services.msc)
# Look for "MySQL80" - should be "Running"

# Connect to verify
mysql -u root -p
```

### Linux:
```bash
# Check status
sudo systemctl status mysql

# Connect to verify
mysql -u root -p
```

---

## Common Issues After Installation

### Issue 1: "Can't connect to MySQL server"

**Solution:**
```bash
# Check if MySQL is running
# macOS:
brew services start mysql

# Windows:
net start MySQL80

# Linux:
sudo systemctl start mysql
```

### Issue 2: "Access denied for user 'root'"

**Solution:**
```bash
# Reset root password
# macOS/Linux:
sudo mysql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
exit;

# Windows:
# Stop MySQL service
# Start MySQL with --skip-grant-tables
# Reset password
# Restart normally
```

### Issue 3: Port 3306 already in use

**Solution:**
```bash
# Find what's using port 3306
# macOS/Linux:
lsof -i :3306

# Windows:
netstat -ano | findstr :3306

# Kill the process or use a different port
```

---

## Quick Test Script

After installation, test your setup:

```bash
# Test connection
mysql -u root -p

# In MySQL prompt:
CREATE DATABASE test_db;
SHOW DATABASES;
DROP DATABASE test_db;
exit;
```

If all commands work, you're ready! ✅

---

## RegexFlow Setup After MySQL Installation

Once MySQL Server is installed and running:

### 1. Create Database (Choose ONE method):

**Method A: Using MySQL Workbench (GUI)**
- Open Workbench → Connect to server
- Run the queries from Step 3 above

**Method B: Using Terminal/Command Prompt**
```bash
cd regexflow_project
mysql -u root -p < database/mysql-setup-root.sql
mysql -u root -p regexflow < database/schema.sql
```

### 2. Update application.yml password

If you set a root password during installation:
```yaml
password: your_mysql_root_password
```

If you have NO password (not recommended for production):
```yaml
password: 
```

### 3. Start RegexFlow Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run

# Should see: "Started RegexFlowApplication"
```

### 4. Start Frontend

```bash
cd frontend
npm run dev

# Opens at: http://localhost:3000
```

---

## Security Recommendation

For better security, create a dedicated database user:

```sql
-- Login to MySQL
mysql -u root -p

-- Create dedicated user
CREATE USER 'regexflow_user'@'localhost' IDENTIFIED BY 'secure_password';

-- Grant permissions
GRANT ALL PRIVILEGES ON regexflow.* TO 'regexflow_user'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Exit
exit;
```

Then update `application.yml`:
```yaml
username: regexflow_user
password: secure_password
```

---

## Summary

1. **Install MySQL Server** using one of the methods above
2. **Start MySQL** service
3. **Create `regexflow` database** using Workbench or command line
4. **Import schema** from `database/schema.sql`
5. **Update password** in `application.yml` (leave empty if no password)
6. **Start RegexFlow** backend and frontend
7. **Test** at http://localhost:3000

---

## Need Help?

### Check MySQL is Running:
```bash
# macOS:
brew services list | grep mysql

# Windows:
sc query MySQL80

# Linux:
sudo systemctl status mysql
```

### Check Port 3306:
```bash
# macOS/Linux:
lsof -i :3306

# Windows:
netstat -ano | findstr :3306
```

### Test Connection:
```bash
mysql -u root -p
# Should connect successfully
```

---

**After MySQL Server is installed, your RegexFlow will work perfectly!** 🚀

The installation should take 5-10 minutes depending on your system and method.

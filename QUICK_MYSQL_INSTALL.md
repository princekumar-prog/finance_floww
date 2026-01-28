# Quick MySQL Server Installation

You have MySQL Workbench but need the MySQL Server to run RegexFlow.

---

## 🚀 Fastest Installation Method (Based on Your OS)

### For macOS (What I recommend for you)

#### Using Homebrew (No Admin - Easiest!)

```bash
# 1. Install Homebrew (if you don't have it)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 2. Install MySQL
brew install mysql

# 3. Start MySQL
brew services start mysql

# 4. Secure it (OPTIONAL - set root password)
mysql_secure_installation
```

**Done! MySQL is running on port 3306** ✅

### For Windows

1. **Download:** https://dev.mysql.com/downloads/installer/
   - Get: `mysql-installer-community-8.0.x.msi`
   
2. **Install:**
   - Run the installer
   - Choose: **"Server only"** (you have Workbench already)
   - Set root password when asked
   
3. **MySQL will start automatically** ✅

### For Linux

```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo mysql_secure_installation
```

---

## ✅ After Installation - Quick Setup

### Step 1: Verify MySQL is Running

```bash
# Test connection (may ask for password, press Enter if you didn't set one)
mysql -u root -p
```

If you see `mysql>` prompt, it's working! ✅

### Step 2: Create Database (Option A - Easy Way)

**In MySQL Workbench:**
1. Open Workbench
2. Click "+" to add connection
3. Connection settings:
   - Hostname: `127.0.0.1`
   - Port: `3306`
   - Username: `root`
   - Password: (empty or what you set)
4. Test Connection → OK
5. Double-click the connection
6. Run this query:

```sql
CREATE DATABASE regexflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

7. Import schema:
   - **Server** menu → **Data Import**
   - Select file: `regexflow_project/database/schema.sql`
   - Default Target Schema: `regexflow`
   - Click **"Start Import"**

**OR Option B - Command Line:**

```bash
cd regexflow_project
mysql -u root -p < database/mysql-setup-root.sql
mysql -u root -p regexflow < database/schema.sql
```

### Step 3: Update Application Config

Your `application.yml` already has the right settings!

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/regexflow?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password:   # Leave empty if you didn't set a password
```

If you set a password during installation, add it:
```yaml
password: your_mysql_root_password
```

### Step 4: Start RegexFlow!

```bash
# Terminal 1 - Backend
cd backend
mvn clean install
mvn spring-boot:run

# Terminal 2 - Frontend
cd frontend
npm run dev
```

Open: **http://localhost:3000** 🎉

---

## 🔍 Troubleshooting

### "Can't connect to MySQL"

```bash
# Check if MySQL is running
# macOS:
brew services list

# Windows:
sc query MySQL80

# Start if not running:
# macOS:
brew services start mysql

# Windows:
net start MySQL80
```

### "Access denied"

You set a password but forgot to update `application.yml`:
```yaml
password: your_actual_password  # Add your MySQL root password
```

### "Unknown database 'regexflow'"

You need to create the database:
```bash
mysql -u root -p
CREATE DATABASE regexflow;
exit;
```

---

## 📋 Complete Installation Checklist

- [ ] MySQL Server installed
- [ ] MySQL running (verify with `mysql -u root -p`)
- [ ] Database `regexflow` created
- [ ] Schema imported (5 tables)
- [ ] Password updated in `application.yml` (if you set one)
- [ ] Backend starts without errors
- [ ] Frontend loads at http://localhost:3000

---

## Time Estimate

- **Installation:** 5-10 minutes
- **Setup database:** 2 minutes
- **Start RegexFlow:** 2 minutes

**Total: ~15 minutes to be fully running!** ⏱️

---

## What You'll Get

After installation:
- ✅ MySQL Server running on port 3306
- ✅ MySQL Workbench can connect to it
- ✅ RegexFlow database ready
- ✅ Application can connect and work

---

## Next Steps After MySQL Installation

1. **Start MySQL** (should auto-start after installation)
2. **Create database** using Workbench or command line
3. **Import schema** from `database/schema.sql`
4. **Update password** in `application.yml` if needed
5. **Run RegexFlow** - follow `READY_TO_RUN.md`

---

**Need more details?** Check `INSTALL_MYSQL_SERVER.md` for complete instructions with screenshots and troubleshooting!

**Ready to install? Pick your OS above and follow the steps!** 🚀

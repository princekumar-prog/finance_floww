# 🚀 RegexFlow - Ready to Run!

## ✅ All Updates Complete

Your RegexFlow project is now configured with:
- ✅ **Java 17** (instead of Java 21)
- ✅ **MySQL 8.0+** database
- ✅ **Root user** configuration (username: root)

---

## 📋 Quick Start (5 Steps)

### 1️⃣ Verify Java 17

```bash
java -version
# Should show: java version "17.0.x"
```

**If you don't have Java 17:**
- Download from: https://adoptium.net/temurin/releases/?version=17
- No admin needed - portable installation available

### 2️⃣ Setup Database

```bash
cd regexflow_project

# Option A: Quick script
mysql -u root -p < database/mysql-setup-root.sql

# Option B: Manual
mysql -u root -p
CREATE DATABASE regexflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;

# Import schema
mysql -u root -p regexflow < database/schema.sql
```

### 3️⃣ Update MySQL Password

Edit: `backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    username: root
    password: root  # ⚠️ Change to YOUR MySQL root password!
```

**Current settings:**
- URL: `jdbc:mysql://localhost:3306/regexflow`
- Username: `root`
- Password: `root` (UPDATE THIS!)

### 4️⃣ Start Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run

# Wait for: "Started RegexFlowApplication"
```

### 5️⃣ Start Frontend

**In a NEW terminal:**
```bash
cd frontend
npm install
npm run dev

# Opens at: http://localhost:3000
```

---

## 🎯 Test Your Setup

### Open Browser
Navigate to: **http://localhost:3000**

### Register First User
1. Click **"Register"**
2. Choose role: **Maker**
3. Create account
4. You'll be redirected to Maker Dashboard ✅

### Test Regex Template
In Maker Dashboard:
1. Bank Name: `Test Bank`
2. SMS Type: `Debit`
3. Pattern: `Rs\.(?<amount>\d+)`
4. Sample SMS: `Debited Rs.5000`
5. Click **"Test Regex"**
6. See extracted amount! ✅

---

## 📚 Important Files

### Configuration Files
- **`backend/src/main/resources/application.yml`** - Database config ⚠️ UPDATE PASSWORD!
- **`backend/pom.xml`** - Java 17 configuration ✅
- **`database/schema.sql`** - Database structure ✅

### Documentation
- **`SETUP_INSTRUCTIONS.md`** - Complete setup guide
- **`JAVA_17_UPDATE.md`** - Java 17 specific notes
- **`MYSQL_SETUP.md`** - MySQL configuration help
- **`QUICKSTART.md`** - 5-minute setup
- **`README.md`** - Full documentation

---

## ⚠️ Important Notes

### 1. MySQL Root Password
**You MUST update the password in `application.yml`:**

```yaml
password: root  # Change this to your actual MySQL root password!
```

To find/reset your MySQL root password:
```bash
# Mac:
mysql.server start
mysql -u root  # If no password set

# Set password:
mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED BY 'your_new_password';
FLUSH PRIVILEGES;
exit;
```

### 2. Java Version
Make sure you're using Java 17:
```bash
java -version
# Must show 17.0.x

# If multiple Java versions:
export JAVA_HOME=/path/to/jdk-17
```

### 3. MySQL Running
Ensure MySQL is running:
```bash
# Mac:
brew services list | grep mysql
# Should show: started

# Start if needed:
brew services start mysql

# Linux:
sudo systemctl status mysql
sudo systemctl start mysql
```

---

## 🔧 Troubleshooting

### ❌ Error: "Access denied for user 'root'"
**Fix:** Update password in `application.yml` to match your MySQL root password

### ❌ Error: "Unknown database 'regexflow'"
**Fix:**
```bash
mysql -u root -p
CREATE DATABASE regexflow;
exit;
mysql -u root -p regexflow < database/schema.sql
```

### ❌ Backend fails to start
**Fix:**
```bash
# Check Java version
java -version  # Must be 17.x

# Clean rebuild
cd backend
mvn clean install

# Check MySQL connection
mysql -u root -p regexflow
```

### ❌ Port 8080 already in use
**Fix:**
```bash
# Find and kill process
lsof -ti:8080 | xargs kill -9
```

---

## ✅ Verification Checklist

Before you start:
- [ ] Java 17 installed (`java -version`)
- [ ] MySQL 8.0+ installed and running
- [ ] Database `regexflow` created
- [ ] Schema imported (5 tables)
- [ ] Password updated in `application.yml`
- [ ] Maven installed (`mvn -version`)
- [ ] Node.js installed (`node -v`)

After starting:
- [ ] Backend running (http://localhost:8080)
- [ ] Frontend running (http://localhost:3000)
- [ ] Can access homepage
- [ ] Can register user
- [ ] Can login
- [ ] Dashboard loads

---

## 🎓 What's Different from Original

| Item | Original | Your Setup |
|------|----------|------------|
| Java Version | 21 | **17** ✅ |
| Database | PostgreSQL | **MySQL** ✅ |
| DB User | regexflow_user | **root** ✅ |
| DB Password | regexflow_password | **YOUR_PASSWORD** ⚠️ |

---

## 🚀 Ready to Start!

Run these commands:

```bash
# Terminal 1 - Backend
cd backend
mvn spring-boot:run

# Terminal 2 - Frontend  
cd frontend
npm run dev
```

Then open: **http://localhost:3000**

---

## 📖 Next Steps

1. **Complete Setup:** Follow `SETUP_INSTRUCTIONS.md` for detailed walkthrough
2. **Learn Features:** Read `README.md` for complete documentation
3. **Test Workflow:** Create templates, approve them, parse SMS
4. **Add Your Banks:** Add regex patterns for your bank SMS formats

---

## 🎉 You're All Set!

Your RegexFlow application is configured for:
- ✅ Java 17 (no admin access needed)
- ✅ MySQL database
- ✅ Root user access
- ✅ All features working

**Just update the MySQL password in `application.yml` and you're ready to go!**

---

**Questions?** Check:
- `SETUP_INSTRUCTIONS.md` - Step-by-step setup
- `JAVA_17_UPDATE.md` - Java 17 information
- `MYSQL_SETUP.md` - MySQL help

**Let's parse some SMS! 🚀**

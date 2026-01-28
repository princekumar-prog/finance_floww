# RegexFlow - Complete Setup Instructions

## Quick Setup Guide (10 minutes)

Follow these steps to get RegexFlow running on your system.

---

## Step 1: Prerequisites Check ✅

### 1.1 Install Java 17

```bash
# Check if Java 17 is installed
java -version

# Should show: java version "17.0.x"
```

**If not installed:**

- **Windows/Mac/Linux (No Admin):** Download from https://adoptium.net/temurin/releases/?version=17
- **Mac (Homebrew):** `brew install openjdk@17`
- **Ubuntu/Debian:** `sudo apt install openjdk-17-jdk`

### 1.2 Install Maven

```bash
# Check Maven
mvn -version

# Should show: Apache Maven 3.8.x or higher
```

**If not installed:**
- Download from https://maven.apache.org/download.cgi
- Or use: `brew install maven` (Mac)

### 1.3 Install Node.js

```bash
# Check Node.js
node -v

# Should show: v18.x or higher
```

**If not installed:**
- Download from https://nodejs.org/

### 1.4 Install MySQL

```bash
# Check MySQL
mysql --version

# Should show: mysql Ver 8.0.x or higher
```

**If not installed:**
- **Mac:** `brew install mysql && brew services start mysql`
- **Ubuntu:** `sudo apt install mysql-server`
- **Windows:** Download from https://dev.mysql.com/downloads/mysql/

---

## Step 2: Database Setup (3 minutes)

### 2.1 Create Database

```bash
# Login to MySQL as root
mysql -u root -p
# Enter your MySQL root password
```

In MySQL prompt:
```sql
-- Create database
CREATE DATABASE regexflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify
SHOW DATABASES;

-- Exit
exit;
```

**OR** use the quick script:
```bash
cd regexflow_project
mysql -u root -p < database/mysql-setup-root.sql
```

### 2.2 Import Schema

```bash
mysql -u root -p regexflow < database/schema.sql
# Enter your MySQL root password when prompted
```

### 2.3 Verify Tables

```bash
mysql -u root -p regexflow

# In MySQL prompt:
SHOW TABLES;

# Should show 5 tables:
# - users
# - regex_templates
# - regex_audit_trail
# - raw_sms_logs
# - parsed_transactions

exit;
```

---

## Step 3: Configure Application (2 minutes)

### 3.1 Update Database Password

Edit: `backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/regexflow?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: YOUR_MYSQL_ROOT_PASSWORD  # ⚠️ Change this to your actual password!
```

**Security Tip:** For production, use environment variables:
```yaml
password: ${DB_PASSWORD}
```

Then set: `export DB_PASSWORD=your_password`

---

## Step 4: Backend Setup (3 minutes)

### 4.1 Install Dependencies

```bash
cd regexflow_project/backend

# Install all dependencies
mvn clean install -DskipTests

# This will download all required libraries
```

### 4.2 Test Backend

```bash
# Run tests to ensure everything works
mvn test

# Should show: BUILD SUCCESS
```

### 4.3 Start Backend

```bash
# Start the Spring Boot application
mvn spring-boot:run

# Wait for:
# "Started RegexFlowApplication in X seconds"
```

**Keep this terminal running!**

Backend is now running at: http://localhost:8080

---

## Step 5: Frontend Setup (2 minutes)

### 5.1 Install Dependencies

Open a **NEW terminal** window:

```bash
cd regexflow_project/frontend

# Install npm packages
npm install

# This will install React and all dependencies
```

### 5.2 Start Frontend

```bash
# Start the Vite dev server
npm run dev

# Should show:
# Local: http://localhost:3000
```

**Keep this terminal running!**

Frontend is now running at: http://localhost:3000

---

## Step 6: Test the Application ✅

### 6.1 Open Browser

Navigate to: **http://localhost:3000**

You should see the RegexFlow homepage!

### 6.2 Register a User

1. Click **"Register"**
2. Fill in details:
   - Username: `testmaker`
   - Email: `maker@test.com`
   - Password: `password123`
   - Full Name: `Test Maker`
   - Role: Select **"Maker"**
3. Click **"Register"**

### 6.3 Test Maker Dashboard

After registration, you'll be redirected to the Maker Dashboard.

**Create a Test Template:**

1. Go to **"Create Template"** tab
2. Fill in:
   - **Bank Name:** `HDFC Bank`
   - **SMS Type:** `Debit`
   - **Regex Pattern:**
     ```regex
     Rs\.(?<amount>[\d,]+).*Bal.*Rs\.(?<balance>[\d,]+)
     ```
   - **Sample SMS:**
     ```
     Debited Rs.5,000 from A/c XX1234. Avl Bal: Rs.45,000
     ```
3. Click **"Test Regex"**
4. You should see extracted fields in the preview!
5. Click **"Save Draft"** or **"Submit for Approval"**

### 6.4 Test Other Roles

**Register a Checker:**
- Logout → Register with role "Checker"
- View pending templates
- Approve/reject templates

**Register a User:**
- Logout → Register with role "Normal User"
- Upload SMS and see parsed transactions

---

## Step 7: Run Demo Script (Optional)

```bash
# Stop the backend (Ctrl+C in backend terminal)
# Then run:

cd regexflow_project
chmod +x sample-data/demo-script.sh
./sample-data/demo-script.sh

# This creates:
# - maker_demo / maker123
# - checker_demo / checker123
# - user_demo / user123

# And sets up sample templates
```

---

## Common Issues & Solutions

### Issue 1: "Access denied for user 'root'"

**Solution:** Update the password in `application.yml`
```yaml
password: your_actual_mysql_root_password
```

### Issue 2: "Unknown database 'regexflow'"

**Solution:** Create the database
```bash
mysql -u root -p
CREATE DATABASE regexflow;
exit;
```

### Issue 3: Backend won't start - "Port 8080 already in use"

**Solution:** Kill the process using port 8080
```bash
# Mac/Linux:
lsof -ti:8080 | xargs kill -9

# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Issue 4: Frontend won't start - "Port 3000 already in use"

**Solution:** Kill the process or use different port
```bash
# Kill process:
lsof -ti:3000 | xargs kill -9

# Or change port in frontend/vite.config.js:
server: { port: 3001 }
```

### Issue 5: "JAVA_HOME is not set"

**Solution:** Set JAVA_HOME
```bash
# Find Java location
which java
/usr/libexec/java_home -v 17  # Mac

# Set JAVA_HOME (add to ~/.bashrc or ~/.zshrc)
export JAVA_HOME=/path/to/jdk-17
export PATH=$JAVA_HOME/bin:$PATH
```

### Issue 6: Maven build fails

**Solution:** Clean and rebuild
```bash
cd backend
mvn clean
rm -rf ~/.m2/repository/com/regexflow
mvn install
```

---

## Verification Checklist

After setup, verify:

- [ ] Java 17 installed (`java -version`)
- [ ] MySQL running and database created
- [ ] Backend starts without errors
- [ ] Frontend starts at http://localhost:3000
- [ ] Can register a user
- [ ] Can login successfully
- [ ] Maker dashboard loads
- [ ] Can create and test regex templates
- [ ] Can parse SMS messages

---

## Next Steps

1. **Read the documentation:**
   - `README.md` - Full documentation
   - `JAVA_17_UPDATE.md` - Java 17 specific notes
   - `MYSQL_SETUP.md` - MySQL configuration details

2. **Explore sample data:**
   - `sample-data/bank-sms-samples.json` - 10 sample SMS patterns

3. **Test the workflow:**
   - Create templates as Maker
   - Approve as Checker
   - Parse SMS as User

4. **Customize:**
   - Add your bank SMS patterns
   - Adjust regex templates
   - Modify UI colors in CSS

---

## Production Deployment

When ready for production:

1. **Build backend JAR:**
   ```bash
   cd backend
   mvn clean package
   java -jar target/regexflow-backend-1.0.0.jar
   ```

2. **Build frontend:**
   ```bash
   cd frontend
   npm run build
   # Deploy dist/ folder to web server
   ```

3. **Security checklist:**
   - [ ] Change all default passwords
   - [ ] Use environment variables for secrets
   - [ ] Enable HTTPS
   - [ ] Set up proper database user (not root)
   - [ ] Configure firewall
   - [ ] Enable production mode

---

## Getting Help

- **Setup Issues:** Read `QUICKSTART.md`
- **MySQL Issues:** Read `MYSQL_SETUP.md`
- **Java 17 Issues:** Read `JAVA_17_UPDATE.md`
- **General Questions:** Read `README.md`

---

## Success! 🎉

If you've completed all steps, congratulations! RegexFlow is now running on your machine.

**Access Points:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api

**Test Credentials (if you ran demo script):**
- Maker: `maker_demo` / `maker123`
- Checker: `checker_demo` / `checker123`
- User: `user_demo` / `user123`

**Happy parsing!** 🚀

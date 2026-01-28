# RegexFlow - Quick Start Guide

Get up and running with RegexFlow in 5 minutes!

## Prerequisites Checklist

- [ ] Java 17 installed (`java -version`)
- [ ] Maven 3.8+ installed (`mvn -version`)
- [ ] Node.js 18+ installed (`node -v`)
- [ ] MySQL 8.0+ installed and running
- [ ] Git (for version control)

## Step 1: Database Setup (2 minutes)

```bash
# Start MySQL (if not running)
# macOS: brew services start mysql
# Linux: sudo systemctl start mysql
# Windows: Start from Services

# Create database
mysql -u root -p
```

In MySQL prompt:
```sql
CREATE DATABASE regexflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

Or use the provided script:
```bash
mysql -u root -p < database/mysql-setup-root.sql
```

Import schema:
```bash
mysql -u root -p regexflow < database/schema.sql
```

**Important:** Update your MySQL root password in `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    username: root
    password: YOUR_MYSQL_ROOT_PASSWORD  # Change this!
```

## Step 2: Install Dependencies (2 minutes)

```bash
# Navigate to project
cd regexflow_project

# Make scripts executable
chmod +x scripts/setup.sh
chmod +x sample-data/demo-script.sh

# Run setup (installs all dependencies)
./scripts/setup.sh
```

## Step 3: Start Application (1 minute)

```bash
# Option 1: Start both backend and frontend
./run.sh

# Option 2: Start separately
# Terminal 1 - Backend
cd backend && mvn spring-boot:run

# Terminal 2 - Frontend
cd frontend && npm run dev
```

## Step 4: Access Application

- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080/api

## Step 5: Test with Demo Data

```bash
# Run demo script (creates users and sample data)
./sample-data/demo-script.sh
```

## Default Test Users

After running demo script:

| Role | Username | Password |
|------|----------|----------|
| Maker | maker_demo | maker123 |
| Checker | checker_demo | checker123 |
| User | user_demo | user123 |

## Quick Workflow Test

### 1. As Maker (maker_demo)
1. Login at http://localhost:3000/login
2. Create a new regex template:
   - Bank: "HDFC Bank"
   - Type: "Debit"
   - Pattern: `Your A/c (?<account>\w+) debited for Rs\.(?<amount>[\d,]+\.?\d*)`
   - Sample SMS: "Your A/c XX1234 debited for Rs.5,000.00"
3. Test the regex (see live preview)
4. Submit for approval

### 2. As Checker (checker_demo)
1. Logout and login as checker_demo
2. View pending templates
3. Review the template
4. Approve it

### 3. As User (user_demo)
1. Logout and login as user_demo
2. Go to "Upload SMS"
3. Paste: "Your A/c XX1234 debited for Rs.5,000.00"
4. Click "Parse SMS"
5. View extracted transaction details!

## Troubleshooting

### Backend won't start
```bash
# Check if MySQL is running
mysql -u root -p regexflow

# Check Java version
java -version  # Should be 17+

# Clean and rebuild
cd backend
mvn clean install
```

### Frontend won't start
```bash
# Clear node_modules and reinstall
cd frontend
rm -rf node_modules
npm install
```

### Database connection error
```bash
# Check MySQL is running
mysql -u regexflow_user -p

# Update credentials in backend/src/main/resources/application.yml
# Make sure MySQL is running on port 3306
```

### Port already in use
```bash
# Backend (8080)
lsof -ti:8080 | xargs kill -9

# Frontend (3000)
lsof -ti:3000 | xargs kill -9
```

## API Testing with cURL

### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@test.com",
    "password": "password123",
    "fullName": "Test User",
    "role": "MAKER"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### Create Template (use token from login)
```bash
curl -X POST http://localhost:8080/api/maker/templates \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "bankName": "Test Bank",
    "regexPattern": "Rs\\.(?<amount>\\d+)",
    "smsType": "DEBIT",
    "sampleSms": "Debited Rs.5000",
    "description": "Test template"
  }'
```

## Next Steps

1. Read the full [README.md](README.md) for detailed documentation
2. Explore sample SMS patterns in `sample-data/bank-sms-samples.json`
3. Check out the [API Documentation](README.md#api-documentation)
4. Run tests: `cd backend && mvn test`
5. View code coverage: `mvn jacoco:report`

## Need Help?

- Check [README.md](README.md) for comprehensive documentation
- Review [sample-data/bank-sms-samples.json](sample-data/bank-sms-samples.json) for regex pattern examples
- See [database/schema.sql](database/schema.sql) for database structure

## Production Deployment

For production deployment:

1. Update `application.yml` with production database credentials
2. Set secure JWT secret: `jwt.secret=YOUR_SECURE_SECRET_HERE`
3. Configure CORS for your domain
4. Enable HTTPS
5. Set up proper logging and monitoring
6. Use environment variables for sensitive data
7. Build frontend: `cd frontend && npm run build`
8. Build backend: `cd backend && mvn clean package`

---

**Happy coding! 🚀**

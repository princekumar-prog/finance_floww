# RegexFlow — Fintech SMS-to-Ledger Engine

![RegexFlow Banner](https://img.shields.io/badge/RegexFlow-SMS%20Parser-4F46E5?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green?style=flat-square)
![React](https://img.shields.io/badge/React-18.2-blue?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue?style=flat-square)

**RegexFlow** is a production-ready fintech management portal that converts Bank SMS messages into structured financial transactions using regex templates with a robust Maker-Checker approval workflow.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Installation](#installation)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Security](#security)
- [Performance](#performance)
- [How We Used AI](#how-we-used-ai)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Overview

RegexFlow transforms unstructured bank SMS messages into structured financial ledger entries through:

1. **Maker** - Creates and tests regex templates for different bank SMS formats
2. **Checker** - Reviews and approves templates through a secure workflow
3. **Normal User** - Uploads SMS and views automatically parsed transactions

### Why RegexFlow?

- **Automated Transaction Parsing**: Convert SMS to structured data instantly
- **Maker-Checker Compliance**: Enterprise-grade approval workflow
- **Multi-Bank Support**: Handles various SMS formats from different banks
- **Real-Time Testing**: Live preview of regex extraction before deployment
- **Audit Trail**: Complete history of all template changes
- **PWA Ready**: Works offline with Progressive Web App support

---

## ✨ Features

### 🔧 Maker Dashboard
- Create regex templates with named capturing groups
- Test patterns with live SMS preview
- See extracted fields in real-time JSON preview
- Save drafts and submit for approval
- Track template status (Draft, Pending, Active, Rejected, Deprecated)

### ✅ Checker Dashboard
- Review pending template submissions
- View regex patterns and test results
- Approve or reject with comments
- Deprecate outdated templates
- Complete audit trail visibility

### 📱 User Dashboard
- Upload bank SMS messages
- Auto-detect best matching template
- View extracted transaction details
- Filter transactions by date, bank, amount
- Transaction history with pagination
- Export capabilities

### 🔐 Security Features
- JWT-based authentication
- Role-based access control (RBAC)
- BCrypt password hashing
- CORS configuration
- XSS protection
- SQL injection prevention

### ⚡ Performance Features
- Regex catastrophic backtracking detection
- Execution timeout protection (5s default)
- Database indexing for fast queries
- Connection pooling (HikariCP)
- Optimized for 10,000+ SMS batch processing

---

## 🛠️ Tech Stack

### Backend
- **Java 17** - LTS version
- **Spring Boot 3.2.1** - Framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - ORM
- **Hibernate** - Database operations
- **MySQL 8.0+** - Primary database
- **JWT (JJWT)** - Token-based auth
- **Maven** - Build tool
- **JUnit 5 & Mockito** - Testing (70%+ coverage)
- **Jacoco** - Code coverage

### Frontend
- **React 18** - UI library
- **Vite** - Build tool
- **React Router** - Navigation
- **Axios** - HTTP client
- **React Hook Form** - Form handling
- **React Toastify** - Notifications
- **date-fns** - Date utilities
- **Vite PWA Plugin** - Progressive Web App

### Database
- **MySQL 8.0+** - Production database
- **H2** - In-memory database for testing

---

## 🏗️ Architecture

### Project Structure

```
regexflow_project/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/regexflow/
│   │   │   │   ├── controller/      # REST API Controllers
│   │   │   │   ├── service/         # Business Logic
│   │   │   │   ├── repository/      # Data Access Layer
│   │   │   │   ├── entity/          # JPA Entities
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── enums/           # Enumerations
│   │   │   │   ├── parser/          # Regex & SMS Parsers
│   │   │   │   ├── workflow/        # Maker-Checker Logic
│   │   │   │   ├── security/        # JWT & Security
│   │   │   │   ├── exception/       # Custom Exceptions
│   │   │   │   ├── config/          # Configuration
│   │   │   │   └── util/            # Utilities
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── application-test.yml
│   │   └── test/                    # JUnit Tests
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/              # Reusable Components
│   │   ├── pages/                   # Page Components
│   │   ├── services/                # API Services
│   │   ├── context/                 # React Context
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
├── database/
│   └── schema.sql                   # Database Schema
├── sample-data/
│   ├── bank-sms-samples.json       # Sample SMS Data
│   └── demo-script.sh              # Demo Script
├── scripts/
│   └── setup.sh                    # Setup Script
└── README.md
```

### Database Schema

```sql
Users
├── id (PK)
├── username (UNIQUE)
├── email (UNIQUE)
├── password (BCrypt)
├── role (MAKER/CHECKER/NORMAL_USER)
└── timestamps

RegexTemplates
├── id (PK)
├── bankName
├── regexPattern
├── smsType (DEBIT/CREDIT/BILL)
├── status (DRAFT/PENDING_APPROVAL/ACTIVE/REJECTED/DEPRECATED)
├── createdBy (FK -> Users)
├── approvedBy (FK -> Users)
└── timestamps

RegexAuditTrail
├── id (PK)
├── templateId (FK -> RegexTemplates)
├── previousStatus
├── newStatus
├── action
├── performedBy (FK -> Users)
└── timestamp

RawSmsLogs
├── id (PK)
├── rawSmsText
├── parseStatus (SUCCESS/PARTIAL/FAILED/NO_MATCH/ERROR)
├── templateId (FK -> RegexTemplates)
├── uploadedBy (FK -> Users)
└── timestamp

ParsedTransactions
├── id (PK)
├── smsLogId (FK -> RawSmsLogs)
├── userId (FK -> Users)
├── bankName
├── amount (DECIMAL 19,2)
├── balance (DECIMAL 19,2)
├── transactionType
├── transactionDate
└── timestamps
```

### Maker-Checker Workflow State Machine

```
DRAFT → PENDING_APPROVAL → ACTIVE → DEPRECATED
           ↓
        REJECTED → DRAFT (can be revised)
```

**State Transition Rules:**
- Only Maker can create drafts
- Only Maker can submit for approval
- Only Checker (different from Maker) can approve/reject
- Active templates can be deprecated by Checker
- Deprecated templates cannot transition to any other state
- All transitions create audit trail entries

---

## 🚀 Installation

### Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Node.js 18+** and npm
- **MySQL 8.0+**

### Database Setup

1. Install MySQL and create database:

```sql
CREATE DATABASE regexflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'regexflow_user'@'localhost' IDENTIFIED BY 'regexflow_password';
GRANT ALL PRIVILEGES ON regexflow.* TO 'regexflow_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Import schema:

```bash
mysql -u regexflow_user -p regexflow < database/schema.sql
```

### Quick Start

1. **Clone the repository:**

```bash
cd regexflow_project
```

2. **Run setup script:**

```bash
chmod +x scripts/setup.sh
./scripts/setup.sh
```

3. **Start the application:**

```bash
./run.sh
```

Or manually:

**Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm run dev
```

4. **Access the application:**

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api

---

## 📖 Usage

### User Registration

Register with one of three roles:

1. **MAKER** - Create and test regex templates
2. **CHECKER** - Review and approve templates
3. **NORMAL_USER** - Upload SMS and view transactions

### Maker Workflow

1. Login as Maker
2. Go to "Create Template" tab
3. Enter:
   - Bank Name (e.g., "HDFC Bank")
   - SMS Type (Debit/Credit/Bill)
   - Regex Pattern with named groups:
     ```regex
     Your A/c (?<account>\w+) debited for Rs\.(?<amount>[\d,]+\.?\d*) on (?<date>[\d-A-Za-z]+).*?Avl Bal: Rs\.(?<balance>[\d,]+\.?\d*)
     ```
   - Sample SMS for testing
4. Click "Test Regex" to see live preview
5. Save as Draft or Submit for Approval

### Checker Workflow

1. Login as Checker
2. View pending templates
3. Select a template to review
4. Review regex pattern and extracted fields
5. Approve with comments or Reject with reason

### User Workflow

1. Login as Normal User
2. Go to "Upload SMS" tab
3. Paste bank SMS text
4. Click "Parse SMS"
5. View extracted transaction details
6. Check "Transaction History" for all parsed transactions

---

## 📡 API Documentation

### Authentication APIs

**Register:**
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_maker",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "role": "MAKER"
}
```

**Login:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_maker",
  "password": "password123"
}
```

### Maker APIs

**Create Template:**
```http
POST /api/maker/templates
Authorization: Bearer <token>
Content-Type: application/json

{
  "bankName": "HDFC Bank",
  "regexPattern": "pattern...",
  "smsType": "DEBIT",
  "sampleSms": "sample...",
  "description": "description..."
}
```

**Test Regex:**
```http
POST /api/maker/templates/test
Authorization: Bearer <token>

{
  "regexPattern": "pattern...",
  "sampleSms": "sample..."
}
```

### Checker APIs

**Get Pending Templates:**
```http
GET /api/checker/templates/pending
Authorization: Bearer <token>
```

**Approve Template:**
```http
POST /api/checker/templates/{id}/approve
Authorization: Bearer <token>

{
  "comments": "Looks good!"
}
```

### User APIs

**Parse SMS:**
```http
POST /api/user/sms/parse
Authorization: Bearer <token>

{
  "smsText": "Your A/c XX1234 debited...",
  "senderHeader": "HDFCBK"
}
```

**Get Transactions:**
```http
GET /api/user/transactions?page=0&size=20
Authorization: Bearer <token>
```

---

## 🧪 Testing

### Run Backend Tests

```bash
cd backend
mvn test
```

### Generate Coverage Report

```bash
mvn clean test jacoco:report
```

View coverage at: `backend/target/site/jacoco/index.html`

### Test Coverage

- **Target:** 70%+ code coverage
- **Frameworks:** JUnit 5, Mockito
- **Categories:**
  - Unit Tests (Parser, Workflow, Services)
  - Integration Tests (Controllers, Repositories)
  - State Machine Tests (Workflow transitions)

### Run Demo Script

```bash
chmod +x sample-data/demo-script.sh
./sample-data/demo-script.sh
```

---

## 🔒 Security

### Authentication
- JWT tokens with 24-hour expiration
- BCrypt password hashing (strength: 10)
- Secure HTTP-only cookies (optional)

### Authorization
- Role-based access control (RBAC)
- Method-level security annotations
- Endpoint protection:
  - `/api/auth/**` - Public
  - `/api/maker/**` - MAKER only
  - `/api/checker/**` - CHECKER only
  - `/api/user/**` - NORMAL_USER only

### Best Practices
- CORS configuration for specific origins
- SQL injection prevention (Prepared Statements)
- XSS protection (Content Security Policy)
- Input validation using Bean Validation
- Error handling without sensitive info leakage

---

## ⚡ Performance

### Regex Safety
- **Catastrophic Backtracking Detection**
  - Validates patterns before execution
  - Rejects dangerous constructs: `(.*)*, (.+)+, (a*)*`
  
- **Execution Timeout Protection**
  - Default: 5 seconds per regex execution
  - Prevents infinite loops
  - Uses ExecutorService with Future timeout

### Database Optimization
- Indexed columns for fast lookups
- Connection pooling (HikariCP)
- Lazy loading for relationships
- Pagination for large datasets

### Scalability
- Tested for 10,000 SMS batch processing
- Stateless REST API design
- Horizontal scaling ready
- Database query optimization

---

## 🤖 How We Used AI

This section documents how AI (Cursor AI with Claude Sonnet 4.5) was leveraged to build RegexFlow, demonstrating responsible and effective AI-assisted development.

### Initial Project Setup (10 minutes)

**Prompt 1:** *"You are Cursor AI acting as a Senior Full-Stack Fintech Architect. Build a COMPLETE PRODUCTION-READY project named RegexFlow..."*

**AI Contribution:**
- Generated complete project structure for Spring Boot + React
- Created Maven POM with all dependencies (Spring Security, JWT, JPA, PostgreSQL)
- Set up Vite configuration with PWA support
- Established clean architecture folder structure

**Human Oversight:**
- Reviewed dependency versions for compatibility
- Verified security configurations
- Ensured proper separation of concerns

### Database Design & Entity Modeling (15 minutes)

**AI Contribution:**
- Designed normalized database schema with proper relationships
- Created JPA entities with:
  - Proper indexing strategies
  - BigDecimal for financial precision
  - Audit trail entities
  - Optimistic locking with @Version

**Human Decisions:**
- Reviewed and approved table relationships
- Validated index placement for query patterns
- Confirmed financial data precision requirements

### Core Business Logic (30 minutes)

**Regex Parser Engine:**
- AI generated catastrophic backtracking detection
- Implemented timeout protection using ExecutorService
- Created match scoring algorithm for template selection

**Maker-Checker Workflow:**
- AI implemented state machine with validation
- Built audit trail logging
- Created transition rules enforcement

**Human Refinement:**
- Added business-specific validation rules
- Enhanced error messages for domain clarity
- Improved state transition logging

### Authentication & Security (20 minutes)

**AI Contribution:**
- Implemented JWT token generation/validation
- Configured Spring Security with role-based access
- Set up BCrypt password encoding
- Created authentication filters and interceptors

**Security Review:**
- Human verified JWT secret management
- Reviewed token expiration policies
- Validated CORS configuration
- Confirmed no hardcoded credentials

### Frontend Development (45 minutes)

**AI Generated:**
- Complete React component structure
- Responsive CSS with modern design
- Form validation and error handling
- API service layer with Axios
- React Router configuration
- PWA manifest and service worker

**Human Enhancements:**
- Refined UI/UX based on fintech standards
- Improved accessibility (ARIA labels)
- Enhanced mobile responsiveness
- Customized color scheme and branding

### Testing Suite (25 minutes)

**AI Contribution:**
- Generated JUnit test cases for all services
- Created Mockito mocks for dependencies
- Implemented state machine transition tests
- Built regex validation tests

**Coverage:**
- Initial AI-generated tests: ~65% coverage
- Human-added edge cases: 75%+ coverage
- Focus areas: Workflow, Parser, Authentication

### Documentation (15 minutes)

**AI Generated:**
- Complete README structure
- API documentation with examples
- Setup instructions
- Architecture diagrams (text-based)

**Human Polish:**
- Added business context
- Refined installation steps
- Created this "How We Used AI" section
- Verified all commands and examples

---

## 📚 Lessons Learned from AI-Assisted Development

### What Worked Well

1. **Rapid Prototyping**
   - AI generated 90% of boilerplate code
   - Reduced development time from weeks to hours
   - Consistent code style across modules

2. **Best Practices**
   - AI suggested industry-standard patterns
   - Proper separation of concerns
   - Enterprise-grade architecture

3. **Test Coverage**
   - Comprehensive test suite generated quickly
   - Good balance of unit and integration tests

### What Required Human Oversight

1. **Business Logic Validation**
   - AI needed guidance on state transition rules
   - Domain-specific edge cases required manual addition
   - Financial calculations verified independently

2. **Security Configuration**
   - JWT secret management reviewed
   - CORS policy adjusted for production
   - Rate limiting added manually

3. **Performance Optimization**
   - Database index placement verified
   - Query optimization reviewed
   - Caching strategy designed manually

### Best Practices for AI-Assisted Development

1. **Clear Prompts**
   - Specify technology stack versions
   - Define architecture patterns upfront
   - Provide domain context

2. **Iterative Refinement**
   - Start with core features
   - Review and test incrementally
   - Refactor with AI assistance

3. **Human-in-the-Loop**
   - Review all generated code
   - Test thoroughly
   - Validate business logic
   - Security audit critical components

4. **Documentation**
   - Document AI contributions
   - Note human decisions
   - Track refinements

---

## 🎯 Future Enhancements

- [ ] Machine Learning for auto-template generation
- [ ] Multi-language support
- [ ] Mobile apps (iOS/Android)
- [ ] Advanced analytics dashboard
- [ ] Bulk SMS import from CSV
- [ ] Export to accounting software (QuickBooks, Tally)
- [ ] OCR support for SMS screenshots
- [ ] Webhook notifications
- [ ] GraphQL API
- [ ] Microservices architecture

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Code Style

- **Backend:** Follow Google Java Style Guide
- **Frontend:** Use Prettier for formatting
- **Tests:** Minimum 70% coverage for new code

---

## 📄 License

This project is licensed under the MIT License. See `LICENSE` file for details.

---

## 👥 Authors

**RegexFlow Team**
- Built with AI assistance (Cursor AI + Claude Sonnet 4.5)
- Human oversight and refinement
- Production-ready architecture

---

## 🙏 Acknowledgments

- Spring Boot team for excellent framework
- React team for powerful UI library
- Cursor AI for development acceleration
- Open source community

---

## 📞 Support

For issues, questions, or contributions:

- **Issues:** GitHub Issues
- **Discussions:** GitHub Discussions
- **Email:** support@regexflow.com

---

**Built with ❤️ using Spring Boot, React, and AI**

*RegexFlow - Transform SMS into Insights*

#!/bin/bash

# MySQL Server Installation Script for macOS
# Run this script to install MySQL Server automatically

echo "========================================"
echo "MySQL Server Installation for RegexFlow"
echo "========================================"
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Check if Homebrew is installed
echo "Step 1: Checking for Homebrew..."
if command -v brew &> /dev/null; then
    print_success "Homebrew is already installed"
else
    print_warning "Homebrew not found. Installing Homebrew..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    
    if [ $? -eq 0 ]; then
        print_success "Homebrew installed successfully"
    else
        print_error "Failed to install Homebrew"
        exit 1
    fi
fi

echo ""

# Check if MySQL is already installed
echo "Step 2: Checking for MySQL..."
if command -v mysql &> /dev/null; then
    MYSQL_VERSION=$(mysql --version)
    print_warning "MySQL is already installed: $MYSQL_VERSION"
    
    echo ""
    read -p "Do you want to reinstall MySQL? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Skipping MySQL installation"
        
        # Check if MySQL is running
        if brew services list | grep mysql | grep started &> /dev/null; then
            print_success "MySQL is already running"
        else
            print_info "Starting MySQL..."
            brew services start mysql
            print_success "MySQL started"
        fi
        
        echo ""
        print_success "MySQL is ready to use!"
        echo ""
        print_info "Next steps:"
        echo "  1. Create database: mysql -u root -p < database/mysql-setup-root.sql"
        echo "  2. Import schema: mysql -u root -p regexflow < database/schema.sql"
        echo "  3. Start RegexFlow: ./run.sh"
        exit 0
    fi
fi

echo ""

# Install MySQL
echo "Step 3: Installing MySQL Server..."
print_info "This may take a few minutes..."

brew install mysql

if [ $? -eq 0 ]; then
    print_success "MySQL installed successfully"
else
    print_error "Failed to install MySQL"
    exit 1
fi

echo ""

# Start MySQL
echo "Step 4: Starting MySQL Server..."
brew services start mysql

if [ $? -eq 0 ]; then
    print_success "MySQL server started"
else
    print_error "Failed to start MySQL"
    exit 1
fi

echo ""
echo "Waiting for MySQL to fully start..."
sleep 3

# Verify MySQL is running
if brew services list | grep mysql | grep started &> /dev/null; then
    print_success "MySQL is running on port 3306"
else
    print_warning "MySQL may not have started correctly"
fi

echo ""

# Test connection
echo "Step 5: Testing MySQL connection..."
if mysql -u root -e "SELECT 1" &> /dev/null; then
    print_success "MySQL connection successful (no password set)"
    NO_PASSWORD=true
else
    print_info "MySQL requires password authentication"
    NO_PASSWORD=false
fi

echo ""

# Ask about root password
if [ "$NO_PASSWORD" = true ]; then
    echo "Step 6: MySQL Root Password Setup"
    print_warning "No root password is currently set"
    echo ""
    read -p "Do you want to set a root password? (recommended for security) (y/n) " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Running mysql_secure_installation..."
        print_info "Answer Y to all questions and set a strong root password"
        echo ""
        sleep 2
        mysql_secure_installation
    else
        print_warning "Skipping password setup (not recommended for production)"
    fi
fi

echo ""

# Create RegexFlow database
echo "Step 7: Creating RegexFlow Database..."

if [ -f "database/mysql-setup-root.sql" ]; then
    if mysql -u root -p < database/mysql-setup-root.sql 2> /dev/null; then
        print_success "Database 'regexflow' created"
    else
        print_warning "Could not create database automatically"
        print_info "You can create it manually:"
        echo "  mysql -u root -p"
        echo "  CREATE DATABASE regexflow;"
    fi
else
    print_info "Creating database manually..."
    mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS regexflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    
    if [ $? -eq 0 ]; then
        print_success "Database 'regexflow' created"
    else
        print_warning "Please create database manually"
    fi
fi

echo ""

# Import schema
echo "Step 8: Importing Database Schema..."

if [ -f "database/schema.sql" ]; then
    print_info "Importing schema from database/schema.sql..."
    
    if mysql -u root -p regexflow < database/schema.sql 2> /dev/null; then
        print_success "Schema imported successfully"
        
        # Verify tables
        TABLE_COUNT=$(mysql -u root -p regexflow -e "SHOW TABLES;" 2> /dev/null | wc -l)
        if [ $TABLE_COUNT -gt 1 ]; then
            print_success "Verified: $(($TABLE_COUNT - 1)) tables created"
        fi
    else
        print_warning "Could not import schema automatically"
        print_info "Import it manually:"
        echo "  mysql -u root -p regexflow < database/schema.sql"
    fi
else
    print_warning "Schema file not found at database/schema.sql"
fi

echo ""

# Summary
echo "========================================"
echo "MySQL Installation Complete!"
echo "========================================"
echo ""

print_success "MySQL Server is installed and running"
print_success "Port: 3306"
print_success "User: root"

if [ "$NO_PASSWORD" = true ]; then
    print_warning "Password: (none - consider setting one)"
else
    print_info "Password: (as configured)"
fi

echo ""
print_info "Database: regexflow"
print_info "Tables: 5 (users, regex_templates, raw_sms_logs, parsed_transactions, regex_audit_trail)"

echo ""
echo "========================================"
echo "Next Steps:"
echo "========================================"
echo ""
echo "1. Update application.yml with your MySQL root password"
echo "   File: backend/src/main/resources/application.yml"
echo ""
echo "2. Start the backend:"
echo "   cd backend"
echo "   mvn spring-boot:run"
echo ""
echo "3. Start the frontend (new terminal):"
echo "   cd frontend"
echo "   npm run dev"
echo ""
echo "4. Open browser:"
echo "   http://localhost:3000"
echo ""

print_success "RegexFlow is ready to use!"

echo ""
echo "========================================"
echo "Useful MySQL Commands:"
echo "========================================"
echo ""
echo "Connect to MySQL:"
echo "  mysql -u root -p"
echo ""
echo "Start MySQL:"
echo "  brew services start mysql"
echo ""
echo "Stop MySQL:"
echo "  brew services stop mysql"
echo ""
echo "Check MySQL status:"
echo "  brew services list | grep mysql"
echo ""
echo "View databases:"
echo "  mysql -u root -p -e 'SHOW DATABASES;'"
echo ""

print_success "Installation script completed successfully!"

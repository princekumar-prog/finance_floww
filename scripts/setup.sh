#!/bin/bash

# RegexFlow Setup Script
# This script sets up the development environment and runs the application

echo "========================================"
echo "RegexFlow Setup Script"
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

# Check prerequisites
echo "Checking prerequisites..."
echo "------------------------"

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    print_success "Java found: $JAVA_VERSION"
else
    print_error "Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    print_success "Maven found: $MVN_VERSION"
else
    print_error "Maven not found. Please install Maven."
    exit 1
fi

# Check Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v)
    print_success "Node.js found: $NODE_VERSION"
else
    print_error "Node.js not found. Please install Node.js 18 or higher."
    exit 1
fi

# Check npm
if command -v npm &> /dev/null; then
    NPM_VERSION=$(npm -v)
    print_success "npm found: $NPM_VERSION"
else
    print_error "npm not found. Please install npm."
    exit 1
fi

echo ""

# Setup Database
echo "Database Setup..."
echo "----------------"
print_info "Make sure MySQL is installed and running"
print_info "Database: regexflow"
print_info "Username: regexflow_user"
print_info "Password: regexflow_password"
echo ""
print_warning "Create database using:"
echo "  mysql -u root -p"
echo "  CREATE DATABASE regexflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo "  CREATE USER 'regexflow_user'@'localhost' IDENTIFIED BY 'regexflow_password';"
echo "  GRANT ALL PRIVILEGES ON regexflow.* TO 'regexflow_user'@'localhost';"
echo "  FLUSH PRIVILEGES;"
echo "  USE regexflow;"
echo "  SOURCE database/schema.sql;"
echo ""

read -p "Press Enter when database is ready..."

# Backend Setup
echo ""
echo "Setting up Backend..."
echo "--------------------"

cd backend

print_info "Installing backend dependencies..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    print_success "Backend dependencies installed"
else
    print_error "Failed to install backend dependencies"
    exit 1
fi

cd ..

# Frontend Setup
echo ""
echo "Setting up Frontend..."
echo "---------------------"

cd frontend

print_info "Installing frontend dependencies..."
npm install

if [ $? -eq 0 ]; then
    print_success "Frontend dependencies installed"
else
    print_error "Failed to install frontend dependencies"
    exit 1
fi

cd ..

# Create run script
echo ""
echo "Creating run scripts..."
echo "----------------------"

cat > run.sh << 'EOF'
#!/bin/bash

echo "Starting RegexFlow Application..."

# Start backend
cd backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ..

# Wait for backend to start
echo "Waiting for backend to start..."
sleep 15

# Start frontend
cd frontend
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "========================================"
echo "RegexFlow is running!"
echo "========================================"
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop both servers"

# Wait for interrupt
wait $BACKEND_PID $FRONTEND_PID
EOF

chmod +x run.sh

print_success "Setup completed successfully!"

echo ""
echo "========================================"
echo "Setup Complete!"
echo "========================================"
echo ""
print_info "To start the application, run:"
echo "  ./run.sh"
echo ""
print_info "To run backend only:"
echo "  cd backend && mvn spring-boot:run"
echo ""
print_info "To run frontend only:"
echo "  cd frontend && npm run dev"
echo ""
print_info "To run demo script:"
echo "  chmod +x sample-data/demo-script.sh"
echo "  ./sample-data/demo-script.sh"

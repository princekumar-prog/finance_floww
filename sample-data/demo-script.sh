#!/bin/bash

# RegexFlow Demo Script
# This script demonstrates the complete workflow of the RegexFlow application

BASE_URL="http://localhost:8080/api"
MAKER_TOKEN=""
CHECKER_TOKEN=""
USER_TOKEN=""

echo "========================================"
echo "RegexFlow Demo Script"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Step 1: Register Users
echo "Step 1: Registering users..."
echo "----------------------------"

# Register Maker
print_info "Registering Maker user..."
MAKER_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "maker_demo",
    "email": "maker@regexflow.com",
    "password": "maker123",
    "fullName": "Demo Maker",
    "role": "MAKER"
  }')

MAKER_TOKEN=$(echo $MAKER_RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')
if [ -n "$MAKER_TOKEN" ]; then
    print_success "Maker registered successfully"
else
    print_warning "Maker might already exist, trying login..."
    MAKER_LOGIN=$(curl -s -X POST "${BASE_URL}/auth/login" \
      -H "Content-Type: application/json" \
      -d '{"username": "maker_demo", "password": "maker123"}')
    MAKER_TOKEN=$(echo $MAKER_LOGIN | grep -o '"token":"[^"]*' | sed 's/"token":"//')
fi

# Register Checker
print_info "Registering Checker user..."
CHECKER_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "checker_demo",
    "email": "checker@regexflow.com",
    "password": "checker123",
    "fullName": "Demo Checker",
    "role": "CHECKER"
  }')

CHECKER_TOKEN=$(echo $CHECKER_RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')
if [ -n "$CHECKER_TOKEN" ]; then
    print_success "Checker registered successfully"
else
    print_warning "Checker might already exist, trying login..."
    CHECKER_LOGIN=$(curl -s -X POST "${BASE_URL}/auth/login" \
      -H "Content-Type: application/json" \
      -d '{"username": "checker_demo", "password": "checker123"}')
    CHECKER_TOKEN=$(echo $CHECKER_LOGIN | grep -o '"token":"[^"]*' | sed 's/"token":"//')
fi

# Register Normal User
print_info "Registering Normal User..."
USER_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user_demo",
    "email": "user@regexflow.com",
    "password": "user123",
    "fullName": "Demo User",
    "role": "NORMAL_USER"
  }')

USER_TOKEN=$(echo $USER_RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')
if [ -n "$USER_TOKEN" ]; then
    print_success "Normal User registered successfully"
else
    print_warning "User might already exist, trying login..."
    USER_LOGIN=$(curl -s -X POST "${BASE_URL}/auth/login" \
      -H "Content-Type: application/json" \
      -d '{"username": "user_demo", "password": "user123"}')
    USER_TOKEN=$(echo $USER_LOGIN | grep -o '"token":"[^"]*' | sed 's/"token":"//')
fi

echo ""

# Step 2: Maker creates template
echo "Step 2: Maker creates regex template..."
echo "---------------------------------------"

print_info "Creating HDFC Bank debit template..."
TEMPLATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/maker/templates" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${MAKER_TOKEN}" \
  -d '{
    "bankName": "HDFC Bank",
    "regexPattern": "Your A/c (?<account>\\w+) debited for Rs\\.(?<amount>[\\d,]+\\.?\\d*) on (?<date>[\\d-A-Za-z]+).*?Avl Bal: Rs\\.(?<balance>[\\d,]+\\.?\\d*)",
    "smsType": "DEBIT",
    "sampleSms": "Your A/c XX1234 debited for Rs.5,000.00 on 15-Jan-24 at POS 423156XXXXXX9876 at AMAZON RETAIL. Avl Bal: Rs.45,230.50. Not you? Call 18002586161",
    "description": "HDFC Bank debit transaction SMS parser"
  }')

TEMPLATE_ID=$(echo $TEMPLATE_RESPONSE | grep -o '"id":[0-9]*' | sed 's/"id"://')
print_success "Template created with ID: $TEMPLATE_ID"

echo ""

# Step 3: Test Regex
echo "Step 3: Testing regex pattern..."
echo "--------------------------------"

print_info "Testing regex with sample SMS..."
TEST_RESPONSE=$(curl -s -X POST "${BASE_URL}/maker/templates/test" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${MAKER_TOKEN}" \
  -d '{
    "regexPattern": "Your A/c (?<account>\\w+) debited for Rs\\.(?<amount>[\\d,]+\\.?\\d*) on (?<date>[\\d-A-Za-z]+).*?Avl Bal: Rs\\.(?<balance>[\\d,]+\\.?\\d*)",
    "sampleSms": "Your A/c XX1234 debited for Rs.5,000.00 on 15-Jan-24 at POS 423156XXXXXX9876 at AMAZON RETAIL. Avl Bal: Rs.45,230.50. Not you? Call 18002586161"
  }')

echo "$TEST_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TEST_RESPONSE"
print_success "Regex test completed"

echo ""

# Step 4: Submit for approval
echo "Step 4: Submitting template for approval..."
echo "-------------------------------------------"

print_info "Maker submits template..."
SUBMIT_RESPONSE=$(curl -s -X POST "${BASE_URL}/maker/templates/${TEMPLATE_ID}/submit" \
  -H "Authorization: Bearer ${MAKER_TOKEN}")

print_success "Template submitted for approval"

echo ""

# Step 5: Checker approves
echo "Step 5: Checker reviews and approves template..."
echo "------------------------------------------------"

print_info "Fetching pending templates..."
PENDING=$(curl -s -X GET "${BASE_URL}/checker/templates/pending" \
  -H "Authorization: Bearer ${CHECKER_TOKEN}")

print_info "Approving template..."
APPROVE_RESPONSE=$(curl -s -X POST "${BASE_URL}/checker/templates/${TEMPLATE_ID}/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${CHECKER_TOKEN}" \
  -d '{"comments": "Looks good! Approved."}')

print_success "Template approved successfully"

echo ""

# Step 6: User parses SMS
echo "Step 6: User uploads and parses SMS..."
echo "--------------------------------------"

print_info "Parsing bank SMS..."
PARSE_RESPONSE=$(curl -s -X POST "${BASE_URL}/user/sms/parse" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -d '{
    "smsText": "Your A/c XX1234 debited for Rs.5,000.00 on 15-Jan-24 at POS 423156XXXXXX9876 at AMAZON RETAIL. Avl Bal: Rs.45,230.50. Not you? Call 18002586161",
    "senderHeader": "HDFCBK"
  }')

echo "$PARSE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$PARSE_RESPONSE"
print_success "SMS parsed successfully"

echo ""

# Step 7: View transaction history
echo "Step 7: Viewing transaction history..."
echo "--------------------------------------"

print_info "Fetching user transactions..."
HISTORY_RESPONSE=$(curl -s -X GET "${BASE_URL}/user/transactions?page=0&size=10" \
  -H "Authorization: Bearer ${USER_TOKEN}")

echo "$HISTORY_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$HISTORY_RESPONSE"
print_success "Transaction history retrieved"

echo ""
echo "========================================"
echo "Demo completed successfully!"
echo "========================================"
echo ""
print_info "You can now:"
echo "  - Login to the frontend at http://localhost:3000"
echo "  - Maker credentials: maker_demo / maker123"
echo "  - Checker credentials: checker_demo / checker123"
echo "  - User credentials: user_demo / user123"

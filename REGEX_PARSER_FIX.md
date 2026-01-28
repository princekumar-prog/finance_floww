# Regex and SMS Parsing Fix

## Issue

The regex parser was only extracting named groups that were in a predefined list. This meant that if a user created a regex pattern with custom named groups (e.g., `(?<customField>...)`), those groups would not be extracted, even though they matched.

### Example of the Problem

If someone created a regex like:
```regex
Transaction of Rs\.(?<transactionAmount>\d+) at (?<storeName>[A-Z]+)
```

The fields `transactionAmount` and `storeName` would NOT be extracted because they weren't in the predefined list of field names like "amount", "balance", "account", etc.

## Root Cause

In `RegexParser.java`, the `extractFields()` method had a hardcoded array of possible field names:

```java
String[] possibleFields = {
    "amount", "balance", "bank", "bankName", "bankname",
    "date", "time", "datetime",
    "account", "accountId", "accountNumber", "acct",
    "merchant", "merchantName", "payee", "vendor",
    "ref", "refNo", "refno", "referenceNumber", "reference",
    "mode", "type", "transactionType", "txnType",
    "upi", "card", "status", "message"
};
```

It only tried to extract these specific field names. If your regex used different names, they were ignored.

## Solution

The fix dynamically extracts ALL named group names from the regex pattern itself, regardless of what they're named. This is done by:

1. Parsing the regex pattern string to find all named groups using the pattern `(?<name>...)`
2. Extracting all found named groups from the matched text
3. Falling back to numbered groups if no named groups are found

### Changes Made

**File: `backend/src/main/java/com/regexflow/parser/RegexParser.java`**

1. Updated `extractFields()` method to call a new helper method that extracts named group names
2. Added `extractNamedGroupNames()` method that parses the regex pattern to find all named groups
3. Both methods work with Java 17 (no need for Java 20's `namedGroups()` API)

## Testing

### Manual Test Results

```
Test 1: HDFC Bank Debit
✓ Pattern matched! Extracted fields:
  date = 15-Jan-24
  amount = 5,000.00
  balance = 45,230.50
  merchant = AMAZON RETAIL
  account = XX1234

Test 2: HDFC Bank Credit
✓ Pattern matched! Extracted fields:
  date = 16-Jan-24
  amount = 10,000.00
  balance = 55,230.50
  account = XX1234

Test 3: SBI Debit
✓ Pattern matched! Extracted fields:
  date = 17-Jan-24
  amount = 3,500.00
  ref = 123456789012
  account = **9876

Test 4: Custom Named Groups (not in predefined list)
✓ Pattern matched! Extracted fields:
  transactionAmount = 1500
  storeName = STARBUCKS
  transactionDate = 2024-01-20
  cardLastDigits = 4567
```

**Test 4** demonstrates the fix - it successfully extracts custom field names that weren't in the original predefined list.

### Unit Test Results

- **RegexParserTest**: 10/10 tests passed ✓
- **SmsParserTest**: 3/3 tests passed ✓

## Impact

This fix enables:
1. **Flexibility**: Users can create regex templates with any named group names they want
2. **No Limitations**: No longer restricted to predefined field names
3. **Backward Compatibility**: Existing patterns continue to work as before
4. **Better User Experience**: SMS parsing will extract more fields from custom patterns

## Usage

Users can now create templates with custom named groups:

```java
// Example: Custom payment app SMS
String pattern = "(?<appName>[A-Z]+) payment of (?<transactionAmount>[\\d,]+) to (?<recipientName>[A-Za-z\\s]+) successful";
String sms = "PHONEPE payment of 500 to John Doe successful";

// Now extracts: appName=PHONEPE, transactionAmount=500, recipientName=John Doe
```

## Files Modified

1. `backend/src/main/java/com/regexflow/parser/RegexParser.java`
   - Updated `extractFields()` method
   - Added `extractNamedGroupNames()` helper method

## Backward Compatibility

✓ All existing regex patterns continue to work
✓ No breaking changes to the API
✓ All existing tests pass

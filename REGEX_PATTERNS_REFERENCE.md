# Regex Patterns Reference - All Correct Patterns

## Common Errors to Avoid ❌

### 1. Character Class Errors
- ❌ `[*\|d]` - WRONG (using pipe character)
- ✅ `[*\d]` - CORRECT (matches asterisk and digits)

### 2. Escape Characters
- ❌ `Rs.(?<amount>` - WRONG (dot not escaped)
- ✅ `Rs\.(?<amount>` - CORRECT (dot escaped)

### 3. Backslash in Character Classes
- ❌ `[\d,]+` - Might appear wrong in UI
- ✅ `[\d,]+` - Copy exactly as shown

---

## All Correct Patterns by Bank

### 1. HDFC Bank - Debit
**SMS Sample:**
```
Your A/c XX1234 debited for Rs.5,000.00 on 15-Jan-24 at POS 423156XXXXXX9876 at AMAZON RETAIL. Avl Bal: Rs.45,230.50. Not you? Call 18002586161
```

**Correct Pattern:**
```regex
Your A/c (?<account>\w+) debited for Rs\.(?<amount>[\d,]+\.?\d*) on (?<date>[\d-A-Za-z]+).*?at (?<merchant>[A-Z\s]+)\. Avl Bal: Rs\.(?<balance>[\d,]+\.?\d*)
```

**Extracts:**
- account: XX1234
- amount: 5,000.00
- date: 15-Jan-24
- merchant: AMAZON RETAIL
- balance: 45,230.50

---

### 2. HDFC Bank - Credit
**SMS Sample:**
```
Rs.10,000.00 credited to A/c XX1234 on 16-Jan-24 by A/c linked to VPA john@paytm (UPI Ref No 401234567890). Avl Bal Rs.55,230.50
```

**Correct Pattern:**
```regex
Rs\.(?<amount>[\d,]+\.?\d*) credited to A/c (?<account>\w+) on (?<date>[\d-A-Za-z]+).*?Avl Bal Rs\.(?<balance>[\d,]+\.?\d*)
```

**Extracts:**
- amount: 10,000.00
- account: XX1234
- date: 16-Jan-24
- balance: 55,230.50

---

### 3. SBI - Debit
**SMS Sample:**
```
Dear Customer, INR 3,500.00 debited from A/c **9876 on 17-Jan-24 to VPA paytm@paytm Ref No 123456789012. If not done by you, please call 1800112211
```

**Correct Pattern:**
```regex
INR (?<amount>[\d,]+\.?\d*) debited from A/c (?<account>[*\d]+) on (?<date>[\d-A-Za-z]+).*?Ref No (?<ref>\d+)
```

**⚠️ Common Error:** Using `[*\|d]` instead of `[*\d]`

**Extracts:**
- amount: 3,500.00
- account: **9876
- date: 17-Jan-24
- ref: 123456789012

---

### 4. SBI - Credit
**SMS Sample:**
```
SBI A/c **9876 credited with INR 25,000.00 on 18-Jan-24 by NEFT-SBIH24018123456. Avl bal INR 78,500.00
```

**Correct Pattern:**
```regex
SBI A/c (?<account>[*\d]+) credited with INR (?<amount>[\d,]+\.?\d*) on (?<date>[\d-A-Za-z]+).*?Avl bal INR (?<balance>[\d,]+\.?\d*)
```

**⚠️ Common Error:** Using `[*\|d]` instead of `[*\d]`

**Extracts:**
- account: **9876
- amount: 25,000.00
- date: 18-Jan-24
- balance: 78,500.00

---

### 5. ICICI Bank - Debit
**SMS Sample:**
```
ICICI Bank A/c XX5678 debited with Rs 2,800.50 on 19Jan24. Info: UPI-PHONEPE. Avl Bal Rs 32,199.50. To dispute, SMS BLOCK 5678 to 9215676766
```

**Correct Pattern:**
```regex
ICICI Bank A/c (?<account>\w+) debited with Rs (?<amount>[\d,]+\.?\d*) on (?<date>\d+[A-Za-z]+\d+)\. Info: (?<mode>[A-Z-]+)\. Avl Bal Rs (?<balance>[\d,]+\.?\d*)
```

**Extracts:**
- account: XX5678
- amount: 2,800.50
- date: 19Jan24
- mode: UPI-PHONEPE
- balance: 32,199.50

---

### 6. ICICI Bank - Credit
**SMS Sample:**
```
Rs 15,000 cr to ICICI Bank A/c XX5678 on 20Jan24 from SALARY-XYZ Corp. Bal: Rs 47,199.50. Download app: icicibank.com/app
```

**Correct Pattern:**
```regex
Rs (?<amount>[\d,]+\.?\d*) cr to ICICI Bank A/c (?<account>\w+) on (?<date>\d+[A-Za-z]+\d+) from (?<merchant>[A-Z\s-]+)\. Bal: Rs (?<balance>[\d,]+\.?\d*)
```

**Extracts:**
- amount: 15,000
- account: XX5678
- date: 20Jan24
- merchant: SALARY-XYZ Corp
- balance: 47,199.50

---

### 7. Axis Bank - Debit
**SMS Sample:**
```
Dear Customer, Rs. 1,250.00 is debited from your A/c **3456 on 21-01-2024 for txn at SWIGGY via Debit Card ending 7890. Avl Bal is Rs. 23,750.00
```

**Correct Pattern:**
```regex
Rs\. (?<amount>[\d,]+\.?\d*) is debited from your A/c (?<account>[*\d]+) on (?<date>[\d-]+) for txn at (?<merchant>[A-Z]+).*?Avl Bal is Rs\. (?<balance>[\d,]+\.?\d*)
```

**⚠️ Common Error:** Using `[*\|d]` instead of `[*\d]`

**Extracts:**
- amount: 1,250.00
- account: **3456
- date: 21-01-2024
- merchant: SWIGGY
- balance: 23,750.00

---

### 8. Axis Bank - Credit
**SMS Sample:**
```
Rs. 8,000.00 credited to your Axis Bank A/c **3456 on 22-01-2024. Ref No: AXI123456789. Available Balance: Rs. 31,750.00
```

**Correct Pattern:**
```regex
Rs\. (?<amount>[\d,]+\.?\d*) credited to your Axis Bank A/c (?<account>[*\d]+) on (?<date>[\d-]+)\. Ref No: (?<ref>[A-Z\d]+)\. Available Balance: Rs\. (?<balance>[\d,]+\.?\d*)
```

**⚠️ Common Error:** Using `[*\|d]` instead of `[*\d]`

**Extracts:**
- amount: 8,000.00
- account: **3456
- date: 22-01-2024
- ref: AXI123456789
- balance: 31,750.00

---

### 9. Kotak Mahindra - Debit
**SMS Sample:**
```
INR 4,500.00 debited from your A/c no. XXXXXX2345 on 23-JAN-24. Towards Bill Payment-ELECTRICITY. Avl Bal: INR 56,500.00. Call 18602662666
```

**Correct Pattern:**
```regex
INR (?<amount>[\d,]+\.?\d*) debited from your A/c no\. (?<account>[X\d]+) on (?<date>[\d-A-Z]+)\. Towards Bill Payment-(?<merchant>[A-Z]+)\. Avl Bal: INR (?<balance>[\d,]+\.?\d*)
```

**Extracts:**
- amount: 4,500.00
- account: XXXXXX2345
- date: 23-JAN-24
- merchant: ELECTRICITY
- balance: 56,500.00

---

### 10. Kotak Mahindra - Credit
**SMS Sample:**
```
Your A/c XXXXXX2345 is credited with INR 20,000.00 on 24-JAN-24. Transaction Ref: NEFT-KOT240124123. New Bal: INR 76,500.00
```

**Correct Pattern:**
```regex
Your A/c (?<account>[X\d]+) is credited with INR (?<amount>[\d,]+\.?\d*) on (?<date>[\d-A-Z]+)\. Transaction Ref: (?<ref>[A-Z\d-]+)\. New Bal: INR (?<balance>[\d,]+\.?\d*)
```

**Extracts:**
- account: XXXXXX2345
- amount: 20,000.00
- date: 24-JAN-24
- ref: NEFT-KOT240124123
- balance: 76,500.00

---

## Quick Error Checker

### ❌ If you see this error: "Illegal character range"
**Problem:** You typed `[*\|d]` or similar
**Solution:** Replace with `[*\d]` - Use backslash-d (\d) not backslash-pipe (\|)

### ❌ If you see this error: "Invalid escape sequence"
**Problem:** Missing backslashes before special characters
**Solution:** Escape dots: `\.` not just `.`

### ❌ If pattern doesn't match
**Problem:** Wrong character class or missing escape
**Solution:** Copy the exact pattern from this document

---

## How to Use

1. **Find your bank** in the list above
2. **Copy the exact pattern** (use Ctrl+C / Cmd+C)
3. **Paste into Regex Pattern field** in the UI
4. **Paste the corresponding SMS** into Sample SMS field
5. **Click "Test Regex"** - should show "Match Found!"

---

## Character Class Quick Reference

| Pattern | Matches | Example |
|---------|---------|---------|
| `\d` | Any digit 0-9 | `\d+` matches "123" |
| `\w` | Word character (a-z, A-Z, 0-9, _) | `\w+` matches "ABC123" |
| `[*\d]` | Asterisk OR digit | `[*\d]+` matches "**9876" |
| `[X\d]` | Letter X OR digit | `[X\d]+` matches "XXXXXX2345" |
| `[A-Z]` | Uppercase letters | `[A-Z]+` matches "AMAZON" |
| `[A-Za-z]` | Any letter | `[A-Za-z]+` matches "Jan" |
| `[\d,]` | Digit OR comma | `[\d,]+` matches "5,000" |
| `\.` | Literal dot | `Rs\.` matches "Rs." |

---

## Still Getting Errors?

1. Make sure you're copying the ENTIRE pattern
2. Don't add extra spaces at the beginning or end
3. Use copy-paste, don't type manually
4. Check for invisible characters
5. If all else fails, delete the pattern and paste again fresh

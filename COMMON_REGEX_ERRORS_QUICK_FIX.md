# Common Regex Errors - Quick Fix Guide

## Top 3 Most Common Errors (99% of issues!)

### 1. ❌ ERROR: "Illegal character range near index"

**You probably typed:**
```regex
(?<account>[*\|d]+)
```

**Should be:**
```regex
(?<account>[*\d]+)
```

**Why?** You typed `\|` (backslash pipe) instead of `\d` (backslash d)
- `\|` = WRONG - means "pipe character" 
- `\d` = CORRECT - means "any digit"

---

### 2. ❌ ERROR: Pattern doesn't match

**Problem:** Forgot to escape the dot

**Wrong:**
```regex
Rs.(?<amount>
```

**Correct:**
```regex
Rs\.(?<amount>
```

**Why?** In regex, `.` means "any character". Use `\.` to match a literal dot.

---

### 3. ❌ ERROR: "Unclosed character class"

**You probably typed:**
```regex
(?<amount>[\d,]+.?\d*)
```

**Should be:**
```regex
(?<amount>[\d,]+\.?\d*)
```

**Why?** Inside `[...]` you need `\.` not just `.`

---

## Copy-Paste These Correct Patterns

### For SBI Credit SMS:
```regex
SBI A/c (?<account>[*\d]+) credited with INR (?<amount>[\d,]+\.?\d*) on (?<date>[\d-A-Za-z]+).*?Avl bal INR (?<balance>[\d,]+\.?\d*)
```

### For SBI Debit SMS:
```regex
INR (?<amount>[\d,]+\.?\d*) debited from A/c (?<account>[*\d]+) on (?<date>[\d-A-Za-z]+).*?Ref No (?<ref>\d+)
```

### For Axis Bank Debit SMS:
```regex
Rs\. (?<amount>[\d,]+\.?\d*) is debited from your A/c (?<account>[*\d]+) on (?<date>[\d-]+) for txn at (?<merchant>[A-Z]+).*?Avl Bal is Rs\. (?<balance>[\d,]+\.?\d*)
```

### For Axis Bank Credit SMS:
```regex
Rs\. (?<amount>[\d,]+\.?\d*) credited to your Axis Bank A/c (?<account>[*\d]+) on (?<date>[\d-]+)\. Ref No: (?<ref>[A-Z\d]+)\. Available Balance: Rs\. (?<balance>[\d,]+\.?\d*)
```

---

## How to Fix

### Step 1: Clear the Pattern Field
Delete everything in the "Regex Pattern" field

### Step 2: Copy Correct Pattern
From the list above, find your bank and **copy the entire pattern** (Ctrl+C or Cmd+C)

### Step 3: Paste
Paste into the Regex Pattern field (Ctrl+V or Cmd+V)

### Step 4: Test
Click "Test Regex" - you should see "✓ Match Found!"

---

## Still Not Working?

### Check These:

1. ✅ Are you using the sample SMS that matches the pattern?
2. ✅ Did you copy the ENTIRE pattern (no missing characters)?
3. ✅ No extra spaces at beginning/end?
4. ✅ Using the correct bank pattern for your SMS?

### Common Copy-Paste Issues:

- ❌ Selected pattern but missed first/last character
- ❌ Added extra space when pasting
- ❌ Mixed patterns from different banks
- ❌ Typed instead of copy-paste (introduces typos)

---

## Character Cheat Sheet

| Character | What It Means |
|-----------|---------------|
| `\d` | Any digit (0-9) |
| `\.` | Literal dot (.) |
| `[*\d]` | Asterisk (*) OR digit |
| `[X\d]` | Letter X OR digit |
| `\w` | Word character |
| `\s` | Space |

---

## Remember!

- **ALWAYS** use `\d` (backslash-d) not `\|d` (backslash-pipe-d)
- **ALWAYS** escape dots: `\.` not `.`
- **ALWAYS** copy-paste, never type manually
- **CHECK** sample SMS matches the bank pattern you're using

---

## Need More Help?

See complete reference: `REGEX_PATTERNS_REFERENCE.md`

# Personal Finance Manager - Usage Guide

## How to Access

### For Users (NORMAL_USER role)

1. **Login** to the application as a NORMAL_USER
2. You'll be redirected to the **User Dashboard**
3. Click the **"📊 View Personal Finance Manager"** button
4. You'll be taken to the Personal Finance Manager interface

### Direct URL
Once logged in as a NORMAL_USER, you can also access directly via:
```
http://localhost:5173/finance
```

## Features & How to Use

### 1. Summary Dashboard

At the top of the page, you'll see three cards:

- **Income Card**
  - Shows total income from CREDIT transactions
  - Displays percentage change (green = increase, red = decrease)
  
- **Expense Card**
  - Shows total expenses from DEBIT transactions
  - Displays percentage change
  
- **Saving Card**
  - Shows net savings (Income - Expense)
  - Displays percentage change

### 2. Search Transactions

Use the search bar to find transactions:
```
Type in the search box to filter by:
- Merchant/Payee name (e.g., "Spotify", "Amazon")
- Transaction type (e.g., "CREDIT", "DEBIT")
- Bank name (e.g., "HDFC", "SBI")
```

The results update in real-time as you type!

### 3. Filter Transactions

Click the **filter icon** (funnel symbol) to open the advanced filter panel:

**Available Filters:**
- **Bank Name**: Filter by specific bank
- **Transaction Type**: Select Credit, Debit, or All
- **Start Date**: Filter transactions from this date
- **End Date**: Filter transactions until this date
- **Min Amount**: Show only transactions above this amount
- **Max Amount**: Show only transactions below this amount

**Actions:**
- **Apply Filters**: Apply the selected filters
- **Clear All**: Reset all filters and search

### 4. Browse by Category

Use the tabs to quickly filter transactions:
- **All**: Shows all transactions
- **Savings**: Shows income transactions (credits)
- **Income**: Shows income transactions (credits)
- **Expenses**: Shows expense transactions (debits)

### 5. View Transaction Details

The transaction table shows:

| Column | Description |
|--------|-------------|
| **Ref ID** | Reference number or transaction ID |
| **Transaction Date** | Date and time of transaction (e.g., "16 Jun 2025, 04:20 PM") |
| **From** | Merchant/Payee name with bank icon and name |
| **Type** | Badge showing Received/Transfer/Subscription |
| **Amount** | Transaction amount (Green = +, Red = -) |

### 6. Navigate

- **Back Button**: Top-left corner, returns to User Dashboard
- **Date Range**: Top-right corner (currently displays current week)

## Tips & Tricks

### Quick Search
- Search for "received" to see all income
- Search for "transfer" to see all transfers
- Search by bank name to see all transactions from that bank

### Combine Filters
You can combine multiple filters for precise results:
```
Example: Find all HDFC transactions between ₹1000-₹5000 in June
1. Click filter icon
2. Enter "HDFC" in Bank Name
3. Select date range (June 1 - June 30)
4. Enter 1000 in Min Amount
5. Enter 5000 in Max Amount
6. Click "Apply Filters"
```

### Reset View
To start fresh:
1. Click the filter icon
2. Click "Clear All"
3. Or reload the page

### Color Guide
- **Green amounts (+)**: Money received (income)
- **Red amounts (-)**: Money spent (expenses)
- **Green percentage**: Positive change
- **Red percentage**: Negative change

## Transaction Type Badges

- **Received**: Income/Credit transactions
- **Transfer**: Debit transactions
- **Subscription**: Recurring payments (if applicable)

## Mobile Usage

The interface is fully responsive:
- Summary cards stack vertically
- Transaction table scrolls horizontally
- Filter panel adjusts for mobile screens
- Touch-friendly buttons and inputs

## Keyboard Shortcuts

- **Search**: Click search box or use Tab to navigate
- **Enter**: Apply search
- **Esc**: Close filter panel (when open)

## Data Refresh

- Data loads automatically when you open the page
- Use pagination at the bottom for more transactions
- Apply new filters to refresh the view

## Troubleshooting

### No Transactions Showing
- Upload some SMS messages first from the User Dashboard
- Check if filters are active (click "Clear All")
- Verify you have transactions in the system

### Search Not Working
- Make sure you're typing at least 2-3 characters
- Try different search terms
- Clear filters first

### Filter Panel Not Opening
- Click the filter icon (funnel symbol)
- Refresh the page if needed
- Check browser console for errors

## Data Privacy

- All transactions are stored securely
- Only you can see your transaction history
- Data is not shared with other users

## Support

For issues or questions:
1. Check the User Dashboard for SMS upload issues
2. Verify your transactions are being parsed correctly
3. Contact your system administrator

---

**Enjoy managing your finances with the Personal Finance Manager! 💰📊**

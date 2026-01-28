import React, { useState, useEffect } from 'react';
import { userAPI } from '../services/api';
import { toast } from 'react-toastify';
import { format } from 'date-fns';
import './PersonalFinanceManager.css';

const PersonalFinanceManager = () => {
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeTab, setActiveTab] = useState('All');
  const [showFilters, setShowFilters] = useState(false);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [dateRange, setDateRange] = useState({
    startDate: '',
    endDate: ''
  });
  const datePickerRef = React.useRef(null);
  
  // Summary stats
  const [stats, setStats] = useState({
    income: 0,
    expense: 0,
    saving: 0,
    incomeChange: 0,
    expenseChange: 0,
    savingChange: 0
  });

  // Filters
  const [filters, setFilters] = useState({
    bankName: '',
    startDate: '',
    endDate: '',
    minAmount: '',
    maxAmount: '',
    transactionType: ''
  });

  useEffect(() => {
    loadTransactions();
  }, [currentPage]);

  useEffect(() => {
    applyLocalFilters();
  }, [searchTerm, activeTab, transactions, dateRange]);

  // Close date picker when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (datePickerRef.current && !datePickerRef.current.contains(event.target)) {
        setShowDatePicker(false);
      }
    };

    if (showDatePicker) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showDatePicker]);

  const loadTransactions = async () => {
    try {
      setLoading(true);
      const response = await userAPI.getTransactionHistory(currentPage, 100);
      const data = response.data.data;
      setTransactions(data.content || []);
      setTotalPages(data.totalPages);
      calculateStats(data.content || []);
    } catch (error) {
      toast.error('Failed to load transactions');
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = (txns) => {
    const income = txns
      .filter(t => t.transactionType === 'CREDIT')
      .reduce((sum, t) => sum + (t.amount || 0), 0);
    
    const expense = txns
      .filter(t => t.transactionType === 'DEBIT')
      .reduce((sum, t) => sum + (t.amount || 0), 0);
    
    const saving = income - expense;

    // Mock percentage changes (in real app, compare with previous period)
    setStats({
      income,
      expense,
      saving,
      incomeChange: -0.12,
      expenseChange: 1.4,
      savingChange: 2.2
    });
  };

  const applyLocalFilters = () => {
    let filtered = [...transactions];

    // Filter by tab
    if (activeTab === 'Income') {
      filtered = filtered.filter(t => t.transactionType === 'CREDIT');
    } else if (activeTab === 'Expenses') {
      filtered = filtered.filter(t => t.transactionType === 'DEBIT');
    }

    // Filter by date range
    if (dateRange.startDate || dateRange.endDate) {
      filtered = filtered.filter(t => {
        if (!t.transactionDate) return false;
        const txnDate = new Date(t.transactionDate);
        
        if (dateRange.startDate && dateRange.endDate) {
          const start = new Date(dateRange.startDate);
          const end = new Date(dateRange.endDate);
          end.setHours(23, 59, 59, 999); // Include full end date
          return txnDate >= start && txnDate <= end;
        } else if (dateRange.startDate) {
          const start = new Date(dateRange.startDate);
          return txnDate >= start;
        } else if (dateRange.endDate) {
          const end = new Date(dateRange.endDate);
          end.setHours(23, 59, 59, 999);
          return txnDate <= end;
        }
        return true;
      });
    }

    // Filter by search term (search in merchant name or transaction type)
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(t => 
        (t.merchantOrPayee && t.merchantOrPayee.toLowerCase().includes(term)) ||
        (t.transactionType && t.transactionType.toLowerCase().includes(term)) ||
        (t.bankName && t.bankName.toLowerCase().includes(term))
      );
    }

    setFilteredTransactions(filtered);
  };

  const handleFilterChange = (e) => {
    setFilters({
      ...filters,
      [e.target.name]: e.target.value,
    });
  };

  const applyAdvancedFilters = async () => {
    try {
      setLoading(true);
      const filterParams = {
        ...filters,
        page: 0,
        size: 100,
      };
      
      // Remove empty filters
      Object.keys(filterParams).forEach(key => {
        if (!filterParams[key]) delete filterParams[key];
      });
      
      const response = await userAPI.getFilteredTransactions(filterParams);
      const data = response.data.data;
      setTransactions(data.content || []);
      setTotalPages(data.totalPages);
      setCurrentPage(0);
      calculateStats(data.content || []);
      setShowFilters(false);
      toast.success('Filters applied');
    } catch (error) {
      toast.error('Failed to apply filters');
    } finally {
      setLoading(false);
    }
  };

  const clearFilters = () => {
    setFilters({
      bankName: '',
      startDate: '',
      endDate: '',
      minAmount: '',
      maxAmount: '',
      transactionType: ''
    });
    setSearchTerm('');
    setActiveTab('All');
    setShowFilters(false);
    setDateRange({ startDate: '', endDate: '' });
    loadTransactions();
  };

  const applyDateRange = () => {
    setShowDatePicker(false);
    toast.success('Date range applied');
  };

  const clearDateRange = () => {
    setDateRange({ startDate: '', endDate: '' });
    setShowDatePicker(false);
  };

  const formatDateRange = () => {
    if (dateRange.startDate && dateRange.endDate) {
      const start = new Date(dateRange.startDate);
      const end = new Date(dateRange.endDate);
      return `${format(start, 'dd MMM')} - ${format(end, 'dd MMM')}`;
    } else if (dateRange.startDate) {
      const start = new Date(dateRange.startDate);
      return `From ${format(start, 'dd MMM')}`;
    } else if (dateRange.endDate) {
      const end = new Date(dateRange.endDate);
      return `Until ${format(end, 'dd MMM')}`;
    }
    return 'Select Date Range';
  };

  const formatCurrency = (amount) => {
    if (!amount && amount !== 0) return 'N/A';
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
    }).format(amount);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return format(date, 'dd MMM yyyy, hh:mm a');
    } catch {
      return 'N/A';
    }
  };

  const getAmountClass = (type) => {
    return type === 'CREDIT' ? 'amount-positive' : 'amount-negative';
  };

  const getAmountPrefix = (type) => {
    return type === 'CREDIT' ? '+ ' : '- ';
  };

  const getTransactionType = (txn) => {
    // Check mode first
    if (txn.mode) {
      const mode = txn.mode.toUpperCase();
      if (mode.includes('UPI')) return 'UPI';
      if (mode.includes('NEFT') || mode.includes('RTGS') || mode.includes('IMPS')) return 'Transfer';
      if (mode.includes('ATM')) return 'ATM';
      if (mode.includes('CARD') || mode.includes('DEBIT')) return 'Card';
    }

    // Check merchant/payee
    if (txn.merchantOrPayee) {
      const merchant = txn.merchantOrPayee.toLowerCase();
      if (merchant.includes('salary')) return 'Salary';
      if (merchant.includes('amazon') || merchant.includes('flipkart') || merchant.includes('shop')) return 'Shopping';
      if (merchant.includes('electric') || merchant.includes('bill')) return 'Bill Payment';
      if (merchant.includes('credit')) return 'Credit';
    }

    // Check reference number patterns
    if (txn.referenceNumber) {
      const ref = txn.referenceNumber.toUpperCase();
      if (ref.includes('UPI')) return 'UPI';
      if (ref.includes('NEFT')) return 'Transfer';
    }

    // Default based on transaction type
    return txn.transactionType === 'CREDIT' ? 'Received' : 'Transfer';
  };

  return (
    <div className="finance-manager">
      <div className="finance-container">
        {/* Header */}
        <div className="finance-header">
          <h1 className="finance-title">Personal Finance Manager</h1>
          <div className="date-range-selector" ref={datePickerRef}>
            <button 
              className="date-range-btn"
              onClick={() => setShowDatePicker(!showDatePicker)}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
              </svg>
              {formatDateRange()}
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="6 9 12 15 18 9"></polyline>
              </svg>
            </button>
            
            {showDatePicker && (
              <div className="date-picker-dropdown">
                <div className="date-picker-header">
                  <h4>Select Date Range</h4>
                  <button 
                    className="close-date-picker"
                    onClick={() => setShowDatePicker(false)}
                  >
                    ×
                  </button>
                </div>
                <div className="date-picker-body">
                  <div className="date-input-group">
                    <label>Start Date</label>
                    <input
                      type="date"
                      value={dateRange.startDate}
                      onChange={(e) => setDateRange({...dateRange, startDate: e.target.value})}
                      className="date-input"
                    />
                  </div>
                  <div className="date-input-group">
                    <label>End Date</label>
                    <input
                      type="date"
                      value={dateRange.endDate}
                      onChange={(e) => setDateRange({...dateRange, endDate: e.target.value})}
                      className="date-input"
                    />
                  </div>
                </div>
                <div className="date-picker-actions">
                  <button className="btn btn-secondary btn-sm" onClick={clearDateRange}>
                    Clear
                  </button>
                  <button className="btn btn-primary btn-sm" onClick={applyDateRange}>
                    Apply
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Summary Cards */}
        <div className="summary-cards">
          <div className="summary-card">
            <div className="card-icon income-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <polyline points="19 12 12 19 5 12"></polyline>
              </svg>
            </div>
            <div className="card-content">
              <div className="card-header">
                <span className="card-label">Income</span>
              </div>
              <div className="card-value">{formatCurrency(stats.income)}</div>
            </div>
          </div>

          <div className="summary-card">
            <div className="card-icon expense-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="12" y1="19" x2="12" y2="5"></line>
                <polyline points="5 12 12 5 19 12"></polyline>
              </svg>
            </div>
            <div className="card-content">
              <div className="card-header">
                <span className="card-label">Expense</span>
              </div>
              <div className="card-value">{formatCurrency(stats.expense)}</div>
            </div>
          </div>

          <div className="summary-card">
            <div className="card-icon saving-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                <line x1="1" y1="10" x2="23" y2="10"></line>
              </svg>
            </div>
            <div className="card-content">
              <div className="card-header">
                <span className="card-label">Saving</span>
              </div>
              <div className="card-value">{formatCurrency(stats.saving)}</div>
            </div>
          </div>
        </div>

        {/* Transactions Section */}
        <div className="transactions-section">
          <div className="transactions-header">
            <h2 className="transactions-title">Transactions</h2>
            <div className="transactions-controls">
              <div className="search-box">
                <svg className="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="m21 21-4.35-4.35"></path>
                </svg>
                <input
                  type="text"
                  className="search-input"
                  placeholder="Search transaction..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <button 
                className="filter-btn"
                onClick={() => setShowFilters(!showFilters)}
                title="Filter transactions"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
                </svg>
              </button>
            </div>
          </div>

          {/* Filter Panel */}
          {showFilters && (
            <div className="filter-panel">
              <div className="filter-panel-header">
                <h3>Filter Transactions</h3>
                <button 
                  className="close-filter-btn"
                  onClick={() => setShowFilters(false)}
                >
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              </div>
              <div className="filter-grid">
                <div className="filter-group">
                  <label className="filter-label">Bank Name</label>
                  <input
                    type="text"
                    name="bankName"
                    className="filter-input"
                    placeholder="Filter by bank"
                    value={filters.bankName}
                    onChange={handleFilterChange}
                  />
                </div>
                <div className="filter-group">
                  <label className="filter-label">Transaction Type</label>
                  <select
                    name="transactionType"
                    className="filter-input"
                    value={filters.transactionType}
                    onChange={handleFilterChange}
                  >
                    <option value="">All Types</option>
                    <option value="CREDIT">Credit</option>
                    <option value="DEBIT">Debit</option>
                  </select>
                </div>
                <div className="filter-group">
                  <label className="filter-label">Start Date</label>
                  <input
                    type="date"
                    name="startDate"
                    className="filter-input"
                    value={filters.startDate}
                    onChange={handleFilterChange}
                  />
                </div>
                <div className="filter-group">
                  <label className="filter-label">End Date</label>
                  <input
                    type="date"
                    name="endDate"
                    className="filter-input"
                    value={filters.endDate}
                    onChange={handleFilterChange}
                  />
                </div>
                <div className="filter-group">
                  <label className="filter-label">Min Amount</label>
                  <input
                    type="number"
                    name="minAmount"
                    className="filter-input"
                    placeholder="Min"
                    value={filters.minAmount}
                    onChange={handleFilterChange}
                  />
                </div>
                <div className="filter-group">
                  <label className="filter-label">Max Amount</label>
                  <input
                    type="number"
                    name="maxAmount"
                    className="filter-input"
                    placeholder="Max"
                    value={filters.maxAmount}
                    onChange={handleFilterChange}
                  />
                </div>
              </div>
              <div className="filter-actions">
                <button className="btn btn-primary" onClick={applyAdvancedFilters}>
                  Apply Filters
                </button>
                <button className="btn btn-secondary" onClick={clearFilters}>
                  Clear All
                </button>
              </div>
            </div>
          )}

          {/* Transaction Tabs */}
          <div className="transaction-tabs">
            {['All', 'Income', 'Expenses'].map(tab => (
              <button
                key={tab}
                className={`tab-btn ${activeTab === tab ? 'active' : ''}`}
                onClick={() => setActiveTab(tab)}
              >
                {tab}
              </button>
            ))}
          </div>

          {/* Transaction Table */}
          {loading ? (
            <div className="loader">
              <div className="spinner"></div>
            </div>
          ) : filteredTransactions.length === 0 ? (
            <div className="empty-state">
              <p>No transactions found</p>
            </div>
          ) : (
            <div className="transactions-table-wrapper">
              <table className="finance-table">
                <thead>
                  <tr>
                    <th>Transaction Date</th>
                    <th>Ref ID</th>
                    <th>From</th>
                    <th>Bank</th>
                    <th>Account No</th>
                    <th>Type</th>
                    <th className="text-right">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTransactions.map((txn, index) => (
                    <tr key={txn.id || index}>
                      <td className="txn-date">{formatDate(txn.transactionDate)}</td>
                      <td className="ref-id">{txn.referenceNumber || `TXN${txn.id}`}</td>
                      <td className="txn-from">
                        <div className="from-info">
                          <div className="from-icon">
                            {txn.transactionType === 'CREDIT' ? '💰' : '🏦'}
                          </div>
                          <div className="from-details">
                            <div className="from-name">{txn.merchantOrPayee || txn.bankName || 'Unknown'}</div>
                            <div className="from-bank">{txn.bankName}</div>
                          </div>
                        </div>
                      </td>
                      <td>{txn.bankName || '-'}</td>
                      <td>{txn.accountId || '-'}</td>
                      <td className="txn-type">{getTransactionType(txn)}</td>
                      <td className={`text-right ${getAmountClass(txn.transactionType)}`}>
                        {getAmountPrefix(txn.transactionType)}{formatCurrency(Math.abs(txn.amount || 0))}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              <button
                className="btn btn-secondary btn-sm"
                onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                disabled={currentPage === 0}
              >
                Previous
              </button>
              <span className="page-info">
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                className="btn btn-secondary btn-sm"
                onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={currentPage === totalPages - 1}
              >
                Next
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default PersonalFinanceManager;

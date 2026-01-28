import React, { useState } from 'react';
import { userAPI } from '../services/api';
import { toast } from 'react-toastify';
import PersonalFinanceManager from './PersonalFinanceManager';
import './UserDashboard.css';

const UserDashboard = () => {
  const [view, setView] = useState('upload');
  const [smsText, setSmsText] = useState('');
  const [senderHeader, setSenderHeader] = useState('');
  const [parsing, setParsing] = useState(false);
  const [parseResult, setParseResult] = useState(null);

  const handleParseSms = async (e) => {
    e.preventDefault();
    
    if (!smsText.trim()) {
      toast.warning('Please enter SMS text');
      return;
    }

    try {
      setParsing(true);
      const response = await userAPI.parseSms({
        smsText: smsText.trim(),
        senderHeader: senderHeader.trim() || null,
      });
      
      const result = response.data.data;
      setParseResult(result);
      
      if (result.parseStatus === 'SUCCESS' || result.parseStatus === 'PARTIAL') {
        toast.success('SMS parsed successfully!');
      } else {
        toast.warning(result.errorMessage || 'SMS could not be parsed');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to parse SMS');
    } finally {
      setParsing(false);
    }
  };

  const handleClear = () => {
    setSmsText('');
    setSenderHeader('');
    setParseResult(null);
  };

  const formatCurrency = (amount) => {
    if (!amount) return 'N/A';
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
    }).format(amount);
  };

  return (
    <div className="dashboard">
      <div className="container">
        <div className="dashboard-header">
          <h1>User Dashboard</h1>
          <p>Upload SMS messages and view your transactions</p>
        </div>

        <div className="view-tabs">
          <button
            type="button"
            className={`tab ${view === 'upload' ? 'active' : ''}`}
            onClick={() => setView('upload')}
          >
            Upload SMS
          </button>
          <button
            type="button"
            className={`tab ${view === 'finance' ? 'active' : ''}`}
            onClick={() => setView('finance')}
          >
            📊 Personal Finance Manager
          </button>
        </div>

        {view === 'upload' ? (
          <div className="upload-section">
            <div className="upload-grid">
              <div className="upload-form-section">
                <h2>Parse Bank SMS</h2>
                
                <form onSubmit={handleParseSms}>
                  <div className="form-group">
                    <label className="form-label">SMS Text *</label>
                    <textarea
                      className="form-textarea"
                      placeholder="Paste your bank SMS here..."
                      value={smsText}
                      onChange={(e) => setSmsText(e.target.value)}
                      rows={6}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Sender Header (Optional)</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="e.g., HDFCBK, SBIINB"
                      value={senderHeader}
                      onChange={(e) => setSenderHeader(e.target.value)}
                    />
                  </div>

                  <div className="button-group">
                    <button type="submit" className="btn btn-primary" disabled={parsing}>
                      {parsing ? 'Parsing...' : 'Parse SMS'}
                    </button>
                    <button type="button" className="btn btn-secondary" onClick={handleClear}>
                      Clear
                    </button>
                  </div>
                </form>
              </div>

              <div className="result-section">
                <h2>Parsed Result</h2>
                
                {parseResult ? (
                  <div className="parse-result-card">
                    <div className={`status-badge status-${parseResult.parseStatus.toLowerCase()}`}>
                      {parseResult.parseStatus.replace('_', ' ')}
                    </div>

                    {parseResult.parseStatus === 'SUCCESS' || parseResult.parseStatus === 'PARTIAL' ? (
                      <>
                        <div className="result-details">
                          <div className="result-row">
                            <span className="label">Bank:</span>
                            <span className="value">{parseResult.bankName || 'N/A'}</span>
                          </div>
                          <div className="result-row">
                            <span className="label">Type:</span>
                            <span className="value">{parseResult.transactionType}</span>
                          </div>
                          <div className="result-row highlight">
                            <span className="label">Amount:</span>
                            <span className="value amount">{formatCurrency(parseResult.amount)}</span>
                          </div>
                          <div className="result-row highlight">
                            <span className="label">Balance:</span>
                            <span className="value">{formatCurrency(parseResult.balance)}</span>
                          </div>
                          <div className="result-row">
                            <span className="label">Date:</span>
                            <span className="value">
                              {parseResult.transactionDate ? 
                                new Date(parseResult.transactionDate).toLocaleDateString() : 'N/A'}
                            </span>
                          </div>
                          {parseResult.accountId && (
                            <div className="result-row">
                              <span className="label">Account:</span>
                              <span className="value">{parseResult.accountId}</span>
                            </div>
                          )}
                          {parseResult.merchantOrPayee && (
                            <div className="result-row">
                              <span className="label">Merchant:</span>
                              <span className="value">{parseResult.merchantOrPayee}</span>
                            </div>
                          )}
                          {parseResult.mode && (
                            <div className="result-row">
                              <span className="label">Mode:</span>
                              <span className="value">{parseResult.mode}</span>
                            </div>
                          )}
                          {parseResult.referenceNumber && (
                            <div className="result-row">
                              <span className="label">Ref No:</span>
                              <span className="value">{parseResult.referenceNumber}</span>
                            </div>
                          )}
                        </div>

                        {parseResult.extractedData && (
                          <div className="extracted-fields">
                            <h4>All Extracted Fields:</h4>
                            <pre className="json-preview">
                              {JSON.stringify(parseResult.extractedData, null, 2)}
                            </pre>
                          </div>
                        )}

                        {parseResult.templateUsed && (
                          <div className="template-info">
                            Template Used: {parseResult.templateUsed}
                          </div>
                        )}
                      </>
                    ) : (
                      <div className="error-result">
                        <p>{parseResult.errorMessage || 'No matching template found for this SMS'}</p>
                        <small>Please contact a Maker to create a template for this bank/format</small>
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="no-result">
                    <div className="no-result-icon">📊</div>
                    <p>Parse an SMS to see the extracted transaction details here</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        ) : (
          <PersonalFinanceManager />
        )}
      </div>
    </div>
  );
};

export default UserDashboard;

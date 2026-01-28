import React, { useState, useEffect } from 'react';
import { checkerAPI } from '../services/api';
import { toast } from 'react-toastify';
import './CheckerDashboard.css';

const CheckerDashboard = () => {
  const [activeTab, setActiveTab] = useState('pending'); // 'pending' or 'history'
  const [templates, setTemplates] = useState([]);
  const [reviewedTemplates, setReviewedTemplates] = useState([]);
  const [selectedTemplate, setSelectedTemplate] = useState(null);
  const [loading, setLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [comments, setComments] = useState('');
  const [rejectionReason, setRejectionReason] = useState('');
  const [testMessage, setTestMessage] = useState('');
  const [testResults, setTestResults] = useState(null);
  const [testLoading, setTestLoading] = useState(false);

  useEffect(() => {
    loadPendingTemplates();
    loadReviewedTemplates();
  }, []);

  const loadPendingTemplates = async () => {
    try {
      setLoading(true);
      const response = await checkerAPI.getPendingTemplates();
      setTemplates(response.data.data);
    } catch (error) {
      toast.error('Failed to load pending templates');
    } finally {
      setLoading(false);
    }
  };

  const loadReviewedTemplates = async () => {
    try {
      setHistoryLoading(true);
      const response = await checkerAPI.getReviewedTemplates(true); // Always show all history
      setReviewedTemplates(response.data.data);
    } catch (error) {
      toast.error('Failed to load approval history');
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleViewTemplate = (template) => {
    setSelectedTemplate(template);
    setComments('');
    setRejectionReason('');
    setTestMessage(template.sampleSms || '');
    setTestResults(null);
  };

  const handleApprove = async () => {
    if (!selectedTemplate) return;

    try {
      setActionLoading(true);
      await checkerAPI.approveTemplate(selectedTemplate.id, { comments });
      toast.success('Template approved successfully');
      setSelectedTemplate(null);
      loadPendingTemplates();
      loadReviewedTemplates(); // Reload history
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to approve template');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    if (!selectedTemplate) return;
    
    if (!rejectionReason.trim()) {
      toast.warning('Please provide a rejection reason');
      return;
    }

    try {
      setActionLoading(true);
      await checkerAPI.rejectTemplate(selectedTemplate.id, { reason: rejectionReason });
      toast.success('Template rejected');
      setSelectedTemplate(null);
      loadPendingTemplates();
      loadReviewedTemplates(); // Reload history
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to reject template');
    } finally {
      setActionLoading(false);
    }
  };

  const handleTabSwitch = (tab) => {
    setActiveTab(tab);
    setSelectedTemplate(null);
  };

  const handleDeprecate = async (templateId) => {
    if (!window.confirm('Are you sure you want to deprecate this template?')) {
      return;
    }

    try {
      await checkerAPI.deprecateTemplate(templateId, { comments: 'Template deprecated' });
      toast.success('Template deprecated successfully');
      loadPendingTemplates();
      loadReviewedTemplates(); // Reload history
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to deprecate template');
    }
  };

  const handleTestRegex = async () => {
    if (!testMessage.trim()) {
      toast.warning('Please enter a test message');
      return;
    }

    if (!selectedTemplate?.regexPattern) {
      toast.error('No regex pattern available');
      return;
    }

    try {
      setTestLoading(true);
      const response = await checkerAPI.testRegex({
        regexPattern: selectedTemplate.regexPattern,
        sampleSms: testMessage,
      });
      
      const result = response.data.data;
      
      if (result.matched) {
        setTestResults({
          success: true,
          matched: true,
          groups: result.extractedFields || {}
        });
        toast.success('Regex matched successfully!');
      } else {
        setTestResults({
          success: true,
          matched: false,
          message: 'The regex pattern did not match the test message'
        });
        toast.info('No match found');
      }
    } catch (error) {
      setTestResults({
        success: false,
        error: error.response?.data?.message || error.message
      });
      toast.error('Error testing regex: ' + (error.response?.data?.message || error.message));
    } finally {
      setTestLoading(false);
    }
  };

  return (
    <div className="dashboard">
      <div className="container">
        <div className="dashboard-header">
          <h1>Checker Dashboard</h1>
          <p>Review and approve regex templates</p>
        </div>

        <div className="checker-layout">
          <div className="templates-panel">
            <div className="tabs-header">
              <div className="tabs">
                <button
                  className={`tab-btn ${activeTab === 'pending' ? 'active' : ''}`}
                  onClick={() => handleTabSwitch('pending')}
                >
                  Pending Approvals {templates.length > 0 && <span className="tab-count tab-count-pending">{templates.length}</span>}
                </button>
                <button
                  className={`tab-btn ${activeTab === 'history' ? 'active' : ''}`}
                  onClick={() => handleTabSwitch('history')}
                >
                  Approval History {reviewedTemplates.length > 0 && <span className="tab-count tab-count-history">{reviewedTemplates.length}</span>}
                </button>
              </div>
              <div className="header-actions">
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={activeTab === 'pending' ? loadPendingTemplates : loadReviewedTemplates}
                  disabled={activeTab === 'pending' ? loading : historyLoading}
                >
                  Refresh
                </button>
              </div>
            </div>

            {activeTab === 'pending' ? (
              loading ? (
                <div className="loader">
                  <div className="spinner"></div>
                </div>
              ) : templates.length === 0 ? (
                <div className="empty-state">
                  <p>No templates pending approval</p>
                </div>
              ) : (
                <div className="templates-list">
                  {templates.map((template) => (
                    <div
                      key={template.id}
                      className={`template-item ${selectedTemplate?.id === template.id ? 'selected' : ''}`}
                      onClick={() => handleViewTemplate(template)}
                    >
                      <div className="template-item-header">
                        <h3>{template.bankName}</h3>
                        <span className="badge badge-pending">{template.status}</span>
                      </div>
                      <div className="template-item-info">
                        <span className="info-badge">{template.smsType}</span>
                        <span className="info-text">By: {template.createdByUsername}</span>
                      </div>
                      <div className="template-item-date">
                        Submitted: {new Date(template.createdAt).toLocaleDateString()}
                      </div>
                    </div>
                  ))}
                </div>
              )
            ) : (
              historyLoading ? (
                <div className="loader">
                  <div className="spinner"></div>
                </div>
              ) : reviewedTemplates.length === 0 ? (
                <div className="empty-state">
                  <p>No approval history yet</p>
                </div>
              ) : (
                <div className="templates-list">
                  {reviewedTemplates.map((template) => (
                    <div
                      key={template.id}
                      className={`template-item ${selectedTemplate?.id === template.id ? 'selected' : ''}`}
                      onClick={() => handleViewTemplate(template)}
                    >
                      <div className="template-item-header">
                        <h3>{template.bankName}</h3>
                        <span className={`badge badge-${template.status.toLowerCase()}`}>
                          {template.status}
                        </span>
                      </div>
                      <div className="template-item-info">
                        <span className="info-badge">{template.smsType}</span>
                        <span className="info-text">Created by: {template.createdByUsername}</span>
                      </div>
                      {template.approvedByUsername && (
                        <div className="template-item-info">
                          <span className="info-text">
                            {template.status === 'ACTIVE' ? 'Approved' : 
                             template.status === 'REJECTED' ? 'Rejected' : 'Deprecated'} by: {template.approvedByUsername}
                          </span>
                        </div>
                      )}
                      <div className="template-item-date">
                        {template.status === 'ACTIVE' ? 'Approved' : 
                         template.status === 'REJECTED' ? 'Rejected' : 'Deprecated'}: {' '}
                        {new Date(template.updatedAt).toLocaleDateString()}
                      </div>
                      {template.rejectionReason && (
                        <div className="rejection-preview">
                          Reason: {template.rejectionReason.substring(0, 50)}
                          {template.rejectionReason.length > 50 ? '...' : ''}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )
            )}
          </div>

          <div className="review-panel">
            {selectedTemplate ? (
              <>
                <div className="panel-header">
                  <h2>Template Review</h2>
                  <span className={`badge badge-${selectedTemplate.status.toLowerCase()}`}>
                    {selectedTemplate.status.replace('_', ' ')}
                  </span>
                </div>

                <div className="template-details">
                  <div className="detail-section">
                    <h3>Basic Information</h3>
                    <div className="detail-grid">
                      <div className="detail-item">
                        <label>Bank Name</label>
                        <div className="detail-value">{selectedTemplate.bankName}</div>
                      </div>
                      <div className="detail-item">
                        <label>SMS Type</label>
                        <div className="detail-value">{selectedTemplate.smsType}</div>
                      </div>
                      <div className="detail-item">
                        <label>Created By</label>
                        <div className="detail-value">{selectedTemplate.createdByUsername}</div>
                      </div>
                      <div className="detail-item">
                        <label>Created At</label>
                        <div className="detail-value">
                          {new Date(selectedTemplate.createdAt).toLocaleString()}
                        </div>
                      </div>
                    </div>
                  </div>

                  {selectedTemplate.description && (
                    <div className="detail-section">
                      <h3>Description</h3>
                      <p>{selectedTemplate.description}</p>
                    </div>
                  )}

                  {selectedTemplate.status === 'REJECTED' && selectedTemplate.rejectionReason && (
                    <div className="detail-section">
                      <h3>Rejection Reason</h3>
                      <div className="rejection-block">
                        {selectedTemplate.rejectionReason}
                      </div>
                    </div>
                  )}

                  {(selectedTemplate.status === 'ACTIVE' || selectedTemplate.status === 'DEPRECATED') && selectedTemplate.approvedAt && (
                    <div className="detail-section">
                      <h3>Approval Details</h3>
                      <div className="detail-grid">
                        <div className="detail-item">
                          <label>Approved By</label>
                          <div className="detail-value">{selectedTemplate.approvedByUsername || 'You'}</div>
                        </div>
                        <div className="detail-item">
                          <label>Approved At</label>
                          <div className="detail-value">
                            {new Date(selectedTemplate.approvedAt).toLocaleString()}
                          </div>
                        </div>
                      </div>
                      {selectedTemplate.status === 'DEPRECATED' && selectedTemplate.deprecatedAt && (
                        <div className="detail-item" style={{ marginTop: '16px' }}>
                          <label>Deprecated At</label>
                          <div className="detail-value">
                            {new Date(selectedTemplate.deprecatedAt).toLocaleString()}
                          </div>
                        </div>
                      )}
                    </div>
                  )}

                  <div className="detail-section">
                    <h3>Regex Pattern</h3>
                    <div className="code-block">
                      <code>{selectedTemplate.regexPattern}</code>
                    </div>
                  </div>

                  {selectedTemplate.sampleSms && (
                    <div className="detail-section">
                      <h3>Sample SMS</h3>
                      <div className="sms-block">
                        {selectedTemplate.sampleSms}
                      </div>
                    </div>
                  )}

                  <div className="detail-section">
                    <h3>Test Regex</h3>
                    <p className="section-description">
                      Test the regex pattern against different messages. The sample SMS is pre-filled below but you can edit it or try other messages.
                    </p>
                    
                    <div className="form-group">
                      <label className="form-label">Test Message</label>
                      <textarea
                        className="form-textarea"
                        placeholder="Enter a message to test the regex pattern..."
                        value={testMessage}
                        onChange={(e) => setTestMessage(e.target.value)}
                        rows={4}
                      />
                    </div>

                    <button
                      className="btn btn-primary"
                      onClick={handleTestRegex}
                      disabled={testLoading || !testMessage.trim()}
                    >
                      {testLoading ? 'Testing...' : 'Test Regex'}
                    </button>

                    {testResults && (
                      <div className={`test-results ${testResults.matched ? 'success' : 'failure'}`}>
                        {testResults.success ? (
                          testResults.matched ? (
                            <>
                              <div className="result-header">
                                <span className="result-icon">✓</span>
                                <strong>Match Found!</strong>
                              </div>
                              {Object.keys(testResults.groups).length > 0 && (
                                <div className="result-item">
                                  <label>Extracted Fields:</label>
                                  <div className="result-groups">
                                    {Object.entries(testResults.groups).map(([key, value]) => (
                                      <div key={key} className="group-item">
                                        <span className="group-key">{key}:</span>
                                        <span className="group-value">{value}</span>
                                      </div>
                                    ))}
                                  </div>
                                </div>
                              )}
                            </>
                          ) : (
                            <div className="result-header">
                              <span className="result-icon">✗</span>
                              <strong>{testResults.message}</strong>
                            </div>
                          )
                        ) : (
                          <div className="result-header error">
                            <span className="result-icon">⚠</span>
                            <strong>Error: {testResults.error}</strong>
                          </div>
                        )}
                      </div>
                    )}
                  </div>

                  {selectedTemplate.status === 'PENDING_APPROVAL' && (
                    <div className="action-section">
                      <h3>Review Actions</h3>
                      
                      <div className="form-group">
                        <label className="form-label">Comments (Optional)</label>
                        <textarea
                          className="form-textarea"
                          placeholder="Add any comments about this template..."
                          value={comments}
                          onChange={(e) => setComments(e.target.value)}
                          rows={3}
                        />
                      </div>

                      <div className="button-group">
                        <button
                          className="btn btn-success"
                          onClick={handleApprove}
                          disabled={actionLoading}
                        >
                          ✓ Approve
                        </button>
                      </div>

                      <div className="reject-section">
                        <div className="form-group">
                          <label className="form-label">Rejection Reason *</label>
                          <textarea
                            className="form-textarea"
                            placeholder="Provide reason for rejection..."
                            value={rejectionReason}
                            onChange={(e) => setRejectionReason(e.target.value)}
                            rows={3}
                          />
                        </div>
                        <button
                          className="btn btn-danger"
                          onClick={handleReject}
                          disabled={actionLoading}
                        >
                          ✗ Reject
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="no-selection">
                <div className="no-selection-icon">👈</div>
                <p>Click on any template to see the details and review</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CheckerDashboard;

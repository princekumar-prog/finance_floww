import React, { useState, useEffect } from 'react';
import { makerAPI } from '../services/api';
import { toast } from 'react-toastify';
import './MakerDashboard.css';

const MakerDashboard = () => {
  const [view, setView] = useState('create'); // 'create', 'list', or 'action-needed'
  const [templates, setTemplates] = useState([]);
  const [unparsedSms, setUnparsedSms] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // Form state
  const [formData, setFormData] = useState({
    bankName: '',
    regexPattern: '',
    smsType: 'DEBIT',
    sampleSms: '',
    description: '',
  });
  
  // Test result state
  const [testResult, setTestResult] = useState(null);
  const [testing, setTesting] = useState(false);
  const [editingId, setEditingId] = useState(null);
  
  // Action Needed state
  const [generatedTemplate, setGeneratedTemplate] = useState(null);
  const [generating, setGenerating] = useState(false);
  const [selectedSmsId, setSelectedSmsId] = useState(null);

  useEffect(() => {
    // Load unparsed SMS count on initial mount
    loadUnparsedSms();
  }, []);

  useEffect(() => {
    if (view === 'list') {
      loadTemplates();
    } else if (view === 'action-needed') {
      loadUnparsedSms();
    }
  }, [view]);

  const loadTemplates = async () => {
    try {
      setLoading(true);
      const response = await makerAPI.getMyTemplates();
      setTemplates(response.data.data);
    } catch (error) {
      toast.error('Failed to load templates');
    } finally {
      setLoading(false);
    }
  };

  const loadUnparsedSms = async () => {
    try {
      setLoading(true);
      const response = await makerAPI.getUnparsedSms();
      setUnparsedSms(response.data.data);
    } catch (error) {
      toast.error('Failed to load unparsed SMS');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    // Clear test results when pattern changes
    if (e.target.name === 'regexPattern' || e.target.name === 'sampleSms') {
      setTestResult(null);
    }
  };

  const handleTestRegex = async () => {
    if (!formData.regexPattern || !formData.sampleSms) {
      toast.warning('Please provide both regex pattern and sample SMS');
      return;
    }

    try {
      setTesting(true);
      const response = await makerAPI.testRegex({
        regexPattern: formData.regexPattern,
        sampleSms: formData.sampleSms,
      });
      setTestResult(response.data.data);
      
      if (response.data.data.matched) {
        toast.success('Pattern matched successfully!');
      } else {
        toast.warning('Pattern did not match');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Error testing regex');
    } finally {
      setTesting(false);
    }
  };

  const handleSaveDraft = async () => {
    if (!formData.bankName || !formData.regexPattern) {
      toast.warning('Bank name and regex pattern are required');
      return;
    }

    try {
      setLoading(true);
      if (editingId) {
        await makerAPI.updateTemplate(editingId, formData);
        toast.success('Template updated successfully');
      } else {
        await makerAPI.createTemplate(formData);
        toast.success('Template saved as draft');
      }
      
      // If this was from action needed, delete the SMS
      if (selectedSmsId) {
        try {
          await makerAPI.deleteUnparsedSms(selectedSmsId);
        } catch (error) {
          console.error('Failed to delete SMS:', error);
        }
        setSelectedSmsId(null);
        setGeneratedTemplate(null);
      }
      
      resetForm();
      setView('list');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save template');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitForApproval = async () => {
    if (!formData.bankName || !formData.regexPattern) {
      toast.warning('Bank name and regex pattern are required');
      return;
    }

    try {
      setLoading(true);
      
      // Check if the same regex pattern already exists (in pending or active status)
      try {
        const duplicateCheckResponse = await makerAPI.checkDuplicateRegex({
          regexPattern: formData.regexPattern,
          excludeId: editingId || null
        });
        
        if (duplicateCheckResponse.data.data.exists) {
          const duplicate = duplicateCheckResponse.data.data;
          toast.error(
            duplicate.message || 'This regex pattern already exists in the system. Cannot submit duplicate.'
          );
          setLoading(false);
          return;
        }
      } catch (duplicateError) {
        // If the duplicate check endpoint doesn't exist, fall back to checking maker's own templates
        console.warn('Duplicate check endpoint not available, using fallback method');
        
        const templatesResponse = await makerAPI.getMyTemplates();
        const existingTemplates = templatesResponse.data.data;
        
        const duplicateTemplate = existingTemplates.find(
          (template) =>
            template.id !== editingId &&
            template.regexPattern === formData.regexPattern &&
            (template.status === 'PENDING_APPROVAL' || template.status === 'ACTIVE')
        );
        
        if (duplicateTemplate) {
          toast.error(
            `This regex pattern already exists with status: ${duplicateTemplate.status}. Cannot submit duplicate.`
          );
          setLoading(false);
          return;
        }
      }
      
      let templateId = editingId;
      
      if (!templateId) {
        const response = await makerAPI.createTemplate(formData);
        templateId = response.data.data.id;
      } else {
        await makerAPI.updateTemplate(templateId, formData);
      }
      
      await makerAPI.submitForApproval(templateId);
      toast.success('Template submitted for approval');
      
      // If this was from action needed, delete the SMS
      if (selectedSmsId) {
        try {
          await makerAPI.deleteUnparsedSms(selectedSmsId);
        } catch (error) {
          console.error('Failed to delete SMS:', error);
        }
        setSelectedSmsId(null);
        setGeneratedTemplate(null);
      }
      
      resetForm();
      setView('list');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to submit template');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (template) => {
    if (template.status !== 'DRAFT') {
      toast.warning('Only draft templates can be edited');
      return;
    }
    
    setFormData({
      bankName: template.bankName,
      regexPattern: template.regexPattern,
      smsType: template.smsType,
      sampleSms: template.sampleSms || '',
      description: template.description || '',
    });
    setEditingId(template.id);
    setView('create');
  };

  const resetForm = () => {
    setFormData({
      bankName: '',
      regexPattern: '',
      smsType: 'DEBIT',
      sampleSms: '',
      description: '',
    });
    setTestResult(null);
    setEditingId(null);
  };

  const handleDeleteSms = async (id) => {
    if (!window.confirm('Are you sure you want to delete this SMS?')) {
      return;
    }

    try {
      await makerAPI.deleteUnparsedSms(id);
      toast.success('SMS deleted successfully');
      loadUnparsedSms();
      if (selectedSmsId === id) {
        setSelectedSmsId(null);
        setGeneratedTemplate(null);
      }
    } catch (error) {
      toast.error('Failed to delete SMS');
    }
  };

  const handleGenerateTemplate = async (sms) => {
    try {
      setGenerating(true);
      setSelectedSmsId(sms.id);
      const response = await makerAPI.generateTemplate({
        smsText: sms.rawSmsText,
        senderHeader: sms.senderHeader,
      });
      
      if (response.data.data.success) {
        setGeneratedTemplate(response.data.data);
        toast.success('Template generated successfully!');
      } else {
        toast.error(response.data.data.errorMessage || 'Failed to generate template');
        setGeneratedTemplate(null);
        setSelectedSmsId(null);
      }
    } catch (error) {
      toast.error('Failed to generate template');
      setGeneratedTemplate(null);
      setSelectedSmsId(null);
    } finally {
      setGenerating(false);
    }
  };

  const handleUseTemplate = () => {
    if (!generatedTemplate) return;

    setFormData({
      bankName: generatedTemplate.bankName,
      regexPattern: generatedTemplate.regexPattern,
      smsType: generatedTemplate.smsType,
      sampleSms: generatedTemplate.sampleSms,
      description: generatedTemplate.description,
    });
    setEditingId(null);
    setView('create');
    toast.success('Template loaded! You can now edit and submit it.');
  };

  const handleTryNewGeneration = async () => {
    const sms = unparsedSms.find(s => s.id === selectedSmsId);
    if (sms) {
      setGeneratedTemplate(null);
      await handleGenerateTemplate(sms);
    }
  };

  return (
    <div className="dashboard">
      <div className="container">
        <div className="dashboard-header">
          <h1>Maker Dashboard</h1>
          <p>Create and manage regex templates for SMS parsing</p>
        </div>

        <div className="view-tabs">
          <button
            className={`tab ${view === 'create' ? 'active' : ''}`}
            onClick={() => { setView('create'); resetForm(); setGeneratedTemplate(null); setSelectedSmsId(null); }}
          >
            Create Template
          </button>
          <button
            className={`tab ${view === 'list' ? 'active' : ''}`}
            onClick={() => setView('list')}
          >
            My Templates
          </button>
          <button
            className={`tab ${view === 'action-needed' ? 'active' : ''}`}
            onClick={() => setView('action-needed')}
          >
            Action Needed
            {unparsedSms.length > 0 && (
              <span className="badge-count">{unparsedSms.length}</span>
            )}
          </button>
        </div>

        {view === 'create' ? (
          <div className="create-template-section">
            <div className="template-form-grid">
              <div className="form-section">
                <h2>{editingId ? 'Edit Template' : 'Create New Template'}</h2>
                
                <div className="form-group">
                  <label className="form-label">Bank Name *</label>
                  <input
                    type="text"
                    name="bankName"
                    className="form-input"
                    placeholder="e.g., HDFC Bank, SBI, ICICI"
                    value={formData.bankName}
                    onChange={handleChange}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">SMS Type *</label>
                  <select
                    name="smsType"
                    className="form-select"
                    value={formData.smsType}
                    onChange={handleChange}
                  >
                    <option value="DEBIT">Debit</option>
                    <option value="CREDIT">Credit</option>
                    <option value="BILL">Bill Payment</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Regex Pattern *</label>
                  <textarea
                    name="regexPattern"
                    className="form-textarea"
                    placeholder="Enter regex pattern with named groups, e.g., Rs\\.(?<amount>[\\d,]+)"
                    value={formData.regexPattern}
                    onChange={handleChange}
                    rows={4}
                  />
                  <small className="form-hint">
                    Use named groups: (?&lt;name&gt;pattern) to extract fields
                  </small>
                </div>

                <div className="form-group">
                  <label className="form-label">Sample SMS</label>
                  <textarea
                    name="sampleSms"
                    className="form-textarea"
                    placeholder="Paste a sample bank SMS here to test your regex"
                    value={formData.sampleSms}
                    onChange={handleChange}
                    rows={3}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Description</label>
                  <textarea
                    name="description"
                    className="form-textarea"
                    placeholder="Describe this template (optional)"
                    value={formData.description}
                    onChange={handleChange}
                    rows={2}
                  />
                </div>

                <div className="button-group">
                  <button
                    className="btn btn-secondary"
                    onClick={handleTestRegex}
                    disabled={testing || !formData.regexPattern || !formData.sampleSms}
                  >
                    {testing ? 'Testing...' : 'Test Regex'}
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={handleSaveDraft}
                    disabled={loading}
                  >
                    Save Draft
                  </button>
                  <button
                    className="btn btn-primary"
                    onClick={handleSubmitForApproval}
                    disabled={loading}
                  >
                    Submit for Approval
                  </button>
                </div>
              </div>

              <div className="preview-section">
                <h2>Live Preview</h2>
                
                {testResult ? (
                  <div className="test-result-card">
                    <div className={`result-status ${testResult.matched ? 'success' : 'error'}`}>
                      {testResult.matched ? '✓ Match Found!' : '✗ No Match'}
                    </div>
                    
                    {testResult.matched && testResult.extractedFields && (
                      <div className="extracted-fields">
                        <h3>Extracted Fields:</h3>
                        <div className="field-list">
                          {Object.entries(testResult.extractedFields).map(([key, value]) => (
                            <div key={key} className="field-item">
                              <span className="field-key">{key}:</span>
                              <span className="field-value">{value}</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                    
                    {testResult.errorMessage && (
                      <div className="error-box">
                        <strong>Error:</strong> {testResult.errorMessage}
                      </div>
                    )}
                    
                    <div className="execution-time">
                      Execution time: {testResult.executionTimeMs}ms
                    </div>
                  </div>
                ) : (
                  <div className="no-preview">
                    <p>Enter regex pattern and sample SMS, then click "Test Regex" to see results</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        ) : view === 'list' ? (
          <div className="templates-list-section">
            {loading ? (
              <div className="loader">
                <div className="spinner"></div>
              </div>
            ) : templates.length === 0 ? (
              <div className="empty-state">
                <p>No templates found. Create your first template!</p>
                <button className="btn btn-primary" onClick={() => setView('create')}>
                  Create Template
                </button>
              </div>
            ) : (
              <div className="templates-grid">
                {templates.map((template) => (
                  <div key={template.id} className="template-card">
                    <div className="template-header">
                      <h3>{template.bankName}</h3>
                      <span className={`badge badge-${template.status.toLowerCase().replace('_', '-')}`}>
                        {template.status.replace('_', ' ')}
                      </span>
                    </div>
                    
                    <div className="template-info">
                      <div className="info-row">
                        <span className="label">Type:</span>
                        <span>{template.smsType}</span>
                      </div>
                      <div className="info-row">
                        <span className="label">Created:</span>
                        <span>{new Date(template.createdAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                    
                    <div className="template-pattern">
                      <strong>Pattern:</strong>
                      <code>{template.regexPattern.substring(0, 100)}...</code>
                    </div>
                    
                    {template.status === 'DRAFT' && (
                      <button
                        className="btn btn-secondary btn-sm"
                        onClick={() => handleEdit(template)}
                      >
                        Edit
                      </button>
                    )}
                    
                    {template.status === 'REJECTED' && template.rejectionReason && (
                      <div className="rejection-reason">
                        <strong>Rejection Reason:</strong>
                        <p>{template.rejectionReason}</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : view === 'action-needed' ? (
          <div className="action-needed-section">
            {loading ? (
              <div className="loader">
                <div className="spinner"></div>
              </div>
            ) : unparsedSms.length === 0 ? (
              <div className="empty-state">
                <p>No unparsed SMS found. All your SMS have been successfully parsed!</p>
              </div>
            ) : (
              <div className="unparsed-sms-grid">
                {unparsedSms.map((sms) => (
                  <div key={sms.id} className="unparsed-sms-card">
                    <div className="sms-header">
                      <div>
                        <h3>SMS from {sms.senderHeader || 'Unknown'}</h3>
                        <span className="sms-uploader">Uploaded by: {sms.uploadedByUsername}</span>
                      </div>
                      <span className="sms-date">
                        {new Date(sms.createdAt).toLocaleString()}
                      </span>
                    </div>
                    
                    <div className="sms-content">
                      <strong>Message:</strong>
                      <p>{sms.rawSmsText}</p>
                    </div>
                    
                    {sms.errorMessage && (
                      <div className="sms-error">
                        <strong>Error:</strong> {sms.errorMessage}
                      </div>
                    )}
                    
                    <div className="sms-actions">
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={() => handleDeleteSms(sms.id)}
                      >
                        Delete
                      </button>
                      <button
                        className="btn btn-primary btn-sm"
                        onClick={() => handleGenerateTemplate(sms)}
                        disabled={generating && selectedSmsId === sms.id}
                      >
                        {generating && selectedSmsId === sms.id ? 'Generating...' : 'Generate Template'}
                      </button>
                    </div>
                    
                    {selectedSmsId === sms.id && generatedTemplate && (
                      <div className="generated-template-preview">
                        <div className="preview-header">
                          <h4>Generated Template</h4>
                        </div>
                        
                        <div className="preview-details">
                          <div className="preview-item">
                            <label>Bank Name:</label>
                            <span>{generatedTemplate.bankName}</span>
                          </div>
                          <div className="preview-item">
                            <label>SMS Type:</label>
                            <span>{generatedTemplate.smsType}</span>
                          </div>
                          <div className="preview-item">
                            <label>Description:</label>
                            <span>{generatedTemplate.description}</span>
                          </div>
                        </div>
                        
                        <div className="preview-pattern">
                          <strong>Regex Pattern:</strong>
                          <code>{generatedTemplate.regexPattern}</code>
                        </div>
                        
                        <div className="preview-sms">
                          <strong>Sample SMS:</strong>
                          <p>{generatedTemplate.sampleSms}</p>
                        </div>
                        
                        <div className="preview-actions">
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={handleTryNewGeneration}
                            disabled={generating}
                          >
                            Try New Generation
                          </button>
                          <button
                            className="btn btn-success btn-sm"
                            onClick={handleUseTemplate}
                          >
                            Use This Template
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default MakerDashboard;

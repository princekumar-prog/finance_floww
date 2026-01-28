import React from 'react';
import { Link } from 'react-router-dom';
import './HomePage.css';

const HomePage = () => {
  return (
    <div className="home-page">
      <div className="hero-section">
        <div className="container">
          <div className="hero-content">
            <h1 className="hero-title">
              <span className="logo-icon">⚡</span>
              RegexFlow
            </h1>
            <p className="hero-subtitle">
              Fintech SMS-to-Ledger Engine
            </p>
            <p className="hero-description">
              Transform bank SMS messages into structured financial transactions 
              using powerful regex templates and maker-checker workflows.
            </p>
            
            <div className="cta-buttons">
              <Link to="/login" className="btn btn-primary btn-lg">
                Login
              </Link>
              <Link to="/register" className="btn btn-secondary btn-lg">
                Register
              </Link>
            </div>
          </div>
        </div>
      </div>

      <div className="features-section">
        <div className="container">
          <h2 className="section-title">Key Features</h2>
          
          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon">👨‍💼</div>
              <h3>Maker Dashboard</h3>
              <p>Create and test regex templates for parsing bank SMS messages with live preview.</p>
              <ul className="feature-list">
                <li>Create regex patterns</li>
                <li>Test with sample SMS</li>
                <li>Submit for approval</li>
                <li>Track template status</li>
              </ul>
            </div>

            <div className="feature-card">
              <div className="feature-icon">✅</div>
              <h3>Checker Dashboard</h3>
              <p>Review and approve regex templates with comprehensive workflow management.</p>
              <ul className="feature-list">
                <li>Review pending templates</li>
                <li>Approve or reject</li>
                <li>Deprecate templates</li>
                <li>Audit trail tracking</li>
              </ul>
            </div>

            <div className="feature-card">
              <div className="feature-icon">📱</div>
              <h3>User Dashboard</h3>
              <p>Upload SMS messages and view parsed financial transactions automatically.</p>
              <ul className="feature-list">
                <li>Upload bank SMS</li>
                <li>Auto-parse transactions</li>
                <li>View transaction history</li>
                <li>Filter and search</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <footer className="footer">
        <div className="container">
          <p>&copy; 2024 RegexFlow. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default HomePage;

import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const getDashboardLink = () => {
    switch (user?.role) {
      case 'MAKER':
        return '/maker';
      case 'CHECKER':
        return '/checker';
      case 'NORMAL_USER':
        return '/user';
      default:
        return '/';
    }
  };

  return (
    <nav className="navbar">
      <div className="container navbar-content">
        <Link to={getDashboardLink()} className="navbar-brand">
          <span className="logo-icon">⚡</span>
          RegexFlow
        </Link>
        
        <div className="navbar-right">
          <div className="user-info">
            <span className="user-name">{user?.fullName}</span>
            <span className={`role-badge role-${user?.role?.toLowerCase()}`}>
              {user?.role?.replace('_', ' ')}
            </span>
          </div>
          <button onClick={handleLogout} className="btn btn-secondary btn-sm">
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

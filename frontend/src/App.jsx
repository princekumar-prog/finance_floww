import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import { AuthProvider, useAuth } from './context/AuthContext';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import MakerDashboard from './pages/MakerDashboard';
import CheckerDashboard from './pages/CheckerDashboard';
import UserDashboard from './pages/UserDashboard';
import PersonalFinanceManager from './pages/PersonalFinanceManager';
import Navbar from './components/Navbar';

// Protected Route Component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="loader">
        <div className="spinner"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
};

function AppRoutes() {
  const { user } = useAuth();

  return (
    <>
      {user && <Navbar />}
      <Routes>
        <Route path="/" element={user ? <Navigate to={getDashboardRoute(user.role)} /> : <HomePage />} />
        <Route path="/login" element={user ? <Navigate to={getDashboardRoute(user.role)} /> : <LoginPage />} />
        <Route path="/register" element={user ? <Navigate to={getDashboardRoute(user.role)} /> : <RegisterPage />} />

        <Route
          path="/maker"
          element={
            <ProtectedRoute allowedRoles={['MAKER']}>
              <MakerDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/checker"
          element={
            <ProtectedRoute allowedRoles={['CHECKER']}>
              <CheckerDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/user"
          element={
            <ProtectedRoute allowedRoles={['NORMAL_USER']}>
              <UserDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/finance"
          element={
            <ProtectedRoute allowedRoles={['NORMAL_USER']}>
              <PersonalFinanceManager />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}

function getDashboardRoute(role) {
  switch (role) {
    case 'MAKER':
      return '/maker';
    case 'CHECKER':
      return '/checker';
    case 'NORMAL_USER':
      return '/user';
    default:
      return '/';
  }
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppRoutes />
        <ToastContainer
          position="top-right"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
        />
      </AuthProvider>
    </Router>
  );
}

export default App;

import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth APIs
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

// Maker APIs
export const makerAPI = {
  createTemplate: (data) => api.post('/maker/templates', data),
  testRegex: (data) => api.post('/maker/templates/test', data),
  updateTemplate: (id, data) => api.put(`/maker/templates/${id}`, data),
  submitForApproval: (id) => api.post(`/maker/templates/${id}/submit`),
  getMyTemplates: () => api.get('/maker/templates'),
  getTemplate: (id) => api.get(`/maker/templates/${id}`),
  getUnparsedSms: () => api.get('/maker/unparsed-sms'),
  deleteUnparsedSms: (id) => api.delete(`/maker/unparsed-sms/${id}`),
  generateTemplate: (data) => api.post('/maker/generate-template', data),
  checkDuplicateRegex: (params) => api.get('/maker/templates/check-duplicate', { params }),
};

// Checker APIs
export const checkerAPI = {
  getPendingTemplates: () => api.get('/checker/templates/pending'),
  getReviewedTemplates: (all = true) => api.get('/checker/templates/reviewed', { params: { all } }),
  getTemplate: (id) => api.get(`/checker/templates/${id}`),
  approveTemplate: (id, data) => api.post(`/checker/templates/${id}/approve`, data),
  rejectTemplate: (id, data) => api.post(`/checker/templates/${id}/reject`, data),
  deprecateTemplate: (id, data) => api.post(`/checker/templates/${id}/deprecate`, data),
  testRegex: (data) => api.post('/checker/templates/test', data),
};

// User APIs
export const userAPI = {
  parseSms: (data) => api.post('/user/sms/parse', data),
  getTransactionHistory: (page = 0, size = 20) => 
    api.get(`/user/transactions?page=${page}&size=${size}`),
  getFilteredTransactions: (filters) => api.get('/user/transactions/filter', { params: filters }),
  getTransaction: (id) => api.get(`/user/transactions/${id}`),
};

export default api;

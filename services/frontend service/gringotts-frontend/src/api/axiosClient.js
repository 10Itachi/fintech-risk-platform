// axiosClient.js
// Central API client — all calls go through here
// Automatically attaches JWT to every request
// Handles token expiry globally in one place

import axios from 'axios';
import { tokenManager } from '../auth/tokenManager';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_URL,  // from .env.local — never hardcoded
  timeout: 10000,                               // 10 second timeout
  headers: {
    'Content-Type': 'application/json',
  }
});

// REQUEST INTERCEPTOR
// Runs before every API call
// Attaches the JWT token automatically
apiClient.interceptors.request.use(
  (config) => {
    const token = tokenManager.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// RESPONSE INTERCEPTOR
// Runs after every API response
// If backend returns 401 (token expired) → clear token and redirect to login
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      tokenManager.clearToken();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
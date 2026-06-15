// transactionApi.js
// Handles all transaction service API calls

import apiClient from './axiosClient';

// Create a new transaction — USER role required
export async function createTransaction(transactionData) {
  const response = await apiClient.post('/api/v1/transactions/user/create', transactionData);
  return response.data;
}

// Get all transactions paginated — ADMIN role required
// Matches: /api/v1/transactions/admin/allUsers?page=0&size=10&sort=createdAt,desc
export async function getAllTransactions(page = 0, size = 10, sort = 'createdAt,desc') {
  const response = await apiClient.get('/api/v1/transactions/admin/allUsers', {
    params: { page, size, sort }
  });
  return response.data;
}
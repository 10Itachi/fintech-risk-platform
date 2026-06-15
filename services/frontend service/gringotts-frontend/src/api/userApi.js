import apiClient from './axiosClient';

// Function to create a new user
export async function createUser(userData) {
  const response = await apiClient.post('/api/v1/users/admin/createUser', userData);
  return response.data;
}

// Function to get a user by ID
export async function getUserById(userId) {
  const response = await apiClient.get(`/api/v1/users/shared/getUserById/${userId}`);
  return response.data;
}
import apiClient from './axiosClient';

// Function to get risk decision for a given transaction ID
export async function getRiskDecision(transactionId) {
  const response = await apiClient.get(`/risk/api/v1/decisions/transaction/${transactionId}`);
  return response.data;
}

// Function to get AI summary for a given transaction ID
export async function getAISummary(transactionId) {
  const response = await apiClient.post(
    `/risk/api/v1/ai/${transactionId}/investigation-summary`, 
    {}, // Empty request body since the ID is passed via the path variable
    {
      timeout: 60000 // Forces this specific Axios request to wait up to 60 seconds
    }
  );
  return response.data;
}
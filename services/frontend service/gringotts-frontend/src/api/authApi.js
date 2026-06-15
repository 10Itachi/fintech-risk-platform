// authApi.js
// Handles all authentication calls to Keycloak
// This is the only file that talks to Keycloak directly

import axiosClient from './axiosClient';
import { tokenManager } from '../auth/tokenManager';

// LOGIN
// Sends username + password to Keycloak
// Gets back JWT access_token and stores it in memory
export async function login(username, password) {

  // URLSearchParams formats the body as form data
  // Keycloak requires this format — it does not accept JSON for token requests
  const params = new URLSearchParams({
    grant_type: 'password',
    client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
    username,
    password,
  });

  // POST to Keycloak token endpoint via gateway
  const response = await axiosClient.post(
    `${import.meta.env.VITE_KEYCLOAK_URL}/realms/${import.meta.env.VITE_KEYCLOAK_REALM}/protocol/openid-connect/token`,
    params.toString(),
    {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }
  );

  // Store token in memory — never in localStorage
  tokenManager.setToken(response.data.access_token);

  return response.data;
}

// LOGOUT
// Clears token from memory and sends user back to login
export function logout() {
  tokenManager.clearToken();
  window.location.href = '/login';
}
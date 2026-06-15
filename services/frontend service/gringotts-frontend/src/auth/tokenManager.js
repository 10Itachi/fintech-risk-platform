// tokenManager.js
// Stores JWT in memory only — never in localStorage or cookies
// This means: if someone opens DevTools, they cannot steal the token
// Trade-off: user must log in again after page refresh (acceptable for a secure banking app)

let accessToken = null;
let userRoles = [];
let userName = null;

export const tokenManager = {

  // Called after successful login
  setToken(token) {
    accessToken = token;
    // Decode the JWT payload (middle part) to extract roles and username
    // JWT is base64 encoded — we decode it to read claims
    // This is safe — we are NOT verifying the signature here
    // Signature verification happens on the backend (your gateway)
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      userRoles = payload?.realm_access?.roles || [];
      userName = payload?.preferred_username || null;
    } catch {
      userRoles = [];
      userName = null;
    }
  },

  // Called on logout or token expiry
  clearToken() {
    accessToken = null;
    userRoles = [];
    userName = null;
  },

  getToken() {
    return accessToken;
  },

  getRoles() {
    return userRoles;
  },

  getUserName() {
    return userName;
  },

  isAuthenticated() {
    return accessToken !== null;
  },

  hasRole(role) {
    return userRoles.includes(role);
  },

  isAdmin() {
    return userRoles.includes('ADMIN');
  },

  isUser() {
    return userRoles.includes('USER');
  }

};
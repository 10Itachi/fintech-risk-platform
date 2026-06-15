// main.jsx
// Entry point — mounts the React app into index.html
// AppRouter contains all routes, auth context, and page components

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import AppRouter from './router/AppRouter'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AppRouter />
  </StrictMode>
)
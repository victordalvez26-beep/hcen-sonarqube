/**
 * Configuración del frontend HCEN
 */
const config = {
  BACKEND_URL: process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080',
  CALLBACK_URL: process.env.REACT_APP_CALLBACK_URL || 'http://localhost:8080',
};

export default config;

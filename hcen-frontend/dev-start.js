#!/usr/bin/env node

const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log('Iniciando ambiente de desarrollo HCEN Frontend...\n');

// Función para limpiar caché si es necesario
function cleanCache() {
  const cacheDir = path.join(__dirname, 'node_modules', '.cache');
  if (fs.existsSync(cacheDir)) {
    console.log('Limpiando caché...');
    fs.rmSync(cacheDir, { recursive: true, force: true });
  }
}

// Función para iniciar el servidor de desarrollo
function startDevServer() {
  console.log('Iniciando servidor de desarrollo...');
  
  const child = spawn('npm', ['start'], {
    stdio: 'inherit',
    shell: true,
    env: {
      ...process.env,
      BROWSER: 'none', // Evita abrir el navegador automáticamente
      FAST_REFRESH: 'true'
    }
  });

  child.on('error', (error) => {
    console.error('Error al iniciar el servidor:', error);
  });

  child.on('close', (code) => {
    console.log(`\nServidor cerrado con código ${code}`);
  });

  // Manejo de señales para cerrar limpiamente
  process.on('SIGINT', () => {
    console.log('\nCerrando servidor de desarrollo...');
    child.kill('SIGINT');
    process.exit(0);
  });

  process.on('SIGTERM', () => {
    console.log('\nCerrando servidor de desarrollo...');
    child.kill('SIGTERM');
    process.exit(0);
  });
}

// Verificar si se solicita limpieza de caché
if (process.argv.includes('--clean')) {
  cleanCache();
}

startDevServer();

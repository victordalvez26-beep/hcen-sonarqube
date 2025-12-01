#!/bin/bash 

APP_NAME=hcen-frontend

echo "Construyendo imagen..."
docker build -t $APP_NAME .

echo "Eliminando contenedor previo (si existe)..."
docker rm -f $APP_NAME 2>/dev/null || true

echo "Levantando nuevo contenedor..."
docker run -d \
  --name $APP_NAME \
  -p 3000:3000 \
  --restart=always \
  $APP_NAME

echo "Deploy completado. Servido en puerto 3000"
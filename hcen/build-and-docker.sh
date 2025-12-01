#!/bin/bash

echo "🔨 Compilando aplicación Java EE..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "Compilación exitosa"
    echo "Construyendo imagen Docker..."
    docker-compose build backend
    echo "Imagen Docker creada"
    echo ""
    echo "Para levantar todo el stack:"
    echo "  docker-compose up -d"
else
    echo "Error en la compilación"
    exit 1
fi


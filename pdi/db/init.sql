-- Script de inicialización de la base de datos PDI INUS
-- Este script se ejecuta automáticamente al crear el contenedor PostgreSQL

-- Crear la tabla si no existe (Hibernate también la creará, pero esto asegura que exista para los inserts)
CREATE TABLE IF NOT EXISTS inus_usuario (
    id BIGSERIAL PRIMARY KEY,
    uid VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255),
    primer_nombre VARCHAR(100),
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100),
    segundo_apellido VARCHAR(100),
    tip_docum VARCHAR(20),
    cod_docum VARCHAR(20),
    nacionalidad VARCHAR(10),
    fecha_nacimiento DATE,
    departamento VARCHAR(50),
    localidad VARCHAR(100),
    direccion VARCHAR(255),
    telefono VARCHAR(50),
    codigo_postal VARCHAR(10),
    profile_completed BOOLEAN NOT NULL DEFAULT false,
    rol VARCHAR(20) NOT NULL DEFAULT 'USUARIO_SALUD'
);

-- Crear índices si no existen
CREATE INDEX IF NOT EXISTS idx_uid ON inus_usuario(uid);
CREATE INDEX IF NOT EXISTS idx_documento ON inus_usuario(tip_docum, cod_docum);
CREATE INDEX IF NOT EXISTS idx_email ON inus_usuario(email);

-- Insertar usuarios de prueba (solo si no existen)
INSERT INTO inus_usuario (
    uid, email, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido,
    tip_docum, cod_docum, nacionalidad, fecha_nacimiento, departamento, localidad,
    direccion, telefono, codigo_postal, profile_completed, rol
) VALUES
(
    'uid-victor-50830691',
    'victor.alvez@example.com',
    'Victor',
    'David',
    'Alvez',
    'González',
    'CI',
    '50830691',
    'UY',
    '2000-12-26',
    'MONTEVIDEO',
    'Montevideo',
    'Av. 18 de Julio 1234',
    '099123456',
    '11200',
    true,
    'USUARIO_SALUD'
),
(
    'uid-roberto-26347848',
    'roberto.silva@example.com',
    'Roberto',
    NULL,
    'Silva',
    'Santos',
    'PASAPORTE',
    '26347848',
    'BR',
    '1982-04-18',
    NULL,
    NULL,
    NULL,
    '+55 11 98765-4321',
    NULL,
    true,
    'USUARIO_SALUD'
),
(
    'uid-admin-hcen',
    'admin@hcen.gub.uy',
    'María',
    'Laura',
    'González',
    'Pérez',
    'CI',
    '41500075',
    'UY',
    '1985-03-15',
    'MONTEVIDEO',
    'Montevideo',
    NULL,
    NULL,
    NULL,
    true,
    'ADMIN_HCEN'
)
ON CONFLICT (uid) DO NOTHING;


-- Migration: Add clinic information columns to registro_acceso table
-- Date: 2025-11-26
-- Description: Adds columns to store clinic ID, professional name, and specialty for better access tracking.
--              This allows patients to see which clinic and professional accessed their clinical history.

-- Add clinica_id column (ID of the tenant/clinic)
ALTER TABLE registro_acceso 
ADD COLUMN IF NOT EXISTS clinica_id VARCHAR(100);

-- Add nombre_profesional column (Full name of the professional)
ALTER TABLE registro_acceso 
ADD COLUMN IF NOT EXISTS nombre_profesional VARCHAR(255);

-- Add especialidad column (Specialty of the professional)
ALTER TABLE registro_acceso 
ADD COLUMN IF NOT EXISTS especialidad VARCHAR(100);

-- Add comments to document the columns
COMMENT ON COLUMN registro_acceso.clinica_id IS 'ID del tenant/clínica del profesional que accedió al documento';
COMMENT ON COLUMN registro_acceso.nombre_profesional IS 'Nombre completo del profesional que accedió al documento';
COMMENT ON COLUMN registro_acceso.especialidad IS 'Especialidad del profesional que accedió al documento';

-- Optional: Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_registro_acceso_clinica_id ON registro_acceso(clinica_id);


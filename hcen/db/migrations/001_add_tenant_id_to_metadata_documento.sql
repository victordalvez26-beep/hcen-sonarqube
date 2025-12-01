-- Migration: Add tenant_id column to metadata_documento table
-- Date: 2025-01-XX
-- Description: Adds tenant_id column to store the tenant (clinic) ID that generated the document.
--              This allows the HCEN backend to correctly identify which tenant to query when downloading PDFs.

-- Add tenant_id column (nullable to allow existing records)
ALTER TABLE metadata_documento 
ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- Add comment to document the column
COMMENT ON COLUMN metadata_documento.tenant_id IS 'ID del tenant (clínica) que generó el documento. Usado para identificar correctamente el tenant al descargar PDFs desde el componente periférico.';

-- Optional: Create index for better query performance if needed
-- CREATE INDEX IF NOT EXISTS idx_metadata_documento_tenant_id ON metadata_documento(tenant_id);


package uy.edu.tse.hcen.rndc.mapper;

import uy.edu.tse.hcen.rndc.model.MetadataDocumento;
import uy.edu.tse.hcen.dto.MetadataDocumentoDTO;
import uy.edu.tse.hcen.common.enumerations.FormatoDocumento;
import uy.edu.tse.hcen.common.enumerations.TipoDocumentoClinico;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Mapper para convertir entre la entidad MetadataDocumento y su DTO.
 */
public class MetadataDocumentoMapper {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public static MetadataDocumentoDTO toDTO(MetadataDocumento entidad) {
        if (entidad == null) {
            return null;
        }
        
        MetadataDocumentoDTO dto = new MetadataDocumentoDTO();
        dto.setId(entidad.getId());
        dto.setCodDocum(entidad.getCodDocum());
        dto.setNombrePaciente(entidad.getNombrePaciente());
        dto.setApellidoPaciente(entidad.getApellidoPaciente());
        
        if (entidad.getTipoDocumento() != null) {
            dto.setTipoDocumento(entidad.getTipoDocumento().name());
        }
        
        if (entidad.getFechaCreacion() != null) {
            dto.setFechaCreacion(entidad.getFechaCreacion().format(DATE_FORMATTER));
        }
        
        if (entidad.getFormatoDocumento() != null) {
            dto.setFormatoDocumento(entidad.getFormatoDocumento().name());
        }
        
        dto.setUriDocumento(entidad.getUriDocumento());
        dto.setClinicaOrigen(entidad.getClinicaOrigen());
        dto.setTenantId(entidad.getTenantId());
        dto.setProfesionalSalud(entidad.getProfesionalSalud());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setAccesoPermitido(!entidad.isRestringido());
        
        return dto;
    }
    
    public static MetadataDocumento toEntity(MetadataDocumentoDTO dto) {
        if (dto == null) {
            return null;
        }
        
        MetadataDocumento entidad = new MetadataDocumento();
        entidad.setCodDocum(dto.getCodDocum());
        entidad.setNombrePaciente(dto.getNombrePaciente());
        entidad.setApellidoPaciente(dto.getApellidoPaciente());
        
        if (dto.getTipoDocumento() != null && !dto.getTipoDocumento().isBlank()) {
            try {
                entidad.setTipoDocumento(TipoDocumentoClinico.valueOf(dto.getTipoDocumento()));
            } catch (IllegalArgumentException e) {
                entidad.setTipoDocumento(null);
            }
        }
        
        if (dto.getFechaCreacion() != null && !dto.getFechaCreacion().isBlank()) {
            try {
                entidad.setFechaCreacion(LocalDateTime.parse(dto.getFechaCreacion(), DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                entidad.setFechaCreacion(null);
            }
        }
        
        if (dto.getFormatoDocumento() != null && !dto.getFormatoDocumento().isBlank()) {
            try {
                entidad.setFormatoDocumento(FormatoDocumento.valueOf(dto.getFormatoDocumento()));
            } catch (IllegalArgumentException e) {
                entidad.setFormatoDocumento(null);
            }
        }
        
        entidad.setUriDocumento(dto.getUriDocumento());
        entidad.setClinicaOrigen(dto.getClinicaOrigen());
        entidad.setTenantId(dto.getTenantId());
        entidad.setProfesionalSalud(dto.getProfesionalSalud());
        entidad.setDescripcion(dto.getDescripcion());
        entidad.setRestringido(!dto.isAccesoPermitido());
        
        return entidad;
    }
}


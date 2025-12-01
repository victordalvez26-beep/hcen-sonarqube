package uy.edu.tse.hcen.politicas.mapper;

import uy.edu.tse.hcen.politicas.model.PoliticaAcceso;
import uy.edu.tse.hcen.dto.PoliticaAccesoDTO;

import java.util.Date;

public class PoliticaAccesoMapper {

    public static PoliticaAccesoDTO toDTO(PoliticaAcceso entity) {
        if (entity == null) return null;
        
        PoliticaAccesoDTO dto = new PoliticaAccesoDTO();
        dto.setId(entity.getId());
        dto.setAlcance(entity.getAlcance());
        dto.setDuracion(entity.getDuracion());
        dto.setGestion(entity.getGestion());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setReferencia(entity.getReferencia());
        dto.setCodDocumPaciente(entity.getCodDocumPaciente());
        dto.setProfesionalAutorizado(entity.getProfesionalAutorizado());
        dto.setTipoDocumento(entity.getTipoDocumento());
        dto.setClinicaAutorizada(entity.getClinicaAutorizada());
        dto.setEspecialidadesAutorizadas(entity.getEspecialidadesAutorizadas());
        dto.setFechaVencimiento(entity.getFechaVencimiento());
        dto.setActiva(entity.getActiva()); // Mapear el campo activa
        return dto;
    }

    public static PoliticaAcceso toEntity(PoliticaAccesoDTO dto) {
        if (dto == null) return null;
        
        PoliticaAcceso entity = new PoliticaAcceso();
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setAlcance(dto.getAlcance());
        entity.setDuracion(dto.getDuracion());
        entity.setGestion(dto.getGestion());
        entity.setFechaCreacion(dto.getFechaCreacion() != null ? dto.getFechaCreacion() : new Date());
        entity.setReferencia(dto.getReferencia());
        entity.setCodDocumPaciente(dto.getCodDocumPaciente());
        // Asegurar que profesionalAutorizado nunca sea null (requerido por la base de datos)
        entity.setProfesionalAutorizado(dto.getProfesionalAutorizado() != null && !dto.getProfesionalAutorizado().trim().isEmpty() 
            ? dto.getProfesionalAutorizado() : "*");
        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setClinicaAutorizada(dto.getClinicaAutorizada());
        entity.setEspecialidadesAutorizadas(dto.getEspecialidadesAutorizadas());
        entity.setFechaVencimiento(dto.getFechaVencimiento());
        entity.setActiva(true);
        return entity;
    }
}







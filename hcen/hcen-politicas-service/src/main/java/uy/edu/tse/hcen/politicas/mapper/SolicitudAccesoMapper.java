package uy.edu.tse.hcen.politicas.mapper;

import uy.edu.tse.hcen.politicas.model.SolicitudAcceso;
import uy.edu.tse.hcen.dto.SolicitudAccesoDTO;

public class SolicitudAccesoMapper {

    public static SolicitudAccesoDTO toDTO(SolicitudAcceso entity) {
        if (entity == null) return null;
        
        SolicitudAccesoDTO dto = new SolicitudAccesoDTO();
        dto.setId(entity.getId());
        dto.setFechaSolicitud(entity.getFechaSolicitud());
        dto.setEstado(entity.getEstado());
        dto.setSolicitanteId(entity.getSolicitanteId());
        dto.setEspecialidad(entity.getEspecialidad());
        dto.setCodDocumPaciente(entity.getCodDocumPaciente());
        dto.setTipoDocumento(entity.getTipoDocumento());
        dto.setFechaResolucion(entity.getFechaResolucion());
        return dto;
    }

    public static SolicitudAcceso toEntity(SolicitudAccesoDTO dto) {
        if (dto == null) return null;
        
        SolicitudAcceso entity = new SolicitudAcceso();
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setFechaSolicitud(dto.getFechaSolicitud());
        entity.setEstado(dto.getEstado());
        entity.setSolicitanteId(dto.getSolicitanteId());
        entity.setEspecialidad(dto.getEspecialidad());
        entity.setCodDocumPaciente(dto.getCodDocumPaciente());
        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setFechaResolucion(dto.getFechaResolucion());
        return entity;
    }
}











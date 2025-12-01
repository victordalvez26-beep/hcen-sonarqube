package uy.edu.tse.hcen.politicas.mapper;

import uy.edu.tse.hcen.politicas.model.RegistroAcceso;
import uy.edu.tse.hcen.dto.RegistroAccesoDTO;

public class RegistroAccesoMapper {

    public static RegistroAccesoDTO toDTO(RegistroAcceso entity) {
        if (entity == null) return null;
        
        RegistroAccesoDTO dto = new RegistroAccesoDTO();
        dto.setId(entity.getId());
        dto.setFecha(entity.getFecha());
        dto.setReferencia(entity.getReferencia());
        dto.setProfesionalId(entity.getProfesionalId());
        dto.setCodDocumPaciente(entity.getCodDocumPaciente());
        dto.setDocumentoId(entity.getDocumentoId());
        dto.setTipoDocumento(entity.getTipoDocumento());
        dto.setIpAddress(entity.getIpAddress());
        dto.setUserAgent(entity.getUserAgent());
        dto.setExito(entity.getExito());
        dto.setMotivoRechazo(entity.getMotivoRechazo());
        dto.setClinicaId(entity.getClinicaId());
        dto.setNombreProfesional(entity.getNombreProfesional());
        dto.setEspecialidad(entity.getEspecialidad());
        return dto;
    }
}











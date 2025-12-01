package uy.edu.tse.hcen.politicas.mapper;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.common.enumerations.EstadoSolicitudAcceso;
import uy.edu.tse.hcen.dto.SolicitudAccesoDTO;
import uy.edu.tse.hcen.politicas.model.SolicitudAcceso;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para SolicitudAccesoMapper.
 */
class SolicitudAccesoMapperTest {

    @Test
    void toDTO_validEntity_shouldConvert() {
        SolicitudAcceso entity = new SolicitudAcceso();
        entity.setId(1L);
        entity.setFechaSolicitud(new Date());
        entity.setEstado(EstadoSolicitudAcceso.PENDIENTE);
        entity.setSolicitanteId("PROF-001");
        entity.setEspecialidad("CARDIOLOGIA");
        entity.setCodDocumPaciente("1.234.567-8");
        entity.setTipoDocumento("Resumen");
        
        SolicitudAccesoDTO dto = SolicitudAccesoMapper.toDTO(entity);
        
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(EstadoSolicitudAcceso.PENDIENTE, dto.getEstado());
        assertEquals("PROF-001", dto.getSolicitanteId());
        assertEquals("1.234.567-8", dto.getCodDocumPaciente());
    }

    @Test
    void toDTO_nullEntity_shouldReturnNull() {
        SolicitudAccesoDTO dto = SolicitudAccesoMapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    void toEntity_validDTO_shouldConvert() {
        SolicitudAccesoDTO dto = new SolicitudAccesoDTO();
        dto.setId(1L);
        dto.setFechaSolicitud(new Date());
        dto.setEstado(EstadoSolicitudAcceso.PENDIENTE);
        dto.setSolicitanteId("PROF-001");
        dto.setCodDocumPaciente("1.234.567-8");
        
        SolicitudAcceso entity = SolicitudAccesoMapper.toEntity(dto);
        
        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals(EstadoSolicitudAcceso.PENDIENTE, entity.getEstado());
        assertEquals("PROF-001", entity.getSolicitanteId());
    }

    @Test
    void toEntity_nullDTO_shouldReturnNull() {
        SolicitudAcceso entity = SolicitudAccesoMapper.toEntity(null);
        assertNull(entity);
    }

    @Test
    void toEntity_nullId_shouldNotSetId() {
        SolicitudAccesoDTO dto = new SolicitudAccesoDTO();
        dto.setId(null);
        dto.setSolicitanteId("PROF-001");
        
        SolicitudAcceso entity = SolicitudAccesoMapper.toEntity(dto);
        
        assertNotNull(entity);
        assertNull(entity.getId());
    }

    @Test
    void roundTrip_conversion_shouldMaintainData() {
        SolicitudAcceso original = new SolicitudAcceso();
        original.setId(1L);
        original.setEstado(EstadoSolicitudAcceso.PENDIENTE);
        original.setSolicitanteId("PROF-001");
        original.setCodDocumPaciente("1.234.567-8");
        
        SolicitudAccesoDTO dto = SolicitudAccesoMapper.toDTO(original);
        SolicitudAcceso converted = SolicitudAccesoMapper.toEntity(dto);
        
        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getEstado(), converted.getEstado());
        assertEquals(original.getSolicitanteId(), converted.getSolicitanteId());
    }
}



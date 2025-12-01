package uy.edu.tse.hcen.politicas.mapper;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.dto.RegistroAccesoDTO;
import uy.edu.tse.hcen.politicas.model.RegistroAcceso;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para RegistroAccesoMapper.
 */
class RegistroAccesoMapperTest {

    @Test
    void toDTO_validEntity_shouldConvert() {
        RegistroAcceso entity = new RegistroAcceso();
        entity.setId(1L);
        entity.setFecha(new Date());
        entity.setProfesionalId("PROF-001");
        entity.setCodDocumPaciente("1.234.567-8");
        entity.setDocumentoId("DOC-123");
        entity.setTipoDocumento("Resumen");
        entity.setIpAddress("192.168.1.1");
        entity.setUserAgent("Mozilla/5.0");
        entity.setExito(true);
        entity.setClinicaId("clinic-1");
        entity.setNombreProfesional("Dr. Test");
        entity.setEspecialidad("CARDIOLOGIA");
        
        RegistroAccesoDTO dto = RegistroAccesoMapper.toDTO(entity);
        
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("PROF-001", dto.getProfesionalId());
        assertEquals("1.234.567-8", dto.getCodDocumPaciente());
        assertEquals("DOC-123", dto.getDocumentoId());
        assertEquals("Resumen", dto.getTipoDocumento());
        assertTrue(dto.getExito());
        assertEquals("clinic-1", dto.getClinicaId());
    }

    @Test
    void toDTO_nullEntity_shouldReturnNull() {
        RegistroAccesoDTO dto = RegistroAccesoMapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    void toDTO_withFailure_shouldIncludeMotivoRechazo() {
        RegistroAcceso entity = new RegistroAcceso();
        entity.setId(1L);
        entity.setExito(false);
        entity.setMotivoRechazo("Sin permiso");
        
        RegistroAccesoDTO dto = RegistroAccesoMapper.toDTO(entity);
        
        assertNotNull(dto);
        assertFalse(dto.getExito());
        assertEquals("Sin permiso", dto.getMotivoRechazo());
    }
}



package uy.edu.tse.hcen.politicas.mapper;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.common.enumerations.AlcancePoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.DuracionPoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.TipoGestionAcceso;
import uy.edu.tse.hcen.dto.PoliticaAccesoDTO;
import uy.edu.tse.hcen.politicas.model.PoliticaAcceso;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para PoliticaAccesoMapper.
 */
class PoliticaAccesoMapperTest {

    @Test
    void toDTO_validEntity_shouldConvert() {
        PoliticaAcceso entity = new PoliticaAcceso();
        entity.setId(1L);
        entity.setAlcance(AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS);
        entity.setDuracion(DuracionPoliticaAcceso.INDEFINIDA);
        entity.setGestion(TipoGestionAcceso.AUTOMATICA);
        entity.setCodDocumPaciente("1.234.567-8");
        entity.setProfesionalAutorizado("PROF-001");
        entity.setTipoDocumento("Resumen");
        entity.setFechaCreacion(new Date());
        
        PoliticaAccesoDTO dto = PoliticaAccesoMapper.toDTO(entity);
        
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS, dto.getAlcance());
        assertEquals("1.234.567-8", dto.getCodDocumPaciente());
        assertEquals("PROF-001", dto.getProfesionalAutorizado());
    }

    @Test
    void toDTO_nullEntity_shouldReturnNull() {
        PoliticaAccesoDTO dto = PoliticaAccesoMapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    void toEntity_validDTO_shouldConvert() {
        PoliticaAccesoDTO dto = new PoliticaAccesoDTO();
        dto.setId(1L);
        dto.setAlcance(AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS);
        dto.setDuracion(DuracionPoliticaAcceso.INDEFINIDA);
        dto.setGestion(TipoGestionAcceso.AUTOMATICA);
        dto.setCodDocumPaciente("1.234.567-8");
        dto.setProfesionalAutorizado("PROF-001");
        dto.setTipoDocumento("Resumen");
        
        PoliticaAcceso entity = PoliticaAccesoMapper.toEntity(dto);
        
        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals(AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS, entity.getAlcance());
        assertEquals("1.234.567-8", entity.getCodDocumPaciente());
        assertEquals("PROF-001", entity.getProfesionalAutorizado());
        assertTrue(entity.getActiva());
    }

    @Test
    void toEntity_nullDTO_shouldReturnNull() {
        PoliticaAcceso entity = PoliticaAccesoMapper.toEntity(null);
        assertNull(entity);
    }

    @Test
    void toEntity_nullProfesionalAutorizado_shouldSetWildcard() {
        PoliticaAccesoDTO dto = new PoliticaAccesoDTO();
        dto.setProfesionalAutorizado(null);
        
        PoliticaAcceso entity = PoliticaAccesoMapper.toEntity(dto);
        
        assertNotNull(entity);
        assertEquals("*", entity.getProfesionalAutorizado());
    }

    @Test
    void toEntity_emptyProfesionalAutorizado_shouldSetWildcard() {
        PoliticaAccesoDTO dto = new PoliticaAccesoDTO();
        dto.setProfesionalAutorizado("");
        
        PoliticaAcceso entity = PoliticaAccesoMapper.toEntity(dto);
        
        assertNotNull(entity);
        assertEquals("*", entity.getProfesionalAutorizado());
    }

    @Test
    void toEntity_nullFechaCreacion_shouldSetCurrentDate() {
        PoliticaAccesoDTO dto = new PoliticaAccesoDTO();
        dto.setFechaCreacion(null);
        
        PoliticaAcceso entity = PoliticaAccesoMapper.toEntity(dto);
        
        assertNotNull(entity);
        assertNotNull(entity.getFechaCreacion());
    }

    @Test
    void roundTrip_conversion_shouldMaintainData() {
        PoliticaAcceso original = new PoliticaAcceso();
        original.setId(1L);
        original.setAlcance(AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS);
        original.setCodDocumPaciente("1.234.567-8");
        original.setProfesionalAutorizado("PROF-001");
        original.setFechaCreacion(new Date());
        
        PoliticaAccesoDTO dto = PoliticaAccesoMapper.toDTO(original);
        PoliticaAcceso converted = PoliticaAccesoMapper.toEntity(dto);
        
        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getAlcance(), converted.getAlcance());
        assertEquals(original.getCodDocumPaciente(), converted.getCodDocumPaciente());
    }
}



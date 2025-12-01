package uy.edu.tse.hcen.rndc.mapper;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.rndc.model.MetadataDocumento;
import uy.edu.tse.hcen.dto.MetadataDocumentoDTO;
import uy.edu.tse.hcen.common.enumerations.TipoDocumentoClinico;
import uy.edu.tse.hcen.common.enumerations.FormatoDocumento;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MetadataDocumentoMapperTest {

    @Test
    void toDTO_withValidEntity_shouldMapCorrectly() {
        // Arrange
        MetadataDocumento entity = new MetadataDocumento();
        entity.setId(1L);
        entity.setCodDocum("12345678");
        entity.setNombrePaciente("Juan");
        entity.setApellidoPaciente("Pérez");
        entity.setTipoDocumento(TipoDocumentoClinico.CONSULTA_MEDICA);
        entity.setFechaCreacion(LocalDateTime.of(2024, 1, 15, 0, 0));
        entity.setFormatoDocumento(FormatoDocumento.PDF);
        entity.setUriDocumento("http://example.com/doc.pdf");
        entity.setClinicaOrigen("Clínica 1");
        entity.setTenantId(1L);
        entity.setProfesionalSalud("Dr. Smith");
        entity.setDescripcion("Descripción del documento");
        entity.setRestringido(false);

        // Act
        MetadataDocumentoDTO dto = MetadataDocumentoMapper.toDTO(entity);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("12345678", dto.getCodDocum());
        assertEquals("Juan", dto.getNombrePaciente());
        assertEquals("Pérez", dto.getApellidoPaciente());
        assertEquals("CONSULTA_MEDICA", dto.getTipoDocumento());
        // Mapper usa ISO_LOCAL_DATE_TIME, por lo que incluye la parte de tiempo
        assertEquals("2024-01-15T00:00:00", dto.getFechaCreacion());
        assertEquals("PDF", dto.getFormatoDocumento());
        assertEquals("http://example.com/doc.pdf", dto.getUriDocumento());
        assertEquals("Clínica 1", dto.getClinicaOrigen());
        assertEquals(1L, dto.getTenantId());
        assertEquals("Dr. Smith", dto.getProfesionalSalud());
        assertEquals("Descripción del documento", dto.getDescripcion());
        assertTrue(dto.isAccesoPermitido());
    }

    @Test
    void toDTO_withNullEntity_shouldReturnNull() {
        // Act
        MetadataDocumentoDTO dto = MetadataDocumentoMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    void toDTO_withRestringidoTrue_shouldSetAccesoPermitidoFalse() {
        // Arrange
        MetadataDocumento entity = new MetadataDocumento();
        entity.setId(1L);
        entity.setRestringido(true);

        // Act
        MetadataDocumentoDTO dto = MetadataDocumentoMapper.toDTO(entity);

        // Assert
        assertNotNull(dto);
        assertFalse(dto.isAccesoPermitido());
    }

    @Test
    void toDTO_withNullTipoDocumento_shouldHandleGracefully() {
        // Arrange
        MetadataDocumento entity = new MetadataDocumento();
        entity.setId(1L);
        entity.setTipoDocumento(null);

        // Act
        MetadataDocumentoDTO dto = MetadataDocumentoMapper.toDTO(entity);

        // Assert
        assertNotNull(dto);
        assertNull(dto.getTipoDocumento());
    }

    @Test
    void toDTO_withNullFechaCreacion_shouldHandleGracefully() {
        // Arrange
        MetadataDocumento entity = new MetadataDocumento();
        entity.setId(1L);
        entity.setFechaCreacion(null);

        // Act
        MetadataDocumentoDTO dto = MetadataDocumentoMapper.toDTO(entity);

        // Assert
        assertNotNull(dto);
        assertNull(dto.getFechaCreacion());
    }

    @Test
    void toDTO_withNullFormatoDocumento_shouldHandleGracefully() {
        // Arrange
        MetadataDocumento entity = new MetadataDocumento();
        entity.setId(1L);
        entity.setFormatoDocumento(null);

        // Act
        MetadataDocumentoDTO dto = MetadataDocumentoMapper.toDTO(entity);

        // Assert
        assertNotNull(dto);
        assertNull(dto.getFormatoDocumento());
    }

    @Test
    void toEntity_withValidDTO_shouldMapCorrectly() {
        // Arrange
        MetadataDocumentoDTO dto = new MetadataDocumentoDTO();
        dto.setCodDocum("12345678");
        dto.setNombrePaciente("Juan");
        dto.setApellidoPaciente("Pérez");
        dto.setTipoDocumento("CONSULTA_MEDICA");
        // Mapper espera un ISO_LOCAL_DATE_TIME completo
        dto.setFechaCreacion("2024-01-15T00:00:00");
        dto.setFormatoDocumento("PDF");
        dto.setUriDocumento("http://example.com/doc.pdf");
        dto.setClinicaOrigen("Clínica 1");
        dto.setTenantId(1L);
        dto.setProfesionalSalud("Dr. Smith");
        dto.setDescripcion("Descripción del documento");
        dto.setAccesoPermitido(true);

        // Act
        MetadataDocumento entity = MetadataDocumentoMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertEquals("12345678", entity.getCodDocum());
        assertEquals("Juan", entity.getNombrePaciente());
        assertEquals("Pérez", entity.getApellidoPaciente());
        assertEquals(TipoDocumentoClinico.CONSULTA_MEDICA, entity.getTipoDocumento());
        assertEquals(LocalDateTime.of(2024, 1, 15, 0, 0), entity.getFechaCreacion());
        assertEquals(FormatoDocumento.PDF, entity.getFormatoDocumento());
        assertEquals("http://example.com/doc.pdf", entity.getUriDocumento());
        assertEquals("Clínica 1", entity.getClinicaOrigen());
        assertEquals(1L, entity.getTenantId());
        assertEquals("Dr. Smith", entity.getProfesionalSalud());
        assertEquals("Descripción del documento", entity.getDescripcion());
        assertFalse(entity.isRestringido());
    }

    @Test
    void toEntity_withNullDTO_shouldReturnNull() {
        // Act
        MetadataDocumento entity = MetadataDocumentoMapper.toEntity(null);

        // Assert
        assertNull(entity);
    }

    @Test
    void toEntity_withAccesoPermitidoFalse_shouldSetRestringidoTrue() {
        // Arrange
        MetadataDocumentoDTO dto = new MetadataDocumentoDTO();
        dto.setCodDocum("12345678");
        dto.setAccesoPermitido(false);

        // Act
        MetadataDocumento entity = MetadataDocumentoMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertTrue(entity.isRestringido());
    }

    @Test
    void toEntity_withInvalidTipoDocumento_shouldSetNull() {
        // Arrange
        MetadataDocumentoDTO dto = new MetadataDocumentoDTO();
        dto.setCodDocum("12345678");
        dto.setTipoDocumento("INVALID_TYPE");

        // Act
        MetadataDocumento entity = MetadataDocumentoMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getTipoDocumento());
    }

    @Test
    void toEntity_withInvalidFechaCreacion_shouldSetNull() {
        // Arrange
        MetadataDocumentoDTO dto = new MetadataDocumentoDTO();
        dto.setCodDocum("12345678");
        dto.setFechaCreacion("invalid-date");

        // Act
        MetadataDocumento entity = MetadataDocumentoMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getFechaCreacion());
    }

    @Test
    void toEntity_withInvalidFormatoDocumento_shouldSetNull() {
        // Arrange
        MetadataDocumentoDTO dto = new MetadataDocumentoDTO();
        dto.setCodDocum("12345678");
        dto.setFormatoDocumento("INVALID_FORMAT");

        // Act
        MetadataDocumento entity = MetadataDocumentoMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getFormatoDocumento());
    }

    @Test
    void toEntity_withBlankTipoDocumento_shouldSetNull() {
        // Arrange
        MetadataDocumentoDTO dto = new MetadataDocumentoDTO();
        dto.setCodDocum("12345678");
        dto.setTipoDocumento("   ");

        // Act
        MetadataDocumento entity = MetadataDocumentoMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getTipoDocumento());
    }

    @Test
    void toEntity_withBlankFechaCreacion_shouldSetNull() {
        // Arrange
        MetadataDocumentoDTO dto = new MetadataDocumentoDTO();
        dto.setCodDocum("12345678");
        dto.setFechaCreacion("   ");

        // Act
        MetadataDocumento entity = MetadataDocumentoMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getFechaCreacion());
    }

    @Test
    void toEntity_withBlankFormatoDocumento_shouldSetNull() {
        // Arrange
        MetadataDocumentoDTO dto = new MetadataDocumentoDTO();
        dto.setCodDocum("12345678");
        dto.setFormatoDocumento("   ");

        // Act
        MetadataDocumento entity = MetadataDocumentoMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getFormatoDocumento());
    }
}


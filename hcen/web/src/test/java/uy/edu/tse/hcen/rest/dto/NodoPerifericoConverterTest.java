package uy.edu.tse.hcen.rest.dto;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.model.EstadoNodoPeriferico;
import uy.edu.tse.hcen.model.NodoPeriferico;

import static org.junit.jupiter.api.Assertions.*;

public class NodoPerifericoConverterTest {

    @Test
    public void testDtoToEntityAndBack() {
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
    dto.setId(1L);
        dto.setNombre("Centro A");
        dto.setRUT("12345678-9");
        dto.setDepartamento("MONTEVIDEO");
        dto.setLocalidad("Ciudad");
        dto.setDireccion("Calle Falsa 123");
        dto.setContacto("contacto@example.com");
        dto.setUrl("http://example.com");
        dto.setEstado("ACTIVO");
    dto.setFechaAlta("2023-08-14T12:34:56+00:00");

        NodoPeriferico entity = NodoPerifericoConverter.toEntity(dto);
        assertNotNull(entity);
    assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getNombre(), entity.getNombre());
        assertEquals(dto.getRUT(), entity.getRUT());
        assertNotNull(entity.getEstado());
        assertEquals(EstadoNodoPeriferico.ACTIVO, entity.getEstado());
        assertEquals(dto.getDireccion(), entity.getDireccion());
    assertNotNull(entity.getFechaAlta());

        NodoPerifericoDTO back = NodoPerifericoConverter.toDTO(entity);
        assertNotNull(back);
        assertEquals(dto.getNombre(), back.getNombre());
        assertEquals(dto.getDepartamento(), back.getDepartamento());
        assertEquals(dto.getEstado(), back.getEstado());
        assertNotNull(back.getFechaAlta());
    }

    @Test
    void toEntity_withNullDto_shouldReturnNull() {
        // Act
        NodoPeriferico entity = NodoPerifericoConverter.toEntity(null);

        // Assert
        assertNull(entity);
    }

    @Test
    void toDTO_withNullEntity_shouldReturnNull() {
        // Act
        NodoPerifericoDTO dto = NodoPerifericoConverter.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    void toEntity_withNullDepartamento_shouldHandleGracefully() {
        // Arrange
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Test");
        dto.setDepartamento(null);

        // Act
        NodoPeriferico entity = NodoPerifericoConverter.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getDepartamento());
    }

    @Test
    void toEntity_withNullEstado_shouldHandleGracefully() {
        // Arrange
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Test");
        dto.setEstado(null);

        // Act
        NodoPeriferico entity = NodoPerifericoConverter.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getEstado());
    }

    @Test
    void toEntity_withNullFechaAlta_shouldHandleGracefully() {
        // Arrange
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Test");
        dto.setFechaAlta(null);

        // Act
        NodoPeriferico entity = NodoPerifericoConverter.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getFechaAlta());
    }

    @Test
    void toDTO_withNullDepartamento_shouldHandleGracefully() {
        // Arrange
        NodoPeriferico entity = new NodoPeriferico();
        entity.setNombre("Test");
        entity.setDepartamento(null);

        // Act
        NodoPerifericoDTO dto = NodoPerifericoConverter.toDTO(entity);

        // Assert
        assertNotNull(dto);
        assertNull(dto.getDepartamento());
    }

    @Test
    void toDTO_withNullEstado_shouldHandleGracefully() {
        // Arrange
        NodoPeriferico entity = new NodoPeriferico();
        entity.setNombre("Test");
        entity.setEstado(null);

        // Act
        NodoPerifericoDTO dto = NodoPerifericoConverter.toDTO(entity);

        // Assert
        assertNotNull(dto);
        assertNull(dto.getEstado());
    }

    @Test
    void toDTO_withNullFechaAlta_shouldHandleGracefully() {
        // Arrange
        NodoPeriferico entity = new NodoPeriferico();
        entity.setNombre("Test");
        entity.setFechaAlta(null);

        // Act
        NodoPerifericoDTO dto = NodoPerifericoConverter.toDTO(entity);

        // Assert
        assertNotNull(dto);
        assertNull(dto.getFechaAlta());
    }

    @Test
    void toDTO_withActivationData_shouldMapCorrectly() {
        // Arrange
        NodoPeriferico entity = new NodoPeriferico();
        entity.setId(1L);
        entity.setNombre("Test");
        entity.setAdminNickname("admin_test");
        entity.setActivationUrl("http://test.com/activate");
        entity.setActivationToken("token123");
        entity.setAdminEmail("admin@test.com");

        // Act
        NodoPerifericoDTO dto = NodoPerifericoConverter.toDTO(entity);

        // Assert
        assertNotNull(dto);
        assertEquals("admin_test", dto.getAdminNickname());
        assertEquals("http://test.com/activate", dto.getActivationUrl());
        assertEquals("token123", dto.getActivationToken());
        assertEquals("admin@test.com", dto.getAdminEmail());
    }
}

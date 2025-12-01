package uy.edu.tse.hcen.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para ConfigResource.
 */
class ConfigResourceTest {

    private ConfigResource resource;

    @BeforeEach
    void setUp() {
        resource = new ConfigResource();
    }

    @Test
    void getNacionalidades_shouldReturnList() {
        Response response = resource.getNacionalidades();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof List);
    }

    @Test
    void getNacionalidades_shouldContainValidData() {
        Response response = resource.getNacionalidades();
        
        @SuppressWarnings("unchecked")
        List<ConfigResource.NacionalidadDTO> nacionalidades = (List<ConfigResource.NacionalidadDTO>) response.getEntity();
        
        assertNotNull(nacionalidades);
        assertFalse(nacionalidades.isEmpty());
        
        // Verificar que contiene al menos algunas nacionalidades esperadas
        boolean hasUy = nacionalidades.stream().anyMatch(n -> "UY".equals(n.getCodigo()));
        boolean hasAr = nacionalidades.stream().anyMatch(n -> "AR".equals(n.getCodigo()));
        
        assertTrue(hasUy, "Debe contener UY");
        assertTrue(hasAr, "Debe contener AR");
    }

    @Test
    void getNacionalidades_allShouldHaveCodigoAndNombre() {
        Response response = resource.getNacionalidades();
        
        @SuppressWarnings("unchecked")
        List<ConfigResource.NacionalidadDTO> nacionalidades = (List<ConfigResource.NacionalidadDTO>) response.getEntity();
        
        for (ConfigResource.NacionalidadDTO dto : nacionalidades) {
            assertNotNull(dto.getCodigo(), "Toda nacionalidad debe tener código");
            assertFalse(dto.getCodigo().isEmpty(), "Código no debe estar vacío");
            assertNotNull(dto.getNombre(), "Toda nacionalidad debe tener nombre");
            assertFalse(dto.getNombre().isEmpty(), "Nombre no debe estar vacío");
        }
    }

    @Test
    void getRoles_shouldReturnList() {
        Response response = resource.getRoles();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof List);
    }

    @Test
    void getRoles_shouldContainValidData() {
        Response response = resource.getRoles();
        
        @SuppressWarnings("unchecked")
        List<ConfigResource.RolDTO> roles = (List<ConfigResource.RolDTO>) response.getEntity();
        
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
    }

    @Test
    void getRoles_allShouldHaveCodigoAndDescripcion() {
        Response response = resource.getRoles();
        
        @SuppressWarnings("unchecked")
        List<ConfigResource.RolDTO> roles = (List<ConfigResource.RolDTO>) response.getEntity();
        
        for (ConfigResource.RolDTO dto : roles) {
            assertNotNull(dto.getCodigo(), "Todo rol debe tener código");
            assertFalse(dto.getCodigo().isEmpty(), "Código no debe estar vacío");
            assertNotNull(dto.getDescripcion(), "Todo rol debe tener descripción");
            assertFalse(dto.getDescripcion().isEmpty(), "Descripción no debe estar vacía");
        }
    }

    @Test
    void nacionalidadDTO_shouldWorkCorrectly() {
        ConfigResource.NacionalidadDTO dto = new ConfigResource.NacionalidadDTO("UY", "Uruguay");
        
        assertEquals("UY", dto.getCodigo());
        assertEquals("Uruguay", dto.getNombre());
        
        dto.setCodigo("AR");
        dto.setNombre("Argentina");
        
        assertEquals("AR", dto.getCodigo());
        assertEquals("Argentina", dto.getNombre());
    }

    @Test
    void rolDTO_shouldWorkCorrectly() {
        ConfigResource.RolDTO dto = new ConfigResource.RolDTO("ADMIN", "Administrador");
        
        assertEquals("ADMIN", dto.getCodigo());
        assertEquals("Administrador", dto.getDescripcion());
        
        dto.setCodigo("USER");
        dto.setDescripcion("Usuario");
        
        assertEquals("USER", dto.getCodigo());
        assertEquals("Usuario", dto.getDescripcion());
    }

    @Test
    void nacionalidadDTO_constructor_shouldSetValues() {
        // Act
        ConfigResource.NacionalidadDTO dto = new ConfigResource.NacionalidadDTO("BR", "Brasil");

        // Assert
        assertEquals("BR", dto.getCodigo());
        assertEquals("Brasil", dto.getNombre());
    }

    @Test
    void nacionalidadDTO_setters_shouldUpdateValues() {
        // Arrange
        ConfigResource.NacionalidadDTO dto = new ConfigResource.NacionalidadDTO("UY", "Uruguay");

        // Act
        dto.setCodigo("AR");
        dto.setNombre("Argentina");

        // Assert
        assertEquals("AR", dto.getCodigo());
        assertEquals("Argentina", dto.getNombre());
    }

    @Test
    void rolDTO_constructor_shouldSetValues() {
        // Act
        ConfigResource.RolDTO dto = new ConfigResource.RolDTO("US", "Usuario Salud");

        // Assert
        assertEquals("US", dto.getCodigo());
        assertEquals("Usuario Salud", dto.getDescripcion());
    }

    @Test
    void rolDTO_setters_shouldUpdateValues() {
        // Arrange
        ConfigResource.RolDTO dto = new ConfigResource.RolDTO("AD", "Administrador");

        // Act
        dto.setCodigo("US");
        dto.setDescripcion("Usuario");

        // Assert
        assertEquals("US", dto.getCodigo());
        assertEquals("Usuario", dto.getDescripcion());
    }

    @Test
    void getNacionalidades_shouldReturnNonEmptyList() {
        // Act
        Response response = resource.getNacionalidades();

        // Assert
        @SuppressWarnings("unchecked")
        List<ConfigResource.NacionalidadDTO> nacionalidades = (List<ConfigResource.NacionalidadDTO>) response.getEntity();
        assertFalse(nacionalidades.isEmpty());
    }

    @Test
    void getRoles_shouldReturnNonEmptyList() {
        // Act
        Response response = resource.getRoles();

        // Assert
        @SuppressWarnings("unchecked")
        List<ConfigResource.RolDTO> roles = (List<ConfigResource.RolDTO>) response.getEntity();
        assertFalse(roles.isEmpty());
    }
}


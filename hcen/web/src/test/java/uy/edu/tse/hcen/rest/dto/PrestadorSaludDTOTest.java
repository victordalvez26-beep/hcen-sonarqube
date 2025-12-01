package uy.edu.tse.hcen.rest.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrestadorSaludDTOTest {

    @Test
    void constructor_default_shouldCreateEmpty() {
        // Act
        PrestadorSaludDTO dto = new PrestadorSaludDTO();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getNombre());
        assertNull(dto.getRut());
        assertNull(dto.getContacto());
        assertNull(dto.getUrl());
        assertNull(dto.getDepartamento());
        assertNull(dto.getLocalidad());
        assertNull(dto.getDireccion());
        assertNull(dto.getTelefono());
        assertNull(dto.getEstado());
        assertNull(dto.getInvitationUrl());
    }

    @Test
    void gettersAndSetters_shouldWork() {
        // Arrange
        PrestadorSaludDTO dto = new PrestadorSaludDTO();
        Long id = 1L;
        String nombre = "Clínica Test";
        String rut = "12345678-9";
        String contacto = "contacto@test.com";
        String url = "http://test.com";
        String departamento = "MONTEVIDEO";
        String localidad = "Centro";
        String direccion = "Calle 123";
        String telefono = "099123456";
        String estado = "ACTIVO";
        String invitationUrl = "http://test.com/invite";

        // Act
        dto.setId(id);
        dto.setNombre(nombre);
        dto.setRut(rut);
        dto.setContacto(contacto);
        dto.setUrl(url);
        dto.setDepartamento(departamento);
        dto.setLocalidad(localidad);
        dto.setDireccion(direccion);
        dto.setTelefono(telefono);
        dto.setEstado(estado);
        dto.setInvitationUrl(invitationUrl);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(nombre, dto.getNombre());
        assertEquals(rut, dto.getRut());
        assertEquals(contacto, dto.getContacto());
        assertEquals(url, dto.getUrl());
        assertEquals(departamento, dto.getDepartamento());
        assertEquals(localidad, dto.getLocalidad());
        assertEquals(direccion, dto.getDireccion());
        assertEquals(telefono, dto.getTelefono());
        assertEquals(estado, dto.getEstado());
        assertEquals(invitationUrl, dto.getInvitationUrl());
    }

    @Test
    void setId_withNull_shouldSetNull() {
        // Arrange
        PrestadorSaludDTO dto = new PrestadorSaludDTO();
        dto.setId(1L);

        // Act
        dto.setId(null);

        // Assert
        assertNull(dto.getId());
    }

    @Test
    void setNombre_withEmptyString_shouldSetEmpty() {
        // Arrange
        PrestadorSaludDTO dto = new PrestadorSaludDTO();

        // Act
        dto.setNombre("");

        // Assert
        assertEquals("", dto.getNombre());
    }
}


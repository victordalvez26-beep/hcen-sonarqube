package uy.edu.tse.hcen.rest.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodoPerifericoDTOTest {

    @Test
    void constructor_default_shouldCreateEmpty() {
        // Act
        NodoPerifericoDTO dto = new NodoPerifericoDTO();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getNombre());
        assertNull(dto.getRUT());
        assertNull(dto.getDepartamento());
        assertNull(dto.getLocalidad());
        assertNull(dto.getDireccion());
        assertNull(dto.getContacto());
        assertNull(dto.getUrl());
        assertNull(dto.getNodoPerifericoUrlBase());
        assertNull(dto.getNodoPerifericoUsuario());
        assertNull(dto.getNodoPerifericoPassword());
        assertNull(dto.getEstado());
        assertNull(dto.getFechaAlta());
        assertNull(dto.getAdminNickname());
        assertNull(dto.getActivationUrl());
        assertNull(dto.getActivationToken());
        assertNull(dto.getAdminEmail());
    }

    @Test
    void gettersAndSetters_shouldWork() {
        // Arrange
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        Long id = 1L;
        String nombre = "Clínica Test";
        String rut = "12345678-9";
        String departamento = "MONTEVIDEO";
        String localidad = "Centro";
        String direccion = "Calle 123";
        String contacto = "contacto@test.com";
        String url = "http://test.com";
        String nodoPerifericoUrlBase = "http://nodo.test.com";
        String nodoPerifericoUsuario = "user";
        String nodoPerifericoPassword = "pass";
        String estado = "ACTIVO";
        String fechaAlta = "2023-08-14T12:34:56+00:00";
        String adminNickname = "admin_clinic1";
        String activationUrl = "http://test.com/activate";
        String activationToken = "token123";
        String adminEmail = "admin@test.com";

        // Act
        dto.setId(id);
        dto.setNombre(nombre);
        dto.setRUT(rut);
        dto.setDepartamento(departamento);
        dto.setLocalidad(localidad);
        dto.setDireccion(direccion);
        dto.setContacto(contacto);
        dto.setUrl(url);
        dto.setNodoPerifericoUrlBase(nodoPerifericoUrlBase);
        dto.setNodoPerifericoUsuario(nodoPerifericoUsuario);
        dto.setNodoPerifericoPassword(nodoPerifericoPassword);
        dto.setEstado(estado);
        dto.setFechaAlta(fechaAlta);
        dto.setAdminNickname(adminNickname);
        dto.setActivationUrl(activationUrl);
        dto.setActivationToken(activationToken);
        dto.setAdminEmail(adminEmail);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(nombre, dto.getNombre());
        assertEquals(rut, dto.getRUT());
        assertEquals(departamento, dto.getDepartamento());
        assertEquals(localidad, dto.getLocalidad());
        assertEquals(direccion, dto.getDireccion());
        assertEquals(contacto, dto.getContacto());
        assertEquals(url, dto.getUrl());
        assertEquals(nodoPerifericoUrlBase, dto.getNodoPerifericoUrlBase());
        assertEquals(nodoPerifericoUsuario, dto.getNodoPerifericoUsuario());
        assertEquals(nodoPerifericoPassword, dto.getNodoPerifericoPassword());
        assertEquals(estado, dto.getEstado());
        assertEquals(fechaAlta, dto.getFechaAlta());
        assertEquals(adminNickname, dto.getAdminNickname());
        assertEquals(activationUrl, dto.getActivationUrl());
        assertEquals(activationToken, dto.getActivationToken());
        assertEquals(adminEmail, dto.getAdminEmail());
    }

    @Test
    void setId_withNull_shouldSetNull() {
        // Arrange
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setId(1L);

        // Act
        dto.setId(null);

        // Assert
        assertNull(dto.getId());
    }

    @Test
    void setNombre_withEmptyString_shouldSetEmpty() {
        // Arrange
        NodoPerifericoDTO dto = new NodoPerifericoDTO();

        // Act
        dto.setNombre("");

        // Assert
        assertEquals("", dto.getNombre());
    }

    @Test
    void setRUT_withNull_shouldSetNull() {
        // Arrange
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setRUT("12345678-9");

        // Act
        dto.setRUT(null);

        // Assert
        assertNull(dto.getRUT());
    }
}


package uy.edu.tse.hcen.rest.dto;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.model.Departamento;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioSaludDTOTest {

    @Test
    void constructor_default_shouldCreateEmpty() {
        // Act
        UsuarioSaludDTO dto = new UsuarioSaludDTO();

        // Assert
        assertNotNull(dto);
        assertNull(dto.getTenantId());
        assertNull(dto.getCi());
        assertNull(dto.getNombre());
        assertNull(dto.getApellido());
        assertNull(dto.getFechaNacimiento());
        assertNull(dto.getDireccion());
        assertNull(dto.getTelefono());
        assertNull(dto.getEmail());
        assertNull(dto.getDepartamento());
        assertNull(dto.getLocalidad());
    }

    @Test
    void gettersAndSetters_shouldWork() {
        // Arrange
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        Long tenantId = 1L;
        String ci = "12345678";
        String nombre = "Juan";
        String apellido = "Pérez";
        LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        String direccion = "Calle 123";
        String telefono = "099123456";
        String email = "juan@example.com";
        Departamento departamento = Departamento.MONTEVIDEO;
        String localidad = "Centro";

        // Act
        dto.setTenantId(tenantId);
        dto.setCi(ci);
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setFechaNacimiento(fechaNacimiento);
        dto.setDireccion(direccion);
        dto.setTelefono(telefono);
        dto.setEmail(email);
        dto.setDepartamento(departamento);
        dto.setLocalidad(localidad);

        // Assert
        assertEquals(tenantId, dto.getTenantId());
        assertEquals(ci, dto.getCi());
        assertEquals(nombre, dto.getNombre());
        assertEquals(apellido, dto.getApellido());
        assertEquals(fechaNacimiento, dto.getFechaNacimiento());
        assertEquals(direccion, dto.getDireccion());
        assertEquals(telefono, dto.getTelefono());
        assertEquals(email, dto.getEmail());
        assertEquals(departamento, dto.getDepartamento());
        assertEquals(localidad, dto.getLocalidad());
    }

    @Test
    void toString_shouldContainKeyFields() {
        // Arrange
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setTenantId(1L);
        dto.setCi("12345678");
        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setDepartamento(Departamento.MONTEVIDEO);

        // Act
        String result = dto.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("12345678") || result.contains("tenantId=1"));
        assertTrue(result.contains("Juan") || result.contains("nombre"));
        assertTrue(result.contains("Pérez") || result.contains("apellido"));
        assertTrue(result.contains("MONTEVIDEO") || result.contains("departamento"));
    }

    @Test
    void setTenantId_withNull_shouldSetNull() {
        // Arrange
        UsuarioSaludDTO dto = new UsuarioSaludDTO();
        dto.setTenantId(1L);

        // Act
        dto.setTenantId(null);

        // Assert
        assertNull(dto.getTenantId());
    }
}


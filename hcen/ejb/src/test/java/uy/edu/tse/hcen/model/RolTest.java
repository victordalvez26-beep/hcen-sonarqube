package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para el enum Rol.
 */
class RolTest {

    @Test
    void fromCodigo_validCodigo_shouldReturnRol() {
        assertEquals(Rol.USUARIO_SALUD, Rol.fromCodigo("US"));
        assertEquals(Rol.ADMIN_HCEN, Rol.fromCodigo("AD"));
    }

    @Test
    void fromCodigo_caseInsensitive_shouldReturnRol() {
        assertEquals(Rol.USUARIO_SALUD, Rol.fromCodigo("us"));
        assertEquals(Rol.ADMIN_HCEN, Rol.fromCodigo("ad"));
        assertEquals(Rol.USUARIO_SALUD, Rol.fromCodigo("Us"));
    }

    @Test
    void fromCodigo_invalidCodigo_shouldReturnNull() {
        assertNull(Rol.fromCodigo("XX"));
        assertNull(Rol.fromCodigo("INVALID"));
    }

    @Test
    void fromCodigo_null_shouldReturnNull() {
        assertNull(Rol.fromCodigo(null));
    }

    @Test
    void fromCodigo_empty_shouldReturnNull() {
        assertNull(Rol.fromCodigo(""));
    }

    @Test
    void getCodigo_shouldReturnCorrectCode() {
        assertEquals("US", Rol.USUARIO_SALUD.getCodigo());
        assertEquals("AD", Rol.ADMIN_HCEN.getCodigo());
    }

    @Test
    void getDescripcion_shouldReturnDescription() {
        assertEquals("Usuario de la Salud", Rol.USUARIO_SALUD.getDescripcion());
        assertEquals("Administrador HCEN", Rol.ADMIN_HCEN.getDescripcion());
    }

    @Test
    void getDefault_shouldReturnUsuarioSalud() {
        assertEquals(Rol.USUARIO_SALUD, Rol.getDefault());
    }

    @Test
    void toString_shouldReturnDescripcion() {
        assertEquals("Usuario de la Salud", Rol.USUARIO_SALUD.toString());
        assertEquals("Administrador HCEN", Rol.ADMIN_HCEN.toString());
    }

    @Test
    void values_shouldReturnAllRoles() {
        Rol[] roles = Rol.values();
        assertEquals(2, roles.length);
        assertTrue(roles.length == 2);
    }
}



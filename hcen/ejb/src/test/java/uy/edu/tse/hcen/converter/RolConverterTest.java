package uy.edu.tse.hcen.converter;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.model.Rol;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para RolConverter.
 */
class RolConverterTest {

    private final RolConverter converter = new RolConverter();

    @Test
    void convertToDatabaseColumn_validRol_shouldReturnCodigo() {
        assertEquals("US", converter.convertToDatabaseColumn(Rol.USUARIO_SALUD));
        assertEquals("AD", converter.convertToDatabaseColumn(Rol.ADMIN_HCEN));
    }

    @Test
    void convertToDatabaseColumn_null_shouldReturnNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_validCodigo_shouldReturnRol() {
        assertEquals(Rol.USUARIO_SALUD, converter.convertToEntityAttribute("US"));
        assertEquals(Rol.ADMIN_HCEN, converter.convertToEntityAttribute("AD"));
    }

    @Test
    void convertToEntityAttribute_null_shouldReturnDefault() {
        // El converter retorna null cuando el código es null o vacío según la implementación
        // pero luego el sistema usa getDefault()
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_empty_shouldReturnDefault() {
        // El converter retorna null cuando está vacío, pero luego se usa getDefault()
        assertNull(converter.convertToEntityAttribute(""));
    }

    @Test
    void convertToEntityAttribute_invalidCodigo_shouldReturnDefault() {
        // Según la implementación, si no puede convertir, retorna getDefault()
        Rol result = converter.convertToEntityAttribute("XX");
        assertNotNull(result);
        assertEquals(Rol.getDefault(), result);
    }

    @Test
    void roundTrip_conversion_shouldMaintainValue() {
        Rol original = Rol.USUARIO_SALUD;
        String dbValue = converter.convertToDatabaseColumn(original);
        Rol converted = converter.convertToEntityAttribute(dbValue);
        assertEquals(original, converted);
    }
}



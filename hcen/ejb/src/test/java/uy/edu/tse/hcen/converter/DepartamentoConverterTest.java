package uy.edu.tse.hcen.converter;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.model.Departamento;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para DepartamentoConverter.
 */
class DepartamentoConverterTest {

    private final DepartamentoConverter converter = new DepartamentoConverter();

    @Test
    void convertToDatabaseColumn_validDepartamento_shouldReturnName() {
        assertEquals("MONTEVIDEO", converter.convertToDatabaseColumn(Departamento.MONTEVIDEO));
        assertEquals("CANELONES", converter.convertToDatabaseColumn(Departamento.CANELONES));
        assertEquals("MALDONADO", converter.convertToDatabaseColumn(Departamento.MALDONADO));
    }

    @Test
    void convertToDatabaseColumn_null_shouldReturnNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_validName_shouldReturnDepartamento() {
        assertEquals(Departamento.MONTEVIDEO, converter.convertToEntityAttribute("MONTEVIDEO"));
        assertEquals(Departamento.CANELONES, converter.convertToEntityAttribute("CANELONES"));
        assertEquals(Departamento.MALDONADO, converter.convertToEntityAttribute("MALDONADO"));
    }

    @Test
    void convertToEntityAttribute_null_shouldReturnNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_empty_shouldReturnNull() {
        assertNull(converter.convertToEntityAttribute(""));
    }

    @Test
    void convertToEntityAttribute_invalidName_shouldReturnNull() {
        assertNull(converter.convertToEntityAttribute("INVALID_DEPT"));
        assertNull(converter.convertToEntityAttribute("XX"));
    }

    @Test
    void roundTrip_conversion_shouldMaintainValue() {
        Departamento original = Departamento.MONTEVIDEO;
        String dbValue = converter.convertToDatabaseColumn(original);
        Departamento converted = converter.convertToEntityAttribute(dbValue);
        assertEquals(original, converted);
    }
}



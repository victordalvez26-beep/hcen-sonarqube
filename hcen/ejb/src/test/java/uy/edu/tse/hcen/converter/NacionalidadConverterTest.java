package uy.edu.tse.hcen.converter;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.model.Nacionalidad;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para NacionalidadConverter.
 */
class NacionalidadConverterTest {

    private final NacionalidadConverter converter = new NacionalidadConverter();

    @Test
    void convertToDatabaseColumn_validNacionalidad_shouldReturnCodigo() {
        assertEquals("UY", converter.convertToDatabaseColumn(Nacionalidad.UY));
        assertEquals("AR", converter.convertToDatabaseColumn(Nacionalidad.AR));
        assertEquals("OT", converter.convertToDatabaseColumn(Nacionalidad.OT));
    }

    @Test
    void convertToDatabaseColumn_null_shouldReturnNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_validCodigo_shouldReturnNacionalidad() {
        assertEquals(Nacionalidad.UY, converter.convertToEntityAttribute("UY"));
        assertEquals(Nacionalidad.AR, converter.convertToEntityAttribute("AR"));
        assertEquals(Nacionalidad.OT, converter.convertToEntityAttribute("OT"));
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
    void convertToEntityAttribute_invalidCodigo_shouldReturnNull() {
        assertNull(converter.convertToEntityAttribute("XX"));
        assertNull(converter.convertToEntityAttribute("INVALID"));
    }

    @Test
    void roundTrip_conversion_shouldMaintainValue() {
        Nacionalidad original = Nacionalidad.UY;
        String dbValue = converter.convertToDatabaseColumn(original);
        Nacionalidad converted = converter.convertToEntityAttribute(dbValue);
        assertEquals(original, converted);
    }
}



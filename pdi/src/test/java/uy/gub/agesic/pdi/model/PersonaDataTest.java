package uy.gub.agesic.pdi.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PersonaDataTest {

    private static final String TIPO_DOC = "CI";
    private static final String NUM_DOC = "12345678";
    private static final String NOMBRE1 = "Juan";
    private static final String NOMBRE2 = "Pedro";
    private static final String APELLIDO1 = "Perez";
    private static final String APELLIDO2 = "Gomez";
    private static final String SEXO = "M";
    private static final String NACIONALIDAD = "UY";
    private static final String LUGAR = "Montevideo";

    @Test
    void testConstructorsAndGetters() {
        LocalDate dob = LocalDate.of(1990, 1, 1);
        PersonaData persona = new PersonaData(
                TIPO_DOC, NUM_DOC, NOMBRE1, NOMBRE2, APELLIDO1, APELLIDO2,
                dob, SEXO, NACIONALIDAD, LUGAR, LUGAR
        );

        assertEquals(TIPO_DOC, persona.getTipoDocumento());
        assertEquals(NUM_DOC, persona.getNumeroDocumento());
        assertEquals(NOMBRE1, persona.getPrimerNombre());
        assertEquals(NOMBRE2, persona.getSegundoNombre());
        assertEquals(APELLIDO1, persona.getPrimerApellido());
        assertEquals(APELLIDO2, persona.getSegundoApellido());
        assertEquals(dob, persona.getFechaNacimiento());
        assertEquals(SEXO, persona.getSexo());
        assertEquals(NACIONALIDAD, persona.getNacionalidad());
        assertEquals(LUGAR, persona.getDepartamento());
        assertEquals(LUGAR, persona.getLocalidad());
    }

    @Test
    void testSetters() {
        PersonaData persona = new PersonaData();
        LocalDate dob = LocalDate.of(1990, 1, 1);

        persona.setTipoDocumento(TIPO_DOC);
        persona.setNumeroDocumento(NUM_DOC);
        persona.setPrimerNombre(NOMBRE1);
        persona.setSegundoNombre(NOMBRE2);
        persona.setPrimerApellido(APELLIDO1);
        persona.setSegundoApellido(APELLIDO2);
        persona.setFechaNacimiento(dob);
        persona.setSexo(SEXO);
        persona.setNacionalidad(NACIONALIDAD);
        persona.setDepartamento(LUGAR);
        persona.setLocalidad(LUGAR);

        assertEquals(TIPO_DOC, persona.getTipoDocumento());
        assertEquals(NUM_DOC, persona.getNumeroDocumento());
        assertEquals(NOMBRE1, persona.getPrimerNombre());
        assertEquals(NOMBRE2, persona.getSegundoNombre());
        assertEquals(APELLIDO1, persona.getPrimerApellido());
        assertEquals(APELLIDO2, persona.getSegundoApellido());
        assertEquals(dob, persona.getFechaNacimiento());
        assertEquals(SEXO, persona.getSexo());
        assertEquals(NACIONALIDAD, persona.getNacionalidad());
        assertEquals(LUGAR, persona.getDepartamento());
        assertEquals(LUGAR, persona.getLocalidad());
    }

    @Test
    void getEdadShouldReturnCorrectAge() {
        PersonaData persona = new PersonaData();
        persona.setFechaNacimiento(LocalDate.now().minusYears(25));

        assertEquals(25, persona.getEdad());
    }

    @Test
    void getEdadShouldReturnZeroWhenDobIsNull() {
        PersonaData persona = new PersonaData();
        persona.setFechaNacimiento(null);

        assertEquals(0, persona.getEdad());
    }

    @Test
    void isMayorDeEdadShouldReturnTrueWhenAgeIs18OrMore() {
        PersonaData persona = new PersonaData();
        persona.setFechaNacimiento(LocalDate.now().minusYears(18));
        assertTrue(persona.isMayorDeEdad());

        persona.setFechaNacimiento(LocalDate.now().minusYears(25));
        assertTrue(persona.isMayorDeEdad());
    }

    @Test
    void isMayorDeEdadShouldReturnFalseWhenAgeIsLessThan18() {
        PersonaData persona = new PersonaData();
        persona.setFechaNacimiento(LocalDate.now().minusYears(17));
        assertFalse(persona.isMayorDeEdad());
    }
}

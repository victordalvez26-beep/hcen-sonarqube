package uy.gub.agesic.pdi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uy.gub.agesic.pdi.model.PersonaData;
import uy.gub.agesic.pdi.repository.PersonaRepository;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

    private static final String SUCCESS = "success";
    private static final String MENSAJE = "mensaje";
    private static final String PERSONA = "persona";
    private static final String TOTAL_PERSONAS = "totalPersonas";
    private static final String TOTAL = "total";
    private static final String PERSONAS = "personas";
    private static final String ERROR_MSG = "Error";

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private TestController testController;

    private PersonaData testPersona;

    @BeforeEach
    void setUp() {
        testPersona = new PersonaData();
        testPersona.setNumeroDocumento("12345678");
    }

    @Test
    void agregarPersonaShouldReturnCreatedWhenSuccess() {
        doNothing().when(personaRepository).addPersona(any(PersonaData.class));
        when(personaRepository.findAll()).thenReturn(Collections.singletonList(testPersona));

        ResponseEntity<Map<String, Object>> response = testController.agregarPersona(testPersona);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get(SUCCESS));
        assertEquals("Persona agregada exitosamente", response.getBody().get(MENSAJE));
        assertEquals(testPersona, response.getBody().get(PERSONA));
        assertEquals(1, response.getBody().get(TOTAL_PERSONAS));
    }

    @Test
    void agregarPersonaShouldReturnErrorWhenException() {
        doThrow(new RuntimeException(ERROR_MSG)).when(personaRepository).addPersona(any(PersonaData.class));

        ResponseEntity<Map<String, Object>> response = testController.agregarPersona(testPersona);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get(SUCCESS));
        assertTrue(((String) response.getBody().get(MENSAJE)).contains(ERROR_MSG));
    }

    @Test
    void listarPersonasShouldReturnList() {
        when(personaRepository.findAll()).thenReturn(Collections.singletonList(testPersona));

        ResponseEntity<Map<String, Object>> response = testController.listarPersonas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().get(TOTAL));
        assertNotNull(response.getBody().get(PERSONAS));
    }
}

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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoapManagementControllerTest {

    private static final String CI = "CI";
    private static final String DOC_NUMBER = "12345678";

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private SoapManagementController soapManagementController;

    private PersonaData testPersona;

    @BeforeEach
    void setUp() {
        testPersona = new PersonaData();
        testPersona.setTipoDocumento(CI);
        testPersona.setNumeroDocumento(DOC_NUMBER);
    }

    @Test
    void listarPersonasShouldReturnList() {
        when(personaRepository.findAll()).thenReturn(Collections.singletonList(testPersona));

        ResponseEntity<List<PersonaData>> response = soapManagementController.listarPersonas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void agregarPersonaShouldReturnOkWhenSuccess() {
        when(personaRepository.findByDocumento(CI, DOC_NUMBER)).thenReturn(Optional.empty());
        doNothing().when(personaRepository).addPersona(any(PersonaData.class));

        ResponseEntity<?> response = soapManagementController.agregarPersona(testPersona);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Persona agregada correctamente al servicio SOAP simulado", response.getBody());
    }

    @Test
    void agregarPersonaShouldReturnBadRequestWhenMissingDocType() {
        testPersona.setTipoDocumento(null);

        ResponseEntity<?> response = soapManagementController.agregarPersona(testPersona);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Tipo y Número de documento son obligatorios", response.getBody());
    }

    @Test
    void agregarPersonaShouldReturnBadRequestWhenMissingDocNumber() {
        testPersona.setNumeroDocumento(null);

        ResponseEntity<?> response = soapManagementController.agregarPersona(testPersona);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Tipo y Número de documento son obligatorios", response.getBody());
    }

    @Test
    void agregarPersonaShouldReturnConflictWhenExists() {
        when(personaRepository.findByDocumento(CI, DOC_NUMBER)).thenReturn(Optional.of(testPersona));

        ResponseEntity<?> response = soapManagementController.agregarPersona(testPersona);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Ya existe una persona con ese documento", response.getBody());
    }

    @Test
    void agregarPersonaShouldReturnInternalServerErrorWhenException() {
        when(personaRepository.findByDocumento(CI, DOC_NUMBER)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = soapManagementController.agregarPersona(testPersona);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error al agregar persona"));
    }
}

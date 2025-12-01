package uy.gub.agesic.pdi.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uy.gub.agesic.pdi.model.PersonaData;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PersonaRepositoryTest {

    private static final String DOCUMENTO_EXISTENTE = "50830691";
    private PersonaRepository personaRepository;

    @BeforeEach
    void setUp() {
        personaRepository = new PersonaRepository();
        personaRepository.init();
    }

    @Test
    void initShouldLoadInitialData() {
        List<PersonaData> personas = personaRepository.findAll();
        assertFalse(personas.isEmpty());
        assertTrue(personas.size() >= 5);
    }

    @Test
    void findByDocumentoWithTypeAndNumberShouldReturnPersonaWhenExists() {
        Optional<PersonaData> result = personaRepository.findByDocumento("CI", DOCUMENTO_EXISTENTE);
        
        assertTrue(result.isPresent());
        assertEquals("Victor", result.get().getPrimerNombre());
    }

    @Test
    void findByDocumentoWithTypeAndNumberShouldReturnEmptyWhenNotExists() {
        Optional<PersonaData> result = personaRepository.findByDocumento("CI", "99999999");
        
        assertFalse(result.isPresent());
    }

    @Test
    void findByDocumentoWithNumberOnlyShouldReturnPersonaWhenExists() {
        Optional<PersonaData> result = personaRepository.findByDocumento(DOCUMENTO_EXISTENTE);
        
        assertTrue(result.isPresent());
        assertEquals("Victor", result.get().getPrimerNombre());
    }

    @Test
    void findByDocumentoWithNumberOnlyShouldReturnEmptyWhenNotExists() {
        Optional<PersonaData> result = personaRepository.findByDocumento("99999999");
        
        assertFalse(result.isPresent());
    }

    @Test
    void addPersonaShouldAddNewPersona() {
        PersonaData newPersona = new PersonaData(
            "CI", "11111111", "Test", "User", "A", "B", LocalDate.now(), "M", "UY", "MVD", "MVD"
        );
        
        int initialSize = personaRepository.findAll().size();
        personaRepository.addPersona(newPersona);
        
        assertEquals(initialSize + 1, personaRepository.findAll().size());
        assertTrue(personaRepository.findByDocumento("11111111").isPresent());
    }

    @Test
    void addPersonaShouldUpdateExistingPersona() {
        PersonaData existingPersona = personaRepository.findByDocumento(DOCUMENTO_EXISTENTE).get();
        existingPersona.setPrimerNombre("UpdatedName");
        
        int initialSize = personaRepository.findAll().size();
        personaRepository.addPersona(existingPersona);
        
        assertEquals(initialSize, personaRepository.findAll().size()); // Size shouldn't change
        assertEquals("UpdatedName", personaRepository.findByDocumento(DOCUMENTO_EXISTENTE).get().getPrimerNombre());
    }
}

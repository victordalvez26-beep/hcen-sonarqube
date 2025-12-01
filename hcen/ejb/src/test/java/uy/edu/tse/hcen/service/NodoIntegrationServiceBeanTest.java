package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.model.NodoPeriferico;
import static org.junit.jupiter.api.Assertions.*;

class NodoIntegrationServiceBeanTest {

    private NodoIntegrationServiceBean service;

    @BeforeEach
    void setUp() {
        service = new NodoIntegrationServiceBean();
    }

    @Test
    void checkAndUpdateEstado_shouldNotThrowException() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Test Clinic");
        
        assertDoesNotThrow(() -> {
            service.checkAndUpdateEstado(nodo);
        });
    }

    @Test
    void checkAndUpdateEstado_withNull_shouldNotThrowException() {
        assertDoesNotThrow(() -> {
            service.checkAndUpdateEstado(null);
        });
    }
}

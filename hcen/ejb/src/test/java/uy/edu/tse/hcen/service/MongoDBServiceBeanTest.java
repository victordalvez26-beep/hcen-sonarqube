package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MongoDBServiceBeanTest {

    private MongoDBServiceBean service;

    @BeforeEach
    void setUp() {
        service = new MongoDBServiceBean();
    }

    @Test
    void health_shouldReturnTrue() {
        boolean result = service.health();
        assertTrue(result);
    }

    @Test
    void insertDocument_shouldNotThrowException() {
        assertDoesNotThrow(() -> {
            service.insertDocument("{\"test\": \"data\"}");
        });
    }

    @Test
    void insertDocument_withNull_shouldNotThrowException() {
        assertDoesNotThrow(() -> {
            service.insertDocument(null);
        });
    }

    @Test
    void findByCodigo_shouldReturnNull() {
        String result = service.findByCodigo("test-code");
        assertNull(result);
    }

    @Test
    void findByCodigo_withNull_shouldReturnNull() {
        String result = service.findByCodigo(null);
        assertNull(result);
    }
}

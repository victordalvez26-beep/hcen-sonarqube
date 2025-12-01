package uy.edu.tse.hcen.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para UserClinicAssociation.
 */
class UserClinicAssociationTest {

    @Test
    void constructor_default_shouldCreateEmpty() {
        UserClinicAssociation association = new UserClinicAssociation();
        
        assertNull(association.getId());
        assertNull(association.getUserId());
        assertNull(association.getClinicTenantId());
        assertNull(association.getFechaAlta());
    }

    @Test
    void constructor_withParams_shouldSetFields() {
        UserClinicAssociation association = new UserClinicAssociation(1L, 100L);
        
        assertEquals(1L, association.getUserId());
        assertEquals(100L, association.getClinicTenantId());
        assertNotNull(association.getFechaAlta());
    }

    @Test
    void gettersAndSetters_shouldWork() {
        UserClinicAssociation association = new UserClinicAssociation();
        association.setId(1L);
        association.setUserId(2L);
        association.setClinicTenantId(200L);
        LocalDateTime fecha = LocalDateTime.now();
        association.setFechaAlta(fecha);
        
        assertEquals(1L, association.getId());
        assertEquals(2L, association.getUserId());
        assertEquals(200L, association.getClinicTenantId());
        assertEquals(fecha, association.getFechaAlta());
    }

    @Test
    void toString_shouldIncludeFields() {
        UserClinicAssociation association = new UserClinicAssociation(1L, 100L);
        association.setId(1L);
        
        String str = association.toString();
        
        assertTrue(str.contains("UserClinicAssociation"));
        assertTrue(str.contains("userId=1"));
        assertTrue(str.contains("clinicTenantId=100"));
    }
}



package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.Nacionalidad;
import uy.edu.tse.hcen.model.User;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios adicionales para AuthService.
 * Cubre casos más complejos de getUserInfo.
 */
class AuthServiceExtendedTest {

    @Mock
    private UserDAO userDAO;

    private AuthService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new AuthService();
        Field daoField = AuthService.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(service, userDAO);
    }

    @Test
    void extractJsonField_expiresInNumeric_shouldExtract() throws Exception {
        Method method = AuthService.class.getDeclaredMethod("extractJsonField", String.class, String.class);
        method.setAccessible(true);
        
        String json = "{\"expires_in\":3600}";
        String result = (String) method.invoke(service, json, "expires_in");
        
        assertNotNull(result);
        assertEquals("3600", result);
    }

    @Test
    void extractJsonField_booleanValue_shouldExtract() throws Exception {
        Method method = AuthService.class.getDeclaredMethod("extractJsonField", String.class, String.class);
        method.setAccessible(true);
        
        String json = "{\"active\":true}";
        String result = (String) method.invoke(service, json, "active");
        
        assertNotNull(result);
    }

    @Test
    void extractJsonField_nestedJson_shouldExtractFirst() throws Exception {
        Method method = AuthService.class.getDeclaredMethod("extractJsonField", String.class, String.class);
        method.setAccessible(true);
        
        String json = "{\"field\":\"value\",\"nested\":{\"inner\":\"data\"}}";
        String result = (String) method.invoke(service, json, "field");
        
        assertEquals("value", result);
    }

    @Test
    void extractJsonField_specialCharacters_shouldExtract() throws Exception {
        Method method = AuthService.class.getDeclaredMethod("extractJsonField", String.class, String.class);
        method.setAccessible(true);
        
        String json = "{\"field\":\"value-with-dashes_and_underscores\"}";
        String result = (String) method.invoke(service, json, "field");
        
        assertNotNull(result);
        assertTrue(result.contains("value"));
    }
}



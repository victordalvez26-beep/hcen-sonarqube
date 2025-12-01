package uy.edu.tse.hcen.rest;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RestApplicationTest {

    @Test
    void getClasses_shouldReturnNonEmptySet() {
        // Arrange
        RestApplication app = new RestApplication();

        // Act
        Set<Class<?>> classes = app.getClasses();

        // Assert
        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        assertTrue(classes.size() > 0);
    }

    @Test
    void getClasses_shouldContainCorsFilters() {
        // Arrange
        RestApplication app = new RestApplication();

        // Act
        Set<Class<?>> classes = app.getClasses();

        // Assert
        assertTrue(classes.contains(uy.edu.tse.hcen.rest.filter.CorsRequestFilter.class));
        assertTrue(classes.contains(uy.edu.tse.hcen.rest.filter.CorsResponseFilter.class));
    }

    @Test
    void getClasses_shouldContainResources() {
        // Arrange
        RestApplication app = new RestApplication();

        // Act
        Set<Class<?>> classes = app.getClasses();

        // Assert
        assertTrue(classes.contains(MetadatosDocumentoResource.class));
        assertTrue(classes.contains(NodoPerifericoResource.class));
        assertTrue(classes.contains(ConfigResource.class));
        assertTrue(classes.contains(UsuarioSaludResource.class));
        assertTrue(classes.contains(PrestadorSaludResource.class));
        assertTrue(classes.contains(UserResource.class));
        assertTrue(classes.contains(AuthResource.class));
        assertTrue(classes.contains(PoliticasAccesoResource.class));
        assertTrue(classes.contains(ReportesResource.class));
        assertTrue(classes.contains(LoginCallbackResource.class));
    }
}


package uy.edu.tse.hcen.cli;

import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.model.NodoPeriferico;
import uy.edu.tse.hcen.model.EstadoNodoPeriferico;
import uy.edu.tse.hcen.model.Departamento;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class LocalTestRunnerTest {

    @Test
    void parsePayload_withValidPayload_shouldParseCorrectly() throws Exception {
        // Arrange
        String payload = "RUT:12345678;NOMBRE:Clinica Test;URL:https://test.com;DEPARTAMENTO:MONTEVIDEO;LOCALIDAD:Centro;DIRECCION:Av. Test 123;CONTACTO:contact@test.com;USUARIO:user;PASSWORD:pass";

        // Act - Use reflection to access private method
        Method parseMethod = LocalTestRunner.class.getDeclaredMethod("parsePayload", String.class);
        parseMethod.setAccessible(true);
        NodoPeriferico nodo = (NodoPeriferico) parseMethod.invoke(null, payload);

        // Assert
        assertNotNull(nodo);
        assertEquals("12345678", nodo.getRUT());
        assertEquals("Clinica Test", nodo.getNombre());
        assertEquals("https://test.com", nodo.getNodoPerifericoUrlBase());
        assertEquals(Departamento.MONTEVIDEO, nodo.getDepartamento());
        assertEquals("Centro", nodo.getLocalidad());
        assertEquals("Av. Test 123", nodo.getDireccion());
        assertEquals("contact@test.com", nodo.getContacto());
        assertEquals("user", nodo.getNodoPerifericoUsuario());
        assertEquals("pass", nodo.getNodoPerifericoPassword());
    }

    @Test
    void parsePayload_withMinimalPayload_shouldParseCorrectly() throws Exception {
        // Arrange
        String payload = "RUT:123;NOMBRE:Test";

        // Act
        Method parseMethod = LocalTestRunner.class.getDeclaredMethod("parsePayload", String.class);
        parseMethod.setAccessible(true);
        NodoPeriferico nodo = (NodoPeriferico) parseMethod.invoke(null, payload);

        // Assert
        assertNotNull(nodo);
        assertEquals("123", nodo.getRUT());
        assertEquals("Test", nodo.getNombre());
    }

    @Test
    void parsePayload_withInvalidFormat_shouldHandleGracefully() throws Exception {
        // Arrange
        String payload = "INVALID_FORMAT_NO_COLON;RUT:123;NOMBRE:Test";

        // Act
        Method parseMethod = LocalTestRunner.class.getDeclaredMethod("parsePayload", String.class);
        parseMethod.setAccessible(true);
        NodoPeriferico nodo = (NodoPeriferico) parseMethod.invoke(null, payload);

        // Assert
        assertNotNull(nodo);
        assertEquals("123", nodo.getRUT());
        assertEquals("Test", nodo.getNombre());
    }

    @Test
    void parsePayload_withInvalidDepartamento_shouldHandleGracefully() throws Exception {
        // Arrange
        String payload = "RUT:123;NOMBRE:Test;DEPARTAMENTO:INVALID_DEPT";

        // Act
        Method parseMethod = LocalTestRunner.class.getDeclaredMethod("parsePayload", String.class);
        parseMethod.setAccessible(true);
        NodoPeriferico nodo = (NodoPeriferico) parseMethod.invoke(null, payload);

        // Assert
        assertNotNull(nodo);
        assertEquals("123", nodo.getRUT());
        assertEquals("Test", nodo.getNombre());
        // Departamento should be null or default since invalid value was provided
    }

    @Test
    void parsePayload_withUnknownKey_shouldIgnore() throws Exception {
        // Arrange
        String payload = "RUT:123;NOMBRE:Test;UNKNOWN_KEY:value";

        // Act
        Method parseMethod = LocalTestRunner.class.getDeclaredMethod("parsePayload", String.class);
        parseMethod.setAccessible(true);
        NodoPeriferico nodo = (NodoPeriferico) parseMethod.invoke(null, payload);

        // Assert
        assertNotNull(nodo);
        assertEquals("123", nodo.getRUT());
        assertEquals("Test", nodo.getNombre());
    }

    @Test
    void nodoToString_shouldFormatCorrectly() throws Exception {
        // Arrange
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setRUT("12345678");
        nodo.setNombre("Test Clinic");
        nodo.setNodoPerifericoUrlBase("https://test.com");
        nodo.setEstado(EstadoNodoPeriferico.ACTIVO);

        // Act
        Method toStringMethod = LocalTestRunner.class.getDeclaredMethod("nodoToString", NodoPeriferico.class);
        toStringMethod.setAccessible(true);
        String result = (String) toStringMethod.invoke(null, nodo);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("RUT=12345678"));
        assertTrue(result.contains("nombre=Test Clinic"));
        assertTrue(result.contains("url=https://test.com"));
        assertTrue(result.contains("estado=ACTIVO"));
    }

    @Test
    void inMemoryNodoRepo_create_shouldCreateAndStore() {
        // Arrange
        LocalTestRunner.InMemoryNodoRepo repo = new LocalTestRunner.InMemoryNodoRepo();
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setRUT("123");
        nodo.setNombre("Test");

        // Act
        NodoPeriferico created = repo.create(nodo);

        // Assert
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(EstadoNodoPeriferico.PENDIENTE, created.getEstado());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void inMemoryNodoRepo_update_shouldUpdateExisting() {
        // Arrange
        LocalTestRunner.InMemoryNodoRepo repo = new LocalTestRunner.InMemoryNodoRepo();
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setRUT("123");
        nodo.setNombre("Original");
        repo.create(nodo);

        // Act
        nodo.setNombre("Updated");
        NodoPeriferico updated = repo.update(nodo);

        // Assert
        assertEquals("Updated", updated.getNombre());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void inMemoryNodoRepo_findById_shouldReturnCorrectNodo() {
        // Arrange
        LocalTestRunner.InMemoryNodoRepo repo = new LocalTestRunner.InMemoryNodoRepo();
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setRUT("123");
        NodoPeriferico created = repo.create(nodo);
        Long id = created.getId();

        // Act
        NodoPeriferico found = repo.find(id);

        // Assert
        assertNotNull(found);
        assertEquals(id, found.getId());
        assertEquals("123", found.getRUT());
    }

    @Test
    void inMemoryNodoRepo_findByRUT_shouldReturnCorrectNodo() {
        // Arrange
        LocalTestRunner.InMemoryNodoRepo repo = new LocalTestRunner.InMemoryNodoRepo();
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setRUT("12345678");
        repo.create(nodo);

        // Act
        NodoPeriferico found = repo.findByRUT("12345678");

        // Assert
        assertNotNull(found);
        assertEquals("12345678", found.getRUT());
    }

    @Test
    void inMemoryNodoRepo_delete_shouldRemoveNodo() {
        // Arrange
        LocalTestRunner.InMemoryNodoRepo repo = new LocalTestRunner.InMemoryNodoRepo();
        NodoPeriferico nodo = new NodoPeriferico();
        NodoPeriferico created = repo.create(nodo);
        Long id = created.getId();

        // Act
        repo.delete(id);

        // Assert
        assertNull(repo.find(id));
        assertEquals(0, repo.findAll().size());
    }

    @Test
    void inMemoryIntegrationService_checkAndUpdateEstado_withSimulateOk_shouldSetActivo() {
        // Arrange
        LocalTestRunner.InMemoryNodoRepo repo = new LocalTestRunner.InMemoryNodoRepo();
        LocalTestRunner.InMemoryIntegrationService integration = 
            new LocalTestRunner.InMemoryIntegrationService(repo);
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("https://test.com/simulate-ok");
        nodo.setEstado(EstadoNodoPeriferico.PENDIENTE);
        repo.create(nodo);

        // Act
        integration.checkAndUpdateEstado(nodo);

        // Assert
        assertEquals(EstadoNodoPeriferico.ACTIVO, nodo.getEstado());
    }

    @Test
    void inMemoryIntegrationService_checkAndUpdateEstado_withMaintenance_shouldSetMantenimiento() {
        // Arrange
        LocalTestRunner.InMemoryNodoRepo repo = new LocalTestRunner.InMemoryNodoRepo();
        LocalTestRunner.InMemoryIntegrationService integration = 
            new LocalTestRunner.InMemoryIntegrationService(repo);
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("https://test.com/maintenance");
        nodo.setEstado(EstadoNodoPeriferico.PENDIENTE);
        repo.create(nodo);

        // Act
        integration.checkAndUpdateEstado(nodo);

        // Assert
        assertEquals(EstadoNodoPeriferico.MANTENIMIENTO, nodo.getEstado());
    }

    @Test
    void inMemoryIntegrationService_checkAndUpdateEstado_withNullUrl_shouldSetPendiente() {
        // Arrange
        LocalTestRunner.InMemoryNodoRepo repo = new LocalTestRunner.InMemoryNodoRepo();
        LocalTestRunner.InMemoryIntegrationService integration = 
            new LocalTestRunner.InMemoryIntegrationService(repo);
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase(null);
        nodo.setEstado(EstadoNodoPeriferico.ACTIVO);
        repo.create(nodo);

        // Act
        integration.checkAndUpdateEstado(nodo);

        // Assert
        assertEquals(EstadoNodoPeriferico.PENDIENTE, nodo.getEstado());
    }

    @Test
    void inMemoryIntegrationService_checkAndUpdateEstado_withOtherUrl_shouldSetInactivo() {
        // Arrange
        LocalTestRunner.InMemoryNodoRepo repo = new LocalTestRunner.InMemoryNodoRepo();
        LocalTestRunner.InMemoryIntegrationService integration = 
            new LocalTestRunner.InMemoryIntegrationService(repo);
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("https://test.com/other");
        nodo.setEstado(EstadoNodoPeriferico.PENDIENTE);
        repo.create(nodo);

        // Act
        integration.checkAndUpdateEstado(nodo);

        // Assert
        assertEquals(EstadoNodoPeriferico.INACTIVO, nodo.getEstado());
    }
}


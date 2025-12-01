package uy.edu.tse.hcen.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.NodoPeriferico;
import uy.edu.tse.hcen.repository.NodoPerifericoRepository;
import uy.edu.tse.hcen.rest.dto.NodoPerifericoDTO;
import uy.edu.tse.hcen.rest.dto.NodoPerifericoConverter;

import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NodoPerifericoResourceTest {

    @Mock
    private NodoPerifericoRepository repo;

    @Mock
    private uy.edu.tse.hcen.service.NodoService nodoService;

    private NodoPerifericoResource resource;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        resource = new NodoPerifericoResource();
        // inject mock repo via reflection
        java.lang.reflect.Field f = NodoPerifericoResource.class.getDeclaredField("repo");
        f.setAccessible(true);
        f.set(resource, repo);
        // inject mock nodoService via reflection
        java.lang.reflect.Field f2 = NodoPerifericoResource.class.getDeclaredField("nodoService");
        f2.setAccessible(true);
        f2.set(resource, nodoService);
    }

    @Test
    public void create_valid_shouldCallRepoAndReturnCreated() {
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Centro A");
        dto.setRUT("12345678-9");
        dto.setDepartamento("MONTEVIDEO");
        dto.setEstado("ACTIVO");

        NodoPeriferico saved = NodoPerifericoConverter.toEntity(dto);
        saved.setId(10L);

        when(repo.findByRUT(anyString())).thenReturn(null); // No existe duplicado
        when(nodoService.createAndNotify(any(NodoPeriferico.class))).thenReturn(saved);

        Response resp = resource.create(dto);

        assertEquals(201, resp.getStatus());
        NodoPerifericoDTO returned = (NodoPerifericoDTO) resp.getEntity();
        assertNotNull(returned);
        assertEquals(10L, returned.getId());

        verify(nodoService, times(1)).createAndNotify(any(NodoPeriferico.class));
    }

    @Test
    public void create_invalidEnum_shouldReturnBadRequest() {
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Centro B");
        dto.setRUT("87654321-0");
        dto.setDepartamento("NO_SU_DEPARTAMENTO");
        dto.setEstado("ACTIVO");

        Response resp = resource.create(dto);
        assertEquals(400, resp.getStatus());
        String msg = (String) resp.getEntity();
        assertTrue(msg.contains("Invalid departamento value"));
    }

    @Test
    public void create_duplicateRUT_shouldReturnConflict() {
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Centro A");
        dto.setRUT("12345678-9");
        dto.setDepartamento("MONTEVIDEO");
        dto.setEstado("ACTIVO");

        NodoPeriferico existing = new NodoPeriferico();
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);

        Response resp = resource.create(dto);
        assertEquals(409, resp.getStatus());
    }

    @Test
    public void list_shouldReturnAllNodos() {
        NodoPeriferico nodo1 = new NodoPeriferico();
        nodo1.setId(1L);
        nodo1.setNombre("Centro 1");
        NodoPeriferico nodo2 = new NodoPeriferico();
        nodo2.setId(2L);
        nodo2.setNombre("Centro 2");
        when(repo.findAll()).thenReturn(List.of(nodo1, nodo2));

        List<NodoPerifericoDTO> result = resource.list();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void get_existingRUT_shouldReturnOk() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setRUT("12345678-9");
        nodo.setNombre("Centro A");
        when(repo.findByRUT("12345678-9")).thenReturn(nodo);

        Response resp = resource.get("12345678-9");

        assertEquals(200, resp.getStatus());
        assertNotNull(resp.getEntity());
    }

    @Test
    public void get_nonExistingRUT_shouldReturnNotFound() {
        when(repo.findByRUT("99999999-9")).thenReturn(null);

        Response resp = resource.get("99999999-9");

        assertEquals(404, resp.getStatus());
    }

    @Test
    public void update_existingNodo_shouldReturnOk() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);

        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Centro Actualizado");
        dto.setRUT("12345678-9");
        dto.setDepartamento("MONTEVIDEO");
        dto.setEstado("ACTIVO");

        NodoPeriferico updated = new NodoPeriferico();
        updated.setId(1L);
        updated.setNombre("Centro Actualizado");
        when(repo.update(any(NodoPeriferico.class))).thenReturn(updated);

        Response resp = resource.update("12345678-9", dto);

        assertEquals(200, resp.getStatus());
    }

    @Test
    public void update_nonExistingNodo_shouldReturnNotFound() {
        when(repo.findByRUT("99999999-9")).thenReturn(null);

        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        Response resp = resource.update("99999999-9", dto);

        assertEquals(404, resp.getStatus());
    }

    @Test
    public void update_invalidEnum_shouldReturnBadRequest() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);

        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setDepartamento("INVALID_DEPARTAMENTO");

        Response resp = resource.update("12345678-9", dto);

        assertEquals(400, resp.getStatus());
    }

    @Test
    public void delete_existingNodo_shouldReturnNoContent() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        existing.setNombre("Centro A");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);
        doNothing().when(nodoService).updateEstado(1L, uy.edu.tse.hcen.model.EstadoNodoPeriferico.INACTIVO);

        Response resp = resource.delete("12345678-9");

        assertEquals(200, resp.getStatus());
        verify(nodoService).updateEstado(1L, uy.edu.tse.hcen.model.EstadoNodoPeriferico.INACTIVO);
    }

    @Test
    public void delete_nonExistingNodo_shouldReturnNotFound() {
        when(repo.findByRUT("99999999-9")).thenReturn(null);

        Response resp = resource.delete("99999999-9");

        assertEquals(404, resp.getStatus());
        verify(repo, never()).delete(anyLong());
    }

    @Test
    public void notifyExisting_existingNodo_shouldReturnOk() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);
        doNothing().when(nodoService).notifyPeripheralNode(1L, "init");

        Response resp = resource.notifyExisting("12345678-9");

        assertEquals(200, resp.getStatus());
        verify(nodoService).notifyPeripheralNode(1L, "init");
    }

    @Test
    public void notifyExisting_nonExistingNodo_shouldReturnNotFound() {
        when(repo.findByRUT("99999999-9")).thenReturn(null);

        Response resp = resource.notifyExisting("99999999-9");

        assertEquals(404, resp.getStatus());
    }

    @Test
    public void notifyExisting_serviceException_shouldReturnInternalError() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);
        doThrow(new RuntimeException("Service error"))
            .when(nodoService).notifyPeripheralNode(1L, "init");

        Response resp = resource.notifyExisting("12345678-9");

        assertEquals(500, resp.getStatus());
    }

    @Test
    public void createAndNotify_valid_shouldCallService() {
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Centro A");
        dto.setRUT("12345678-9");
        dto.setDepartamento("MONTEVIDEO");
        dto.setEstado("ACTIVO");

        NodoPeriferico saved = NodoPerifericoConverter.toEntity(dto);
        saved.setId(10L);

        when(nodoService.createAndNotify(any(NodoPeriferico.class))).thenReturn(saved);

        Response resp = resource.createAndNotify(dto);

        assertEquals(201, resp.getStatus());
        verify(nodoService, times(1)).createAndNotify(any(NodoPeriferico.class));
    }

    @Test
    public void createAndNotify_invalidEnum_shouldReturnBadRequest() {
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Centro B");
        dto.setDepartamento("INVALID_DEPT");
        dto.setEstado("ACTIVO");

        Response resp = resource.createAndNotify(dto);

        assertEquals(400, resp.getStatus());
    }

    @Test
    public void createAndNotify_constraintViolation_shouldReturnBadRequest() {
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre(""); // Invalid - should trigger constraint violation

        java.util.Set<jakarta.validation.ConstraintViolation<?>> violations = java.util.Collections.emptySet();
        when(nodoService.createAndNotify(any(NodoPeriferico.class)))
            .thenThrow(new jakarta.validation.ConstraintViolationException(violations));

        Response resp = resource.createAndNotify(dto);

        assertEquals(400, resp.getStatus());
    }

    @Test
    public void createAndNotify_runtimeException_shouldReturnInternalError() {
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Centro A");
        dto.setDepartamento("MONTEVIDEO");
        dto.setEstado("ACTIVO");

        when(nodoService.createAndNotify(any(NodoPeriferico.class)))
            .thenThrow(new RuntimeException("Service error"));

        Response resp = resource.createAndNotify(dto);

        assertEquals(500, resp.getStatus());
    }

    @Test
    public void update_duplicateRUT_shouldReturnConflict() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);

        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setRUT("99999999-9"); // Cambiando RUT
        dto.setDepartamento("MONTEVIDEO");
        dto.setEstado("ACTIVO");

        NodoPeriferico duplicate = new NodoPeriferico();
        duplicate.setRUT("99999999-9");
        when(repo.findByRUT("99999999-9")).thenReturn(duplicate);

        Response resp = resource.update("12345678-9", dto);

        assertEquals(409, resp.getStatus());
    }

    @Test
    public void update_constraintViolation_shouldReturnBadRequest() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);
        java.util.Set<jakarta.validation.ConstraintViolation<?>> violations = java.util.Collections.emptySet();
        when(repo.update(any(NodoPeriferico.class)))
            .thenThrow(new jakarta.validation.ConstraintViolationException(violations));

        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Updated");
        dto.setRUT("12345678-9");
        dto.setDepartamento("MONTEVIDEO");
        dto.setEstado("ACTIVO");

        Response resp = resource.update("12345678-9", dto);

        assertEquals(400, resp.getStatus());
    }

    @Test
    public void completeRegistration_existingNodo_shouldUpdate() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.find(1L)).thenReturn(existing);
        when(repo.update(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> completionData = new HashMap<>();
        completionData.put("rut", "12345678-9");
        completionData.put("departamento", "MONTEVIDEO");
        completionData.put("localidad", "Centro");
        completionData.put("direccion", "Calle 123");
        completionData.put("adminNickname", "admin_clinic1");

        Response resp = resource.completeRegistration(1L, completionData);

        assertEquals(200, resp.getStatus());
        verify(repo).update(any(NodoPeriferico.class));
    }

    @Test
    public void completeRegistration_notFound_shouldReturnNotFound() {
        when(repo.find(999L)).thenReturn(null);

        Map<String, String> completionData = new HashMap<>();
        Response resp = resource.completeRegistration(999L, completionData);

        assertEquals(404, resp.getStatus());
    }

    @Test
    public void completeRegistration_invalidDepartamento_shouldHandleGracefully() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        when(repo.find(1L)).thenReturn(existing);
        when(repo.update(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> completionData = new HashMap<>();
        completionData.put("departamento", "INVALID_DEPT");

        Response resp = resource.completeRegistration(1L, completionData);

        assertEquals(200, resp.getStatus());
        verify(repo).update(any(NodoPeriferico.class));
    }

    @Test
    public void completeRegistration_exception_shouldReturnInternalError() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        when(repo.find(1L)).thenReturn(existing);
        when(repo.update(any(NodoPeriferico.class)))
            .thenThrow(new RuntimeException("Database error"));

        Map<String, String> completionData = new HashMap<>();
        Response resp = resource.completeRegistration(1L, completionData);

        assertEquals(500, resp.getStatus());
    }

    @Test
    public void updateConfig_existingNodo_shouldUpdate() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);
        when(repo.update(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(nodoService).notifyPeripheralNode(1L, "update");

        NodoPerifericoDTO cfg = new NodoPerifericoDTO();
        cfg.setNodoPerifericoUrlBase("http://new-url.com");

        Response resp = resource.updateConfig("12345678-9", cfg);

        assertEquals(200, resp.getStatus());
        verify(repo).update(any(NodoPeriferico.class));
        verify(nodoService).notifyPeripheralNode(1L, "update");
    }

    @Test
    public void updateConfig_notFound_shouldReturnNotFound() {
        when(repo.findByRUT("99999999-9")).thenReturn(null);

        NodoPerifericoDTO cfg = new NodoPerifericoDTO();
        cfg.setNodoPerifericoUrlBase("http://new-url.com");

        Response resp = resource.updateConfig("99999999-9", cfg);

        assertEquals(404, resp.getStatus());
    }

    @Test
    public void updateConfig_nullUrl_shouldReturnBadRequest() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);

        NodoPerifericoDTO cfg = new NodoPerifericoDTO();
        cfg.setNodoPerifericoUrlBase(null);

        Response resp = resource.updateConfig("12345678-9", cfg);

        assertEquals(400, resp.getStatus());
    }

    @Test
    public void updateConfig_urlTooLong_shouldReturnBadRequest() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);

        NodoPerifericoDTO cfg = new NodoPerifericoDTO();
        cfg.setNodoPerifericoUrlBase("http://" + "x".repeat(300)); // Más de 255 caracteres

        Response resp = resource.updateConfig("12345678-9", cfg);

        assertEquals(400, resp.getStatus());
    }

    @Test
    public void updateConfig_notificationFails_shouldReturnAccepted() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setRUT("12345678-9");
        when(repo.findByRUT("12345678-9")).thenReturn(existing);
        when(repo.update(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Notification failed"))
            .when(nodoService).notifyPeripheralNode(1L, "update");

        NodoPerifericoDTO cfg = new NodoPerifericoDTO();
        cfg.setNodoPerifericoUrlBase("http://new-url.com");

        Response resp = resource.updateConfig("12345678-9", cfg);

        assertEquals(202, resp.getStatus()); // ACCEPTED
    }

    @Test
    public void create_persistenceException_shouldReturnInternalError() {
        NodoPerifericoDTO dto = new NodoPerifericoDTO();
        dto.setNombre("Centro A");
        dto.setRUT("12345678-9");
        dto.setDepartamento("MONTEVIDEO");
        dto.setEstado("ACTIVO");

        when(repo.findByRUT(anyString())).thenReturn(null);
        jakarta.persistence.PersistenceException pe = new jakarta.persistence.PersistenceException("Database error");
        when(nodoService.createAndNotify(any(NodoPeriferico.class)))
            .thenThrow(pe);

        Response resp = resource.create(dto);

        // Puede retornar 409 o 500 dependiendo de cómo se detecte
        assertTrue(resp.getStatus() == 409 || resp.getStatus() == 500);
    }
}

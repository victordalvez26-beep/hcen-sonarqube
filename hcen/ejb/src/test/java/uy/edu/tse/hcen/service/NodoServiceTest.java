package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.EstadoNodoPeriferico;
import uy.edu.tse.hcen.model.NodoPeriferico;
import uy.edu.tse.hcen.repository.NodoPerifericoRepository;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para NodoService.
 */
class NodoServiceTest {

    @Mock
    private NodoPerifericoRepository repository;

    @Mock
    private uy.edu.tse.hcen.repository.UserClinicAssociationRepository associationRepo;

    @Mock
    private NodoPerifericoHttpClient httpClient;

    @Mock
    private EmailService emailService;

    private NodoService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new NodoService();
        
        // Inyectar mocks mediante reflexión
        Field repoField = NodoService.class.getDeclaredField("repo");
        repoField.setAccessible(true);
        repoField.set(service, repository);
        
        Field associationRepoField = NodoService.class.getDeclaredField("associationRepo");
        associationRepoField.setAccessible(true);
        associationRepoField.set(service, associationRepo);
        
        Field httpField = NodoService.class.getDeclaredField("httpClient");
        httpField.setAccessible(true);
        httpField.set(service, httpClient);
        
        Field emailField = NodoService.class.getDeclaredField("emailService");
        emailField.setAccessible(true);
        emailField.set(service, emailService);
    }

    @Test
    void createAndNotify_validNodo_shouldCreateAndSendInvitation() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Clínica Test");
        nodo.setContacto("admin@clinica.com");
        nodo.setRUT("12345678-9");
        
        NodoPeriferico saved = new NodoPeriferico();
        saved.setId(1L);
        saved.setNombre(nodo.getNombre());
        saved.setContacto(nodo.getContacto());
        saved.setRUT(nodo.getRUT());
        saved.setEstado(EstadoNodoPeriferico.PENDIENTE); // El repositorio lo establece
        
        when(repository.create(any(NodoPeriferico.class))).thenAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            if (n.getEstado() == null) {
                n.setEstado(EstadoNodoPeriferico.PENDIENTE);
            }
            return n;
        });
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));
        when(emailService.sendInvitationEmail(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);
        
        NodoPeriferico result = service.createAndNotify(nodo);
        
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getActivationToken());
        assertNotNull(result.getActivationUrl());
        assertEquals(EstadoNodoPeriferico.PENDIENTE, result.getEstado());
        
        verify(repository, times(1)).create(any(NodoPeriferico.class));
        verify(repository, times(1)).update(any(NodoPeriferico.class));
        verify(emailService, times(1)).sendInvitationEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createAndNotify_emailServiceFails_shouldContinue() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Clínica Test");
        nodo.setContacto("admin@clinica.com");
        
        when(repository.create(any(NodoPeriferico.class))).thenAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            if (n.getEstado() == null) {
                n.setEstado(EstadoNodoPeriferico.PENDIENTE);
            }
            return n;
        });
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        when(emailService.sendInvitationEmail(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(false);
        
        NodoPeriferico result = service.createAndNotify(nodo);
        
        assertNotNull(result);
        // El email se envía si hay un email válido en el contacto
        verify(emailService, times(1)).sendInvitationEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createAndNotify_noEmailInContacto_shouldNotSendEmail() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Clínica Test");
        nodo.setContacto("No email here");
        
        NodoPeriferico saved = new NodoPeriferico();
        saved.setId(1L);
        
        when(repository.create(any(NodoPeriferico.class))).thenReturn(saved);
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        
        NodoPeriferico result = service.createAndNotify(nodo);
        
        assertNotNull(result);
        verify(emailService, never()).sendInvitationEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void updateEstado_validId_shouldUpdate() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setEstado(EstadoNodoPeriferico.PENDIENTE);
        
        when(repository.find(1L)).thenReturn(nodo);
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        
        service.updateEstado(1L, EstadoNodoPeriferico.ACTIVO);
        
        verify(repository, times(1)).find(1L);
        verify(repository, times(1)).update(any(NodoPeriferico.class));
        assertEquals(EstadoNodoPeriferico.ACTIVO, nodo.getEstado());
    }

    @Test
    void updateEstado_invalidId_shouldNotCrash() {
        when(repository.find(999L)).thenReturn(null);
        
        service.updateEstado(999L, EstadoNodoPeriferico.ACTIVO);
        
        verify(repository, times(1)).find(999L);
        verify(repository, never()).update(any(NodoPeriferico.class));
    }

    @Test
    void deleteAndNotify_validId_shouldDelete() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(existing);
        when(httpClient.deleteTenant(anyString(), any(Long.class))).thenReturn(true);
        doNothing().when(repository).delete(1L);
        
        service.deleteAndNotify(1L);
        
        verify(repository, times(1)).find(1L);
        verify(httpClient, times(1)).deleteTenant(anyString(), eq(1L));
        verify(repository, times(1)).delete(1L);
    }

    @Test
    void deleteAndNotify_notFound_shouldNotCrash() {
        when(repository.find(999L)).thenReturn(null);
        
        service.deleteAndNotify(999L);
        
        verify(repository, times(1)).find(999L);
        verify(repository, never()).delete(any(Long.class));
    }

    @Test
    void updateAndNotify_validNodo_shouldUpdateAndNotify() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Updated Clinic");
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        when(httpClient.updateTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class)))
            .thenReturn(true);
        
        NodoPeriferico result = service.updateAndNotify(nodo);
        
        assertNotNull(result);
        verify(repository).update(nodo);
        verify(httpClient).updateTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class));
    }

    @Test
    void updateAndNotify_noUrl_shouldNotCallHttpClient() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase(null);
        
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        
        NodoPeriferico result = service.updateAndNotify(nodo);
        
        assertNotNull(result);
        verify(repository).update(nodo);
        verify(httpClient, never()).updateTenant(anyString(), any());
    }

    @Test
    void updateAndNotify_httpClientFails_shouldContinue() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        when(httpClient.updateTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class)))
            .thenThrow(new RuntimeException("HTTP error"));
        
        NodoPeriferico result = service.updateAndNotify(nodo);
        
        assertNotNull(result);
        verify(repository).update(nodo);
    }

    @Test
    void notifyPeripheralNode_initAction_shouldCallInitialize() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        NodoPerifericoHttpClient.InitResponse response = new NodoPerifericoHttpClient.InitResponse();
        response.success = true;
        
        when(repository.find(1L)).thenReturn(nodo);
        when(httpClient.initializeTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class)))
            .thenReturn(response);
        
        service.notifyPeripheralNode(1L, "init");
        
        verify(httpClient).initializeTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class));
    }

    @Test
    void notifyPeripheralNode_updateAction_shouldCallUpdate() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(nodo);
        when(httpClient.updateTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class)))
            .thenReturn(true);
        
        service.notifyPeripheralNode(1L, "update");
        
        verify(httpClient).updateTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class));
    }

    @Test
    void notifyPeripheralNode_deleteAction_shouldCallDelete() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(nodo);
        when(httpClient.deleteTenant(anyString(), eq(1L))).thenReturn(true);
        
        service.notifyPeripheralNode(1L, "delete");
        
        verify(httpClient).deleteTenant(anyString(), eq(1L));
    }

    @Test
    void notifyPeripheralNode_notFound_shouldThrowException() {
        when(repository.find(999L)).thenReturn(null);
        
        assertThrows(RuntimeException.class, () -> service.notifyPeripheralNode(999L, "init"));
    }

    @Test
    void notifyPeripheralNode_noUrl_shouldThrowException() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase(null);
        
        when(repository.find(1L)).thenReturn(nodo);
        
        assertThrows(RuntimeException.class, () -> service.notifyPeripheralNode(1L, "init"));
    }

    @Test
    void notifyPeripheralNode_emptyUrl_shouldThrowException() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("   ");
        
        when(repository.find(1L)).thenReturn(nodo);
        
        assertThrows(RuntimeException.class, () -> service.notifyPeripheralNode(1L, "init"));
    }

    @Test
    void notifyPeripheralNode_unknownAction_shouldThrowException() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(nodo);
        
        // El método lanza IllegalArgumentException pero el catch lo envuelve en RuntimeException
        // Verificamos que se lance una excepción (RuntimeException que envuelve IllegalArgumentException)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.notifyPeripheralNode(1L, "unknown");
        });
        
        // Verificamos que la causa sea IllegalArgumentException
        assertTrue(exception.getCause() instanceof IllegalArgumentException || 
                  exception.getMessage().contains("Unknown action"));
    }

    @Test
    void notifyPeripheralNode_initActionFails_shouldThrowException() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        NodoPerifericoHttpClient.InitResponse response = new NodoPerifericoHttpClient.InitResponse();
        response.success = false;
        
        when(repository.find(1L)).thenReturn(nodo);
        when(httpClient.initializeTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class)))
            .thenReturn(response);
        
        assertThrows(RuntimeException.class, () -> service.notifyPeripheralNode(1L, "init"));
    }

    @Test
    void notifyPeripheralNode_initActionNullResponse_shouldThrowException() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(nodo);
        when(httpClient.initializeTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class)))
            .thenReturn(null);
        
        assertThrows(RuntimeException.class, () -> service.notifyPeripheralNode(1L, "init"));
    }

    @Test
    void notifyPeripheralNode_updateActionFails_shouldThrowException() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(nodo);
        when(httpClient.updateTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class)))
            .thenReturn(false);
        
        assertThrows(RuntimeException.class, () -> service.notifyPeripheralNode(1L, "update"));
    }

    @Test
    void notifyPeripheralNode_deleteActionFails_shouldThrowException() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(nodo);
        when(httpClient.deleteTenant(anyString(), eq(1L))).thenReturn(false);
        
        assertThrows(RuntimeException.class, () -> service.notifyPeripheralNode(1L, "delete"));
    }

    @Test
    void notifyPeripheralNode_nullAction_shouldCallInit() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        NodoPerifericoHttpClient.InitResponse response = new NodoPerifericoHttpClient.InitResponse();
        response.success = true;
        
        when(repository.find(1L)).thenReturn(nodo);
        when(httpClient.initializeTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class)))
            .thenReturn(response);
        
        service.notifyPeripheralNode(1L, null);
        
        verify(httpClient).initializeTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class));
    }

    @Test
    void notifyPeripheralNode_httpClientThrowsException_shouldPropagate() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(nodo);
        when(httpClient.initializeTenant(anyString(), any(NodoPerifericoHttpClient.NodoInitPayload.class)))
            .thenThrow(new RuntimeException("Network error"));
        
        assertThrows(RuntimeException.class, () -> service.notifyPeripheralNode(1L, "init"));
    }

    @Test
    void deleteAndNotify_httpClientFails_shouldContinue() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(existing);
        when(httpClient.deleteTenant(anyString(), any(Long.class))).thenReturn(false);
        doNothing().when(repository).delete(1L);
        
        service.deleteAndNotify(1L);
        
        verify(repository).delete(1L);
    }

    @Test
    void deleteAndNotify_noUrl_shouldStillDelete() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setNodoPerifericoUrlBase(null);
        
        when(repository.find(1L)).thenReturn(existing);
        doNothing().when(repository).delete(1L);
        
        service.deleteAndNotify(1L);
        
        verify(repository).delete(1L);
        verify(httpClient, never()).deleteTenant(anyString(), any(Long.class));
    }

    @Test
    void deleteAndNotify_httpClientThrowsException_shouldContinue() {
        NodoPeriferico existing = new NodoPeriferico();
        existing.setId(1L);
        existing.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(existing);
        when(httpClient.deleteTenant(anyString(), any(Long.class))).thenThrow(new RuntimeException("Network error"));
        doNothing().when(repository).delete(1L);
        
        service.deleteAndNotify(1L);
        
        verify(repository).delete(1L);
    }

    @Test
    void extractEmailFromContacto_withEmail_shouldExtract() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "Contact: admin@clinica.com");
        
        assertEquals("admin@clinica.com", result);
    }

    @Test
    void extractEmailFromContacto_withMultipleEmails_shouldExtractFirst() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "admin@clinica.com or info@clinica.com");
        
        assertEquals("admin@clinica.com", result);
    }

    @Test
    void extractEmailFromContacto_withoutEmail_shouldReturnNull() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "No email here");
        
        assertNull(result);
    }

    @Test
    void extractEmailFromContacto_null_shouldReturnNull() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, (Object) null);
        
        assertNull(result);
    }

    @Test
    void extractEmailFromContacto_empty_shouldReturnNull() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "   ");
        
        assertNull(result);
    }

    @Test
    void buildPayload_withAllFields_shouldBuildComplete() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("buildPayload", NodoPeriferico.class);
        method.setAccessible(true);
        
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setRUT("12345678-9");
        nodo.setNombre("Clínica Test");
        nodo.setDepartamento(uy.edu.tse.hcen.model.Departamento.MONTEVIDEO);
        nodo.setLocalidad("Centro");
        nodo.setDireccion("Calle 123");
        nodo.setNodoPerifericoUrlBase("http://example.com");
        nodo.setNodoPerifericoUsuario("user");
        nodo.setNodoPerifericoPassword("pass");
        nodo.setContacto("contact@example.com");
        nodo.setUrl("http://clinic.com");
        
        NodoPerifericoHttpClient.NodoInitPayload result = 
            (NodoPerifericoHttpClient.NodoInitPayload) method.invoke(service, nodo);
        
        assertNotNull(result);
        assertEquals(1L, result.id);
        assertEquals("12345678-9", result.rut);
        assertEquals("Clínica Test", result.nombre);
        assertEquals("MONTEVIDEO", result.departamento);
        assertEquals("Centro", result.localidad);
        assertEquals("Calle 123", result.direccion);
        assertEquals("http://example.com", result.nodoPerifericoUrlBase);
        assertEquals("user", result.nodoPerifericoUsuario);
        assertEquals("pass", result.nodoPerifericoPassword);
        assertEquals("contact@example.com", result.contacto);
        assertEquals("http://clinic.com", result.url);
    }

    @Test
    void buildPayload_withNullFields_shouldHandleGracefully() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("buildPayload", NodoPeriferico.class);
        method.setAccessible(true);
        
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        // otros campos son null
        
        NodoPerifericoHttpClient.NodoInitPayload result = 
            (NodoPerifericoHttpClient.NodoInitPayload) method.invoke(service, nodo);
        
        assertNotNull(result);
        assertEquals(1L, result.id);
        assertNull(result.departamento);
    }

    @Test
    void createAndNotify_withNullContacto_shouldNotSendEmail() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Clínica Test");
        nodo.setContacto(null);
        
        when(repository.create(any(NodoPeriferico.class))).thenAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        
        NodoPeriferico result = service.createAndNotify(nodo);
        
        assertNotNull(result);
        verify(emailService, never()).sendInvitationEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createAndNotify_withExistingEstado_shouldNotOverride() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Clínica Test");
        nodo.setContacto("admin@clinica.com");
        nodo.setEstado(EstadoNodoPeriferico.ACTIVO);
        
        when(repository.create(any(NodoPeriferico.class))).thenAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        when(emailService.sendInvitationEmail(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(true);
        
        NodoPeriferico result = service.createAndNotify(nodo);
        
        assertNotNull(result);
        assertEquals(EstadoNodoPeriferico.ACTIVO, result.getEstado());
    }

    @Test
    void createAndNotify_blankContacto_shouldNotSendEmail() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Clínica Test");
        nodo.setContacto("   ");
        
        when(repository.create(any(NodoPeriferico.class))).thenAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            if (n.getEstado() == null) {
                n.setEstado(EstadoNodoPeriferico.PENDIENTE);
            }
            return n;
        });
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        
        NodoPeriferico result = service.createAndNotify(nodo);
        
        assertNotNull(result);
        verify(emailService, never()).sendInvitationEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void updateEstado_nullId_shouldNotCrash() {
        when(repository.find(null)).thenReturn(null);
        
        service.updateEstado(null, EstadoNodoPeriferico.ACTIVO);
        
        verify(repository).find(null);
        verify(repository, never()).update(any());
    }

    @Test
    void createAndNotify_withNullContacto_shouldStillCreate() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Test Clinic");
        nodo.setContacto(null);
        
        when(repository.create(any(NodoPeriferico.class))).thenAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        NodoPeriferico result = service.createAndNotify(nodo);
        
        assertNotNull(result);
        assertNotNull(result.getActivationToken());
        assertNotNull(result.getActivationUrl());
        verify(emailService, never()).sendInvitationEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createAndNotify_withContactoWithoutEmail_shouldStillCreate() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Test Clinic");
        nodo.setContacto("No email here");
        
        when(repository.create(any(NodoPeriferico.class))).thenAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        NodoPeriferico result = service.createAndNotify(nodo);
        
        assertNotNull(result);
        verify(emailService, never()).sendInvitationEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createAndNotify_withEmailServiceException_shouldStillReturnNodo() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Test Clinic");
        nodo.setContacto("admin@test.com");
        
        when(repository.create(any(NodoPeriferico.class))).thenAnswer(invocation -> {
            NodoPeriferico n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(repository.update(any(NodoPeriferico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailService.sendInvitationEmail(anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Email service error"));
        
        NodoPeriferico result = service.createAndNotify(nodo);
        
        assertNotNull(result);
        assertNotNull(result.getActivationToken());
    }

    @Test
    void updateAndNotify_withNullUrl_shouldStillUpdate() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Test Clinic");
        nodo.setNodoPerifericoUrlBase(null);
        
        when(repository.update(any(NodoPeriferico.class))).thenReturn(nodo);
        
        NodoPeriferico result = service.updateAndNotify(nodo);
        
        assertNotNull(result);
        verify(httpClient, never()).updateTenant(anyString(), any());
    }

    @Test
    void updateAndNotify_withEmptyUrl_shouldStillUpdate() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Test Clinic");
        nodo.setNodoPerifericoUrlBase("");
        
        when(repository.update(any(NodoPeriferico.class))).thenReturn(nodo);
        
        NodoPeriferico result = service.updateAndNotify(nodo);
        
        assertNotNull(result);
        verify(httpClient, never()).updateTenant(anyString(), any());
    }

    @Test
    void updateAndNotify_withHttpClientException_shouldStillReturnNodo() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Test Clinic");
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.update(any(NodoPeriferico.class))).thenReturn(nodo);
        when(httpClient.updateTenant(anyString(), any())).thenThrow(new RuntimeException("HTTP error"));
        
        NodoPeriferico result = service.updateAndNotify(nodo);
        
        assertNotNull(result);
    }

    @Test
    void deleteAndNotify_withNullUrl_shouldStillDelete() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase(null);
        
        when(repository.find(1L)).thenReturn(nodo);
        doNothing().when(repository).delete(1L);
        
        service.deleteAndNotify(1L);
        
        verify(repository).delete(1L);
        verify(httpClient, never()).deleteTenant(anyString(), anyLong());
    }

    @Test
    void deleteAndNotify_withHttpClientException_shouldStillDelete() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://example.com");
        
        when(repository.find(1L)).thenReturn(nodo);
        when(httpClient.deleteTenant(anyString(), anyLong())).thenThrow(new RuntimeException("HTTP error"));
        doNothing().when(repository).delete(1L);
        
        service.deleteAndNotify(1L);
        
        verify(repository).delete(1L);
    }

    @Test
    void extractEmailFromContacto_withMultipleEmails_shouldReturnFirst() throws Exception {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setContacto("Contact: admin@test.com or support@test.com");
        
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, nodo.getContacto());
        
        assertNotNull(result);
        assertTrue(result.contains("@"));
    }

    @Test
    void extractEmailFromContacto_withEmailInParentheses_shouldExtract() throws Exception {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setContacto("Admin (admin@test.com)");
        
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, nodo.getContacto());
        
        assertEquals("admin@test.com", result);
    }

    @Test
    void extractEmailFromContacto_withEmailAtEnd_shouldExtract() throws Exception {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setContacto("Contact us at admin@test.com");
        
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, nodo.getContacto());
        
        assertEquals("admin@test.com", result);
    }

    @Test
    void buildPayload_withAllFields_shouldMapCorrectly() throws Exception {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setRUT("12345678-9");
        nodo.setNombre("Test Clinic");
        nodo.setDepartamento(uy.edu.tse.hcen.model.Departamento.MONTEVIDEO);
        nodo.setLocalidad("Centro");
        nodo.setDireccion("Calle 123");
        nodo.setNodoPerifericoUrlBase("http://example.com");
        nodo.setNodoPerifericoUsuario("user");
        nodo.setNodoPerifericoPassword("pass");
        nodo.setContacto("contact@example.com");
        nodo.setUrl("http://clinic.com");
        
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("buildPayload", NodoPeriferico.class);
        method.setAccessible(true);
        
        NodoPerifericoHttpClient.NodoInitPayload payload = 
            (NodoPerifericoHttpClient.NodoInitPayload) method.invoke(service, nodo);
        
        assertNotNull(payload);
        assertEquals(1L, payload.id);
        assertEquals("12345678-9", payload.rut);
        assertEquals("Test Clinic", payload.nombre);
        assertEquals("MONTEVIDEO", payload.departamento);
        assertEquals("Centro", payload.localidad);
        assertEquals("Calle 123", payload.direccion);
        assertEquals("http://example.com", payload.nodoPerifericoUrlBase);
        assertEquals("user", payload.nodoPerifericoUsuario);
        assertEquals("pass", payload.nodoPerifericoPassword);
        assertEquals("contact@example.com", payload.contacto);
        assertEquals("http://clinic.com", payload.url);
    }

    @Test
    void buildPayload_withNullDepartamento_shouldSetNull() throws Exception {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setDepartamento(null);
        
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("buildPayload", NodoPeriferico.class);
        method.setAccessible(true);
        
        NodoPerifericoHttpClient.NodoInitPayload payload = 
            (NodoPerifericoHttpClient.NodoInitPayload) method.invoke(service, nodo);
        
        assertNotNull(payload);
        assertNull(payload.departamento);
    }

    @Test
    void buildPayload_withNullFields_shouldHandle() throws Exception {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        // otros campos son null
        
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("buildPayload", NodoPeriferico.class);
        method.setAccessible(true);
        
        NodoPerifericoHttpClient.NodoInitPayload payload = 
            (NodoPerifericoHttpClient.NodoInitPayload) method.invoke(service, nodo);
        
        assertNotNull(payload);
        assertEquals(1L, payload.id);
        assertNull(payload.rut);
        assertNull(payload.nombre);
    }

    @Test
    void extractEmailFromContacto_withInvalidEmail_shouldReturnNull() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "No email here");
        
        assertNull(result);
    }

    @Test
    void extractEmailFromContacto_withEmailWithSubdomain_shouldExtract() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "admin@mail.example.com");
        
        assertEquals("admin@mail.example.com", result);
    }

    @Test
    void extractEmailFromContacto_withEmailWithPlusSign_shouldExtract() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "admin+test@example.com");
        
        assertEquals("admin+test@example.com", result);
    }

    @Test
    void extractEmailFromContacto_withEmailWithUnderscore_shouldExtract() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "admin_test@example.com");
        
        assertEquals("admin_test@example.com", result);
    }

    @Test
    void extractEmailFromContacto_withEmailWithNumbers_shouldExtract() throws Exception {
        java.lang.reflect.Method method = NodoService.class.getDeclaredMethod("extractEmailFromContacto", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "admin123@example.com");
        
        assertEquals("admin123@example.com", result);
    }

    @Test
    void find_validId_shouldReturnNodo() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Test Clinic");
        
        when(repository.find(1L)).thenReturn(nodo);
        
        NodoPeriferico result = service.find(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Clinic", result.getNombre());
        verify(repository).find(1L);
    }

    @Test
    void find_invalidId_shouldReturnNull() {
        when(repository.find(999L)).thenReturn(null);
        
        NodoPeriferico result = service.find(999L);
        
        assertNull(result);
        verify(repository).find(999L);
    }

    @Test
    void findAssociationsByUser_validUserId_shouldReturnList() {
        java.util.List<uy.edu.tse.hcen.model.UserClinicAssociation> associations = 
            java.util.Arrays.asList(
                new uy.edu.tse.hcen.model.UserClinicAssociation(),
                new uy.edu.tse.hcen.model.UserClinicAssociation()
            );
        
        when(associationRepo.findByUser(1L)).thenReturn(associations);
        
        java.util.List<uy.edu.tse.hcen.model.UserClinicAssociation> result = 
            service.findAssociationsByUser(1L);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(associationRepo).findByUser(1L);
    }

    @Test
    void findAssociationsByUser_noAssociations_shouldReturnEmptyList() {
        when(associationRepo.findByUser(1L)).thenReturn(java.util.Collections.emptyList());
        
        java.util.List<uy.edu.tse.hcen.model.UserClinicAssociation> result = 
            service.findAssociationsByUser(1L);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(associationRepo).findByUser(1L);
    }
}


package uy.edu.tse.hcen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.model.EstadoPrestador;
import uy.edu.tse.hcen.model.PrestadorSalud;
import uy.edu.tse.hcen.repository.PrestadorSaludRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para PrestadorSaludService.
 */
class PrestadorSaludServiceTest {

    @Mock
    private PrestadorSaludRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PrestadorSaludService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new PrestadorSaludService();
        
        // Inyectar mocks mediante reflexión
        Field repoField = PrestadorSaludService.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(service, repository);
        
        Field emailField = PrestadorSaludService.class.getDeclaredField("emailService");
        emailField.setAccessible(true);
        emailField.set(service, emailService);
    }

    @Test
    void crearInvitacion_validInput_shouldCreateAndSendEmail() {
        String nombre = "Clínica Test";
        String contacto = "test@example.com";
        
        doAnswer(invocation -> {
            PrestadorSalud prestador = invocation.getArgument(0);
            prestador.setId(1L);
            return null;
        }).when(repository).persist(any(PrestadorSalud.class));
        
        PrestadorSalud result = service.crearInvitacion(nombre, contacto);
        
        assertNotNull(result);
        assertEquals(nombre, result.getNombre());
        assertEquals(contacto, result.getContacto());
        assertEquals(EstadoPrestador.PENDIENTE_REGISTRO, result.getEstado());
        assertNotNull(result.getInvitationToken());
        assertNotNull(result.getInvitationUrl());
        assertNotNull(result.getTokenExpiresAt());
        assertTrue(result.getTokenExpiresAt().isAfter(LocalDateTime.now()));
        
        verify(repository, times(1)).persist(any(PrestadorSalud.class));
        verify(emailService, times(1)).sendPrestadorInvitationEmail(
            eq(contacto), eq(nombre), anyString());
    }

    @Test
    void crearInvitacion_emailServiceFails_shouldContinue() {
        String nombre = "Clínica Test";
        String contacto = "test@example.com";
        
        doAnswer(invocation -> {
            PrestadorSalud prestador = invocation.getArgument(0);
            prestador.setId(1L);
            return null;
        }).when(repository).persist(any(PrestadorSalud.class));
        
        doThrow(new RuntimeException("Email error")).when(emailService)
            .sendPrestadorInvitationEmail(anyString(), anyString(), anyString());
        
        PrestadorSalud result = service.crearInvitacion(nombre, contacto);
        
        assertNotNull(result);
        verify(repository, times(1)).persist(any(PrestadorSalud.class));
    }

    @Test
    void completarRegistro_validToken_shouldCompleteRegistration() {
        String token = "valid-token-123";
        PrestadorSalud prestador = new PrestadorSalud("Test Clinic", "test@example.com");
        prestador.setInvitationToken(token);
        prestador.setTokenExpiresAt(LocalDateTime.now().plusDays(7));
        
        when(repository.findByInvitationToken(token)).thenReturn(prestador);
        when(repository.merge(any(PrestadorSalud.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));
        
        PrestadorSalud result = service.completarRegistro(
            token, "12345678-9", "http://example.com", 
            "MONTEVIDEO", "Ciudad", "Calle 123", "123456789");
        
        assertNotNull(result);
        assertEquals(EstadoPrestador.ACTIVO, result.getEstado());
        assertEquals("12345678-9", result.getRut());
        assertEquals("http://example.com", result.getUrl());
        assertNull(result.getInvitationToken());
        assertNull(result.getInvitationUrl());
        
        verify(repository, times(1)).findByInvitationToken(token);
        verify(repository, times(1)).merge(any(PrestadorSalud.class));
    }

    @Test
    void completarRegistro_invalidToken_shouldThrowException() {
        when(repository.findByInvitationToken("invalid")).thenReturn(null);
        
        assertThrows(IllegalArgumentException.class, () -> 
            service.completarRegistro("invalid", "123", "url", 
                "MONTEVIDEO", "loc", "dir", "tel"));
    }

    @Test
    void completarRegistro_expiredToken_shouldThrowException() {
        String token = "expired-token";
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setInvitationToken(token);
        prestador.setTokenExpiresAt(LocalDateTime.now().minusDays(1));
        
        when(repository.findByInvitationToken(token)).thenReturn(prestador);
        
        assertThrows(IllegalArgumentException.class, () ->
            service.completarRegistro(token, "123", "url",
                "MONTEVIDEO", "loc", "dir", "tel"));
    }

    @Test
    void completarRegistro_invalidDepartamento_shouldHandleGracefully() {
        String token = "valid-token";
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setInvitationToken(token);
        prestador.setTokenExpiresAt(LocalDateTime.now().plusDays(7));
        
        when(repository.findByInvitationToken(token)).thenReturn(prestador);
        when(repository.merge(any(PrestadorSalud.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        
        PrestadorSalud result = service.completarRegistro(
            token, "123", "url", "INVALID_DEPT", "loc", "dir", "tel");
        
        assertNotNull(result);
        assertEquals(EstadoPrestador.ACTIVO, result.getEstado());
    }

    @Test
    void listarTodos_shouldReturnAll() {
        PrestadorSalud p1 = new PrestadorSalud("Clinic 1", "c1@example.com");
        PrestadorSalud p2 = new PrestadorSalud("Clinic 2", "c2@example.com");
        
        when(repository.findAll()).thenReturn(Arrays.asList(p1, p2));
        
        List<PrestadorSalud> result = service.listarTodos();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void obtenerPorId_validId_shouldReturnPrestador() {
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setId(1L);
        
        when(repository.findById(1L)).thenReturn(prestador);
        
        PrestadorSalud result = service.obtenerPorId(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorId_invalidId_shouldReturnNull() {
        when(repository.findById(999L)).thenReturn(null);
        
        PrestadorSalud result = service.obtenerPorId(999L);
        
        assertNull(result);
    }

    @Test
    void actualizar_shouldUpdateAndReturn() {
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setId(1L);
        
        when(repository.merge(prestador)).thenReturn(prestador);
        
        PrestadorSalud result = service.actualizar(prestador);
        
        assertNotNull(result);
        verify(repository, times(1)).merge(prestador);
    }

    @Test
    void crearInvitacion_nullNombre_shouldHandle() {
        String contacto = "test@example.com";
        
        doAnswer(invocation -> {
            PrestadorSalud prestador = invocation.getArgument(0);
            prestador.setId(1L);
            return null;
        }).when(repository).persist(any(PrestadorSalud.class));
        
        PrestadorSalud result = service.crearInvitacion(null, contacto);
        
        assertNotNull(result);
        assertNull(result.getNombre());
        verify(repository).persist(any(PrestadorSalud.class));
    }

    @Test
    void crearInvitacion_nullContacto_shouldHandle() {
        String nombre = "Clínica Test";
        
        doAnswer(invocation -> {
            PrestadorSalud prestador = invocation.getArgument(0);
            prestador.setId(1L);
            return null;
        }).when(repository).persist(any(PrestadorSalud.class));
        
        PrestadorSalud result = service.crearInvitacion(nombre, null);
        
        assertNotNull(result);
        assertNull(result.getContacto());
        verify(repository).persist(any(PrestadorSalud.class));
    }

    @Test
    void completarRegistro_nullRut_shouldComplete() {
        String token = "valid-token";
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setInvitationToken(token);
        prestador.setTokenExpiresAt(LocalDateTime.now().plusDays(7));
        
        when(repository.findByInvitationToken(token)).thenReturn(prestador);
        when(repository.merge(any(PrestadorSalud.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        
        PrestadorSalud result = service.completarRegistro(
            token, null, "http://example.com", 
            "MONTEVIDEO", "loc", "dir", "tel");
        
        assertNotNull(result);
        assertEquals(EstadoPrestador.ACTIVO, result.getEstado());
        assertNull(result.getRut());
    }

    @Test
    void completarRegistro_nullUrl_shouldComplete() {
        String token = "valid-token";
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setInvitationToken(token);
        prestador.setTokenExpiresAt(LocalDateTime.now().plusDays(7));
        
        when(repository.findByInvitationToken(token)).thenReturn(prestador);
        when(repository.merge(any(PrestadorSalud.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        
        PrestadorSalud result = service.completarRegistro(
            token, "12345678-9", null, 
            "MONTEVIDEO", "loc", "dir", "tel");
        
        assertNotNull(result);
        assertNull(result.getUrl());
    }

    @Test
    void completarRegistro_nullDepartamento_shouldComplete() {
        String token = "valid-token";
        PrestadorSalud prestador = new PrestadorSalud("Test", "test@example.com");
        prestador.setInvitationToken(token);
        prestador.setTokenExpiresAt(LocalDateTime.now().plusDays(7));
        
        when(repository.findByInvitationToken(token)).thenReturn(prestador);
        when(repository.merge(any(PrestadorSalud.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        
        PrestadorSalud result = service.completarRegistro(
            token, "12345678-9", "http://example.com", 
            null, "loc", "dir", "tel");
        
        assertNotNull(result);
        assertEquals(EstadoPrestador.ACTIVO, result.getEstado());
    }

    @Test
    void listarTodos_emptyList_shouldReturnEmpty() {
        when(repository.findAll()).thenReturn(new ArrayList<>());
        
        List<PrestadorSalud> result = service.listarTodos();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenerPorId_nullId_shouldReturnNull() {
        when(repository.findById(null)).thenReturn(null);
        
        PrestadorSalud result = service.obtenerPorId(null);
        
        assertNull(result);
    }
}


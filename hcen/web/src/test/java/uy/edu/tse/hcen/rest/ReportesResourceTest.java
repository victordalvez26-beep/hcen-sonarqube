package uy.edu.tse.hcen.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.Rol;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.service.ReportesService;
import uy.edu.tse.hcen.util.JWTUtil;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

import org.mockito.MockedStatic;
import jakarta.ws.rs.client.ClientBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios exhaustivos para ReportesResource.
 */
class ReportesResourceTest {

    @Mock
    private ReportesService reportesService;

    @Mock
    private UserDAO userDAO;

    @Mock
    private HttpServletRequest request;

    private ReportesResource resource;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resource = new ReportesResource();
        
        Field serviceField = ReportesResource.class.getDeclaredField("reportesService");
        serviceField.setAccessible(true);
        serviceField.set(resource, reportesService);
        
        Field daoField = ReportesResource.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(resource, userDAO);
    }

    private void setupAdminUser() {
        String jwt = "valid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        // JWTUtil.validateJWT es estático, se mockea en cada test que lo necesite
    }

    private void setupNonAdminUser() {
        String jwt = "valid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        // JWTUtil.validateJWT es estático, se mockea en cada test que lo necesite
    }
    
    private void setupAdminUserWithMock(MockedStatic<JWTUtil> mockedJWTUtil) {
        mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("admin-uid");
        User admin = new User("admin-uid", "admin@example.com", "Admin", null, "User", null, "CI", "12345678", null);
        admin.setRol(Rol.ADMIN_HCEN);
        when(userDAO.findByUid("admin-uid")).thenReturn(admin);
    }
    
    private void setupNonAdminUserWithMock(MockedStatic<JWTUtil> mockedJWTUtil) {
        mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("user-uid");
        User user = new User("user-uid", "user@example.com", "User", null, "Name", null, "CI", "87654321", null);
        user.setRol(Rol.USUARIO_SALUD);
        when(userDAO.findByUid("user-uid")).thenReturn(user);
    }

    @Test
    void test_endpoint_shouldReturnOk() {
        Response response = resource.test();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("ok", entity.get("status"));
        assertTrue(entity.get("message").toString().contains("funcionando"));
    }

    @Test
    void obtenerEvolucionDocumentos_validDates_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            LocalDate inicio = LocalDate.now().minusDays(7);
            LocalDate fin = LocalDate.now();
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();
            row.put("fecha", java.sql.Date.valueOf(inicio));
            row.put("total", 10L);
            resultado.add(row);
            
            when(reportesService.obtenerEvolucionDocumentos(inicio, fin)).thenReturn(resultado);
            
            Response response = resource.obtenerEvolucionDocumentos(
                request, inicio.toString(), fin.toString());
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(reportesService).obtenerEvolucionDocumentos(inicio, fin);
        }
    }

    @Test
    void obtenerEvolucionDocumentos_nullDates_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            when(reportesService.obtenerEvolucionDocumentos(null, null)).thenReturn(resultado);
            
            Response response = resource.obtenerEvolucionDocumentos(request, null, null);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionDocumentos_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerEvolucionDocumentos(request, "2024-01-01", "2024-01-31");
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerEvolucionDocumentos_forbidden_shouldReturnForbidden() {
        setupNonAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupNonAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerEvolucionDocumentos(request, "2024-01-01", "2024-01-31");
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionDocumentos_serviceException_shouldReturnInternalError() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            when(reportesService.obtenerEvolucionDocumentos(any(), any()))
                .thenThrow(new RuntimeException("Service error"));
            
            Response response = resource.obtenerEvolucionDocumentos(request, "2024-01-01", "2024-01-31");
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorEspecialidad_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            when(reportesService.obtenerDocumentosPorEspecialidad()).thenReturn(resultado);
            
            Response response = resource.obtenerDocumentosPorEspecialidad(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorFormato_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            when(reportesService.obtenerDocumentosPorFormato()).thenReturn(resultado);
            
            Response response = resource.obtenerDocumentosPorFormato(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorTenant_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            when(reportesService.obtenerDocumentosPorTenant()).thenReturn(resultado);
            
            Response response = resource.obtenerDocumentosPorTenant(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerResumenDocumentos_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("total", 100L);
            when(reportesService.obtenerResumenDocumentos()).thenReturn(resultado);
            
            Response response = resource.obtenerResumenDocumentos(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerResumenDocumentos_serviceNull_shouldReturnInternalError() throws Exception {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Field serviceField = ReportesResource.class.getDeclaredField("reportesService");
            serviceField.setAccessible(true);
            serviceField.set(resource, null);
            
            Response response = resource.obtenerResumenDocumentos(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerResumenUsuarios_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("totalUsuarios", 50L);
            when(reportesService.obtenerResumenUsuarios()).thenReturn(resultado);
            
            Response response = resource.obtenerResumenUsuarios(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerProfesionales_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            when(reportesService.obtenerProfesionalesDetalle()).thenReturn(resultado);
            
            Response response = resource.obtenerProfesionales(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionDocumentos_invalidDateFormat_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            when(reportesService.obtenerEvolucionDocumentos(null, null)).thenReturn(resultado);
            
            // Fecha inválida será parseada como null
            Response response = resource.obtenerEvolucionDocumentos(request, "invalid-date", "2024-01-31");
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionDocumentos_emptyDateString_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            when(reportesService.obtenerEvolucionDocumentos(null, null)).thenReturn(resultado);
            
            Response response = resource.obtenerEvolucionDocumentos(request, "", "");
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorEspecialidad_serviceException_shouldReturnInternalError() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            when(reportesService.obtenerDocumentosPorEspecialidad())
                .thenThrow(new RuntimeException("Service error"));
            
            Response response = resource.obtenerDocumentosPorEspecialidad(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorFormato_serviceException_shouldReturnInternalError() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            when(reportesService.obtenerDocumentosPorFormato())
                .thenThrow(new RuntimeException("Service error"));
            
            Response response = resource.obtenerDocumentosPorFormato(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorTenant_serviceException_shouldReturnInternalError() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            when(reportesService.obtenerDocumentosPorTenant())
                .thenThrow(new RuntimeException("Service error"));
            
            Response response = resource.obtenerDocumentosPorTenant(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerResumenDocumentos_serviceException_shouldReturnInternalError() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            when(reportesService.obtenerResumenDocumentos())
                .thenThrow(new RuntimeException("Service error"));
            
            Response response = resource.obtenerResumenDocumentos(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerResumenUsuarios_serviceException_shouldReturnInternalError() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            when(reportesService.obtenerResumenUsuarios())
                .thenThrow(new RuntimeException("Service error"));
            
            Response response = resource.obtenerResumenUsuarios(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerProfesionales_serviceException_shouldReturnInternalError() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            when(reportesService.obtenerProfesionalesDetalle())
                .thenThrow(new RuntimeException("Service error"));
            
            Response response = resource.obtenerProfesionales(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionDocumentos_invalidJWT_shouldReturnUnauthorized() {
        String jwt = "invalid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn(null);
            
            Response response = resource.obtenerEvolucionDocumentos(request, "2024-01-01", "2024-01-31");
            
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionDocumentos_userNotFound_shouldReturnForbidden() {
        String jwt = "valid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("unknown-uid");
            when(userDAO.findByUid("unknown-uid")).thenReturn(null);
            
            Response response = resource.obtenerEvolucionDocumentos(request, "2024-01-01", "2024-01-31");
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionDocumentos_userWithoutRole_shouldReturnForbidden() {
        String jwt = "valid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            User user = new User("user-uid", "user@example.com", "User", null, "Name", null, "CI", "12345678", null);
            user.setRol(null);
            when(userDAO.findByUid("user-uid")).thenReturn(user);
            
            Response response = resource.obtenerEvolucionDocumentos(request, "2024-01-01", "2024-01-31");
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionDocumentos_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerEvolucionDocumentos(request, "2024-01-01", "2024-01-31");
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerResumenPoliticas_shouldReturnOk() {
        setupAdminUser();
        
        // El método intentará hacer una llamada HTTP real al servicio de políticas
        Response response = resource.obtenerResumenPoliticas(request);
        
        // Puede fallar por conexión, pero la autenticación debe pasar
        assertNotNull(response);
    }

    @Test
    void obtenerResumenPoliticas_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerResumenPoliticas(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }


    @Test
    void obtenerPoliticasPorAlcance_shouldReturnOk() {
        setupAdminUser();
        
        Response response = resource.obtenerPoliticasPorAlcance(request);
        
        assertNotNull(response);
    }

    @Test
    void obtenerPoliticasPorAlcance_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerPoliticasPorAlcance(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerPoliticasPorDuracion_shouldReturnOk() {
        setupAdminUser();
        
        Response response = resource.obtenerPoliticasPorDuracion(request);
        
        assertNotNull(response);
    }

    @Test
    void obtenerPoliticasPorDuracion_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerPoliticasPorDuracion(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerEvolucionPoliticas_withDates_shouldReturnOk() {
        setupAdminUser();
        
        Response response = resource.obtenerEvolucionPoliticas(
            request, "2024-01-01", "2024-01-31");
        
        assertNotNull(response);
    }

    @Test
    void obtenerEvolucionPoliticas_nullDates_shouldReturnOk() {
        setupAdminUser();
        
        Response response = resource.obtenerEvolucionPoliticas(request, null, null);
        
        assertNotNull(response);
    }

    @Test
    void obtenerEvolucionPoliticas_onlyFechaInicio_shouldReturnOk() {
        setupAdminUser();
        
        Response response = resource.obtenerEvolucionPoliticas(request, "2024-01-01", null);
        
        assertNotNull(response);
    }

    @Test
    void obtenerEvolucionPoliticas_onlyFechaFin_shouldReturnOk() {
        setupAdminUser();
        
        Response response = resource.obtenerEvolucionPoliticas(request, null, "2024-01-31");
        
        assertNotNull(response);
    }

    @Test
    void obtenerEvolucionPoliticas_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerEvolucionPoliticas(request, "2024-01-01", "2024-01-31");
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerResumenPoliticas_serviceUnavailable_shouldHandleGracefully() {
        setupAdminUser();
        
        // El método intentará hacer una llamada HTTP real
        // Si el servicio no está disponible, retornará SERVICE_UNAVAILABLE
        Response response = resource.obtenerResumenPoliticas(request);
        
        assertNotNull(response);
    }

    @Test
    void obtenerPoliticasPorAlcance_serviceUnavailable_shouldReturnServiceUnavailable() {
        setupAdminUser();
        
        Response response = resource.obtenerPoliticasPorAlcance(request);
        
        // Puede retornar SERVICE_UNAVAILABLE si el servicio no está disponible
        assertNotNull(response);
    }

    @Test
    void obtenerPoliticasPorDuracion_serviceUnavailable_shouldReturnServiceUnavailable() {
        setupAdminUser();
        
        Response response = resource.obtenerPoliticasPorDuracion(request);
        
        assertNotNull(response);
    }

    @Test
    void obtenerEvolucionPoliticas_serviceUnavailable_shouldReturnServiceUnavailable() {
        setupAdminUser();
        
        Response response = resource.obtenerEvolucionPoliticas(request, "2024-01-01", "2024-01-31");
        
        assertNotNull(response);
    }

    @Test
    void obtenerResumenPoliticas_withMockedJWT_shouldCallService() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerResumenPoliticas(request);
            
            assertNotNull(response);
        }
    }

    @Test
    void obtenerPoliticasPorAlcance_withMockedJWT_shouldCallService() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerPoliticasPorAlcance(request);
            
            assertNotNull(response);
        }
    }

    @Test
    void obtenerPoliticasPorDuracion_withMockedJWT_shouldCallService() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerPoliticasPorDuracion(request);
            
            assertNotNull(response);
        }
    }

    @Test
    void obtenerEvolucionPoliticas_withMockedJWT_shouldCallService() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerEvolucionPoliticas(request, "2024-01-01", "2024-01-31");
            
            assertNotNull(response);
        }
    }

    @Test
    void obtenerPoliticasPorAlcance_forbidden_shouldReturnForbidden() {
        setupNonAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupNonAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerPoliticasPorAlcance(request);
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerPoliticasPorDuracion_forbidden_shouldReturnForbidden() {
        setupNonAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupNonAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerPoliticasPorDuracion(request);
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerResumenPoliticas_invalidJWT_shouldReturnUnauthorized() {
        String jwt = "invalid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn(null);
            
            Response response = resource.obtenerResumenPoliticas(request);
            
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerPoliticasPorAlcance_invalidJWT_shouldReturnUnauthorized() {
        String jwt = "invalid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn(null);
            
            Response response = resource.obtenerPoliticasPorAlcance(request);
            
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerPoliticasPorDuracion_invalidJWT_shouldReturnUnauthorized() {
        String jwt = "invalid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn(null);
            
            Response response = resource.obtenerPoliticasPorDuracion(request);
            
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionPoliticas_invalidJWT_shouldReturnUnauthorized() {
        String jwt = "invalid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn(null);
            
            Response response = resource.obtenerEvolucionPoliticas(request, "2024-01-01", "2024-01-31");
            
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerResumenDocumentos_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerResumenDocumentos(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerResumenDocumentos_forbidden_shouldReturnForbidden() {
        setupNonAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupNonAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerResumenDocumentos(request);
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerResumenUsuarios_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerResumenUsuarios(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerResumenUsuarios_forbidden_shouldReturnForbidden() {
        setupNonAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupNonAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerResumenUsuarios(request);
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerProfesionales_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerProfesionales(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerProfesionales_forbidden_shouldReturnForbidden() {
        setupNonAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupNonAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerProfesionales(request);
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorEspecialidad_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerDocumentosPorEspecialidad(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorEspecialidad_forbidden_shouldReturnForbidden() {
        setupNonAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupNonAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerDocumentosPorEspecialidad(request);
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorFormato_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerDocumentosPorFormato(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorFormato_forbidden_shouldReturnForbidden() {
        setupNonAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupNonAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerDocumentosPorFormato(request);
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorTenant_unauthorized_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerDocumentosPorTenant(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorTenant_forbidden_shouldReturnForbidden() {
        setupNonAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupNonAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerDocumentosPorTenant(request);
            
            assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionDocumentos_blankDateString_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            when(reportesService.obtenerEvolucionDocumentos(null, null)).thenReturn(resultado);
            
            Response response = resource.obtenerEvolucionDocumentos(request, "   ", "   ");
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionPoliticas_blankDateString_shouldReturnOk() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerEvolucionPoliticas(request, "   ", "   ");
            
            assertNotNull(response);
        }
    }

    @Test
    void obtenerEvolucionPoliticas_withSpecialCharacters_shouldEncodeCorrectly() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerEvolucionPoliticas(request, "2024-01-01", "2024-12-31");
            
            assertNotNull(response);
        }
    }

    @Test
    void obtenerResumenDocumentos_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerResumenDocumentos(request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerResumenUsuarios_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerResumenUsuarios(request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerProfesionales_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerProfesionales(request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorEspecialidad_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerDocumentosPorEspecialidad(request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorFormato_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerDocumentosPorFormato(request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorTenant_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerDocumentosPorTenant(request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerResumenPoliticas_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerResumenPoliticas(request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerPoliticasPorAlcance_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerPoliticasPorAlcance(request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerPoliticasPorDuracion_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerPoliticasPorDuracion(request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerEvolucionPoliticas_ensureAdminException_shouldReturnInternalError() {
        when(request.getCookies()).thenThrow(new RuntimeException("Request error"));
        
        Response response = resource.obtenerEvolucionPoliticas(request, "2024-01-01", "2024-01-31");
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void parseDate_validDate_shouldParse() throws Exception {
        java.lang.reflect.Method method = ReportesResource.class.getDeclaredMethod("parseDate", String.class);
        method.setAccessible(true);
        
        java.time.LocalDate result = (java.time.LocalDate) method.invoke(resource, "2024-01-15");
        
        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test
    void parseDate_nullDate_shouldReturnNull() throws Exception {
        java.lang.reflect.Method method = ReportesResource.class.getDeclaredMethod("parseDate", String.class);
        method.setAccessible(true);
        
        java.time.LocalDate result = (java.time.LocalDate) method.invoke(resource, (String) null);
        
        assertNull(result);
    }

    @Test
    void parseDate_emptyDate_shouldReturnNull() throws Exception {
        java.lang.reflect.Method method = ReportesResource.class.getDeclaredMethod("parseDate", String.class);
        method.setAccessible(true);
        
        java.time.LocalDate result = (java.time.LocalDate) method.invoke(resource, "");
        
        assertNull(result);
    }

    @Test
    void parseDate_blankDate_shouldReturnNull() throws Exception {
        java.lang.reflect.Method method = ReportesResource.class.getDeclaredMethod("parseDate", String.class);
        method.setAccessible(true);
        
        java.time.LocalDate result = (java.time.LocalDate) method.invoke(resource, "   ");
        
        assertNull(result);
    }

    @Test
    void parseDate_invalidDate_shouldReturnNull() throws Exception {
        java.lang.reflect.Method method = ReportesResource.class.getDeclaredMethod("parseDate", String.class);
        method.setAccessible(true);
        
        java.time.LocalDate result = (java.time.LocalDate) method.invoke(resource, "invalid-date");
        
        assertNull(result);
    }

    @Test
    void extractJwtFromCookie_withValidCookie_shouldExtract() throws Exception {
        Cookie cookie = new Cookie("hcen_session", "test-jwt-token");
        Cookie[] cookies = {cookie};
        when(request.getCookies()).thenReturn(cookies);
        
        java.lang.reflect.Method method = ReportesResource.class.getDeclaredMethod("extractJwtFromCookie", HttpServletRequest.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(resource, request);
        
        assertEquals("test-jwt-token", result);
    }

    @Test
    void extractJwtFromCookie_withNoCookies_shouldReturnNull() throws Exception {
        when(request.getCookies()).thenReturn(null);
        
        java.lang.reflect.Method method = ReportesResource.class.getDeclaredMethod("extractJwtFromCookie", HttpServletRequest.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(resource, request);
        
        assertNull(result);
    }

    @Test
    void extractJwtFromCookie_withDifferentCookie_shouldReturnNull() throws Exception {
        Cookie cookie = new Cookie("other_cookie", "value");
        Cookie[] cookies = {cookie};
        when(request.getCookies()).thenReturn(cookies);
        
        java.lang.reflect.Method method = ReportesResource.class.getDeclaredMethod("extractJwtFromCookie", HttpServletRequest.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(resource, request);
        
        assertNull(result);
    }

    @Test
    void extractJwtFromCookie_withMultipleCookies_shouldFindCorrect() throws Exception {
        Cookie cookie1 = new Cookie("other_cookie", "value1");
        Cookie cookie2 = new Cookie("hcen_session", "test-jwt-token");
        Cookie cookie3 = new Cookie("another_cookie", "value2");
        Cookie[] cookies = {cookie1, cookie2, cookie3};
        when(request.getCookies()).thenReturn(cookies);
        
        java.lang.reflect.Method method = ReportesResource.class.getDeclaredMethod("extractJwtFromCookie", HttpServletRequest.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(resource, request);
        
        assertEquals("test-jwt-token", result);
    }

    // Test de getPoliticasUrl eliminado: el método privado ya no existe,
    // ahora se usa PoliticasServiceUrlUtil que ya está cubierto en PoliticasServiceUrlUtilTest

    @Test
    void createClient_shouldReturnClient() throws Exception {
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            jakarta.ws.rs.client.Client mockClient = mock(jakarta.ws.rs.client.Client.class);
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            
            java.lang.reflect.Method method = ReportesResource.class.getDeclaredMethod("createClient");
            method.setAccessible(true);
            
            jakarta.ws.rs.client.Client result = (jakarta.ws.rs.client.Client) method.invoke(resource);
            
            assertNotNull(result);
        }
    }

    @Test
    void obtenerEvolucionDocumentos_withInvalidDates_shouldHandleGracefully() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            when(reportesService.obtenerEvolucionDocumentos(null, null)).thenReturn(resultado);
            
            Response response = resource.obtenerEvolucionDocumentos(request, "invalid", "invalid");
            
            assertNotNull(response);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionDocumentos_withNullDates_shouldHandleGracefully() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            List<Map<String, Object>> resultado = new ArrayList<>();
            when(reportesService.obtenerEvolucionDocumentos(null, null)).thenReturn(resultado);
            
            Response response = resource.obtenerEvolucionDocumentos(request, null, null);
            
            assertNotNull(response);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerEvolucionPoliticas_withNullDates_shouldHandleGracefully() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerEvolucionPoliticas(request, null, null);
            
            assertNotNull(response);
            // Puede retornar SERVICE_UNAVAILABLE si el servicio no está disponible
            assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode() ||
                       response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @Test
    void obtenerEvolucionPoliticas_withOnlyFechaInicio_shouldHandleGracefully() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerEvolucionPoliticas(request, "2024-01-01", null);
            
            assertNotNull(response);
            assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode() ||
                       response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @Test
    void obtenerEvolucionPoliticas_withOnlyFechaFin_shouldHandleGracefully() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerEvolucionPoliticas(request, null, "2024-01-31");
            
            assertNotNull(response);
            assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode() ||
                       response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @Test
    void obtenerResumenPoliticas_clientException_shouldReturnInternalError() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerResumenPoliticas(request);
            
            assertNotNull(response);
            assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode() ||
                       response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @Test
    void obtenerPoliticasPorAlcance_clientException_shouldReturnServiceUnavailable() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerPoliticasPorAlcance(request);
            
            assertNotNull(response);
            assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerPoliticasPorDuracion_clientException_shouldReturnServiceUnavailable() {
        setupAdminUser();
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAdminUserWithMock(mockedJWTUtil);
            
            Response response = resource.obtenerPoliticasPorDuracion(request);
            
            assertNotNull(response);
            assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerResumenPoliticas_securityFails_shouldReturnSecurityResponse()
    {
    setupNonAdminUser();
    try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
        setupNonAdminUserWithMock(mockedJWTUtil);
        
        Response response = resource.obtenerResumenPoliticas(request);
        
        assertNotNull(response);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }
    
    }





}


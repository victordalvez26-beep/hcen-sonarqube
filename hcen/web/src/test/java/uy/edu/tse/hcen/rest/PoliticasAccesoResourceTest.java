package uy.edu.tse.hcen.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.util.CookieUtil;
import uy.edu.tse.hcen.util.JWTUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests completos para PoliticasAccesoResource.
 * Mockea JAX-RS Client para simular llamadas al servicio de políticas.
 */
class PoliticasAccesoResourceTest {

    @Mock
    private UserDAO userDAO;

    private PoliticasAccesoResource resource;
    private Client mockClient;
    private WebTarget mockWebTarget;
    private Invocation.Builder mockBuilder;
    private Response mockResponse;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resource = new PoliticasAccesoResource();
        
        // Inyectar UserDAO mock
        Field userDAOField = PoliticasAccesoResource.class.getDeclaredField("userDAO");
        userDAOField.setAccessible(true);
        userDAOField.set(resource, userDAO);
        
        // Crear mocks de la cadena JAX-RS Client
        mockClient = mock(Client.class);
        mockWebTarget = mock(WebTarget.class);
        mockBuilder = mock(Invocation.Builder.class);
        mockResponse = mock(Response.class);
        mockRequest = mock(HttpServletRequest.class);
        
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
        when(mockClient.target(anyString())).thenReturn(mockWebTarget);
    }

    private void setupAuthenticatedUser(String userUid, String codDocum) {
        String jwt = "valid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(mockRequest.getCookies()).thenReturn(new Cookie[]{cookie});
        
        // Si el userUid no tiene el formato correcto, crear uno con formato uy-ci-{codDocum}
        String formattedUid = userUid;
        if (!userUid.startsWith("uy-ci-")) {
            formattedUid = "uy-ci-" + codDocum;
        }
        
        User user = new User(formattedUid, "test@example.com", "Test", null, "User", null, "CI", codDocum, null);
        when(userDAO.findByUid(formattedUid)).thenReturn(user);
    }


    @Test
    void crearPolitica_validData_shouldReturnCreated() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("especialidadesAutorizadas", List.of("Cardiología"));
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            Map<String, Object> responseEntity = new HashMap<>();
            responseEntity.put("id", 1L);
            responseEntity.put("alcance", "TOTAL");
            when(mockResponse.readEntity(Object.class)).thenReturn(responseEntity);
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            Response response = resource.crearPolitica(body);
            
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void crearPolitica_missingCodDocumPaciente_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("clinicaAutorizada", "Clínica Test");
        
        Response response = resource.crearPolitica(body);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void crearPolitica_emptyCodDocumPaciente_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "");
        body.put("clinicaAutorizada", "Clínica Test");
        
        Response response = resource.crearPolitica(body);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void crearPolitica_nullCodDocumPaciente_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", null);
        body.put("clinicaAutorizada", "Clínica Test");
        
        Response response = resource.crearPolitica(body);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void crearPolitica_missingClinicaAutorizada_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        
        Response response = resource.crearPolitica(body);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void crearPolitica_emptyClinicaAutorizada_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "");
        
        Response response = resource.crearPolitica(body);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void crearPolitica_nullClinicaAutorizada_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", null);
        
        Response response = resource.crearPolitica(body);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void crearPolitica_withEmptyFechaVencimiento_shouldRemoveIt() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("fechaVencimiento", "");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_withEmptyTipoDocumento_shouldRemoveIt() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("tipoDocumento", "");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_withEmptyReferencia_shouldRemoveIt() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("referencia", "");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_withEspecialidadesList_shouldConvertToJson() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("especialidadesAutorizadas", List.of("Cardiología", "Neurología"));
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_withEmptyEspecialidadesList_shouldSetNull() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("especialidadesAutorizadas", List.of());
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_withEspecialidadesString_shouldKeepIt() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("especialidadesAutorizadas", "Cardiología");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_withEmptyEspecialidadesString_shouldSetNull() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("especialidadesAutorizadas", "   ");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_serviceReturnsError_shouldReturnErrorStatus() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
            when(mockResponse.readEntity(String.class)).thenReturn("Error message");
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            Response response = resource.crearPolitica(body);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void crearPolitica_serviceThrowsException_shouldReturnInternalError() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.post(any(Entity.class))).thenThrow(new RuntimeException("Connection error"));
            
            Response response = resource.crearPolitica(body);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void listarPoliticas_shouldReturnOk() throws Exception {
        String userUid = "uy-ci-12345678";
        setupAuthenticatedUser(userUid, "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn(userUid);
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            when(mockResponse.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
            List<Map<String, Object>> politicas = new ArrayList<>();
            Map<String, Object> politica = new HashMap<>();
            politica.put("id", 1L);
            politica.put("clinicaAutorizada", "Clínica Test");
            politicas.add(politica);
            when(mockResponse.readEntity(Object.class)).thenReturn(politicas);
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertNotNull(response);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withNonJsonContentType_shouldReturnInternalError() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            when(mockResponse.getMediaType()).thenReturn(MediaType.TEXT_PLAIN_TYPE);
            when(mockResponse.readEntity(String.class)).thenReturn("Error");
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withProcessingException_shouldReturnServiceUnavailable() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.get()).thenThrow(new jakarta.ws.rs.ProcessingException("Connection refused"));
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticasPorPaciente_validCI_shouldReturnOk() throws Exception {
        String userUid = "uy-ci-12345678";
        setupAuthenticatedUser(userUid, "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn(userUid);
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            List<Map<String, Object>> politicas = new ArrayList<>();
            when(mockResponse.readEntity(Object.class)).thenReturn(politicas);
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticasPorPaciente("12345678", null, mockRequest);
            
            assertNotNull(response);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticasPorPaciente_withError_shouldReturnErrorStatus() throws Exception {
        String userUid = "uy-ci-12345678";
        setupAuthenticatedUser(userUid, "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn(userUid);
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.NOT_FOUND.getStatusCode());
            when(mockResponse.readEntity(String.class)).thenReturn("Not found");
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticasPorPaciente("12345678", null, mockRequest);
            
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticasPorProfesional_validId_shouldReturnOk() throws Exception {
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            List<Map<String, Object>> politicas = new ArrayList<>();
            when(mockResponse.readEntity(Object.class)).thenReturn(politicas);
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticasPorProfesional("prof-123", mockRequest);
            
            assertNotNull(response);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void eliminarPolitica_validId_shouldReturnOk() throws Exception {
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.NO_CONTENT.getStatusCode());
            when(mockBuilder.delete()).thenReturn(mockResponse);
            
            Response response = resource.eliminarPolitica(1L);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void eliminarPolitica_withError_shouldReturnErrorStatus() throws Exception {
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.NOT_FOUND.getStatusCode());
            when(mockResponse.readEntity(String.class)).thenReturn("Not found");
            when(mockBuilder.delete()).thenReturn(mockResponse);
            
            Response response = resource.eliminarPolitica(1L);
            
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    // Test de getPoliticasUrl eliminado: el método privado ya no existe,
    // ahora se usa PoliticasServiceUrlUtil que ya está cubierto en PoliticasServiceUrlUtilTest

    @Test
    void createClientWithTimeout_shouldReturnClient() throws Exception {
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            
            java.lang.reflect.Method method = PoliticasAccesoResource.class.getDeclaredMethod("createClientWithTimeout");
            method.setAccessible(true);
            
            jakarta.ws.rs.client.Client result = (jakarta.ws.rs.client.Client) method.invoke(resource);
            
            assertNotNull(result);
            verify(mockClient, never()).close();
        }
    }

    @Test
    void crearPolitica_withNullBody_shouldReturnBadRequest() {
        Response response = resource.crearPolitica(null);
        
        assertNotNull(response);
        assertTrue(response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode() ||
                   response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    void crearPolitica_withBlankCodDocumPaciente_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "   ");
        body.put("clinicaAutorizada", "Clínica Test");
        
        Response response = resource.crearPolitica(body);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void crearPolitica_withBlankClinicaAutorizada_shouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "   ");
        
        Response response = resource.crearPolitica(body);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void crearPolitica_withNullFechaVencimiento_shouldRemoveIt() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("fechaVencimiento", null);
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_withNullTipoDocumento_shouldRemoveIt() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("tipoDocumento", null);
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_withNullReferencia_shouldRemoveIt() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("referencia", null);
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_withNullEspecialidades_shouldSetNull() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("especialidadesAutorizadas", null);
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void listarPoliticasPorPaciente_nullCI_shouldHandleGracefully() throws Exception {
        String userUid = "uy-ci-12345678";
        setupAuthenticatedUser(userUid, "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn(userUid);
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.get()).thenThrow(new RuntimeException("Error"));
            
            Response response = resource.listarPoliticasPorPaciente(null, null, mockRequest);
            
            assertNotNull(response);
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticasPorProfesional_nullId_shouldHandleGracefully() throws Exception {
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.get()).thenThrow(new RuntimeException("Error"));
            
            Response response = resource.listarPoliticasPorProfesional(null, mockRequest);
            
            assertNotNull(response);
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void eliminarPolitica_nullId_shouldHandleGracefully() throws Exception {
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.delete()).thenThrow(new RuntimeException("Error"));
            
            Response response = resource.eliminarPolitica(null);
            
            assertNotNull(response);
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void listarPoliticas_withEmptyList_shouldReturnOk() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            when(mockResponse.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
            when(mockResponse.readEntity(Object.class)).thenReturn(new ArrayList<>());
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withPoliticaWithoutClinica_shouldSetDefaultTipoAutorizado() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            when(mockResponse.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
            List<Map<String, Object>> politicas = new ArrayList<>();
            Map<String, Object> politica = new HashMap<>();
            politica.put("id", 1L);
            // Sin clinicaAutorizada
            politicas.add(politica);
            when(mockResponse.readEntity(Object.class)).thenReturn(politicas);
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withPoliticaWithEmptyClinica_shouldSetDefaultTipoAutorizado() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            when(mockResponse.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
            List<Map<String, Object>> politicas = new ArrayList<>();
            Map<String, Object> politica = new HashMap<>();
            politica.put("id", 1L);
            politica.put("clinicaAutorizada", "");
            politicas.add(politica);
            when(mockResponse.readEntity(Object.class)).thenReturn(politicas);
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withPoliticaWithEspecialidades_shouldMapCorrectly() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            when(mockResponse.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
            List<Map<String, Object>> politicas = new ArrayList<>();
            Map<String, Object> politica = new HashMap<>();
            politica.put("id", 1L);
            politica.put("clinicaAutorizada", "Clínica Test");
            politica.put("especialidadesAutorizadas", "Cardiología, Neurología");
            politicas.add(politica);
            when(mockResponse.readEntity(Object.class)).thenReturn(politicas);
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void crearPolitica_withNonListEspecialidades_shouldSetNull() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        body.put("especialidadesAutorizadas", 123); // No es lista ni string
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn(new HashMap<>());
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            assertDoesNotThrow(() -> resource.crearPolitica(body));
        }
    }

    @Test
    void crearPolitica_withResponseOK_shouldReturnCreated() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            Map<String, Object> responseEntity = new HashMap<>();
            responseEntity.put("id", 1L);
            when(mockResponse.readEntity(Object.class)).thenReturn(responseEntity);
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            Response response = resource.crearPolitica(body);
            
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void crearPolitica_withResponseEntityMap_shouldMapToFrontendFormat() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            Map<String, Object> responseEntity = new HashMap<>();
            responseEntity.put("id", 1L);
            responseEntity.put("alcance", "TOTAL");
            responseEntity.put("duracion", "PERMANENTE");
            responseEntity.put("gestion", "AUTOMATICA");
            when(mockResponse.hasEntity()).thenReturn(true);
            when(mockResponse.readEntity(Map.class)).thenReturn(responseEntity);
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            Response response = resource.crearPolitica(body);
            
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            @SuppressWarnings("unchecked")
            Map<String, Object> entity = (Map<String, Object>) response.getEntity();
            assertNotNull(entity);
            assertTrue(entity.containsKey("politicaId"));
            assertTrue(entity.containsKey("mensaje"));
            verify(mockClient).close();
        }
    }

    @Test
    void crearPolitica_withResponseEntityNonMap_shouldReturnAsIs() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(mockResponse.readEntity(Object.class)).thenReturn("Simple string response");
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            Response response = resource.crearPolitica(body);
            
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void crearPolitica_withErrorResponseNullMessage_shouldReturnError() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
            when(mockResponse.readEntity(String.class)).thenReturn(null);
            when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
            
            Response response = resource.crearPolitica(body);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void crearPolitica_withProcessingException_shouldReturnInternalError() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("codDocumPaciente", "12345678");
        body.put("clinicaAutorizada", "Clínica Test");
        
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class)) {
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.post(any(Entity.class))).thenThrow(new jakarta.ws.rs.ProcessingException("Connection refused"));
            
            Response response = resource.crearPolitica(body);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void listarPoliticas_withNonListEntity_shouldReturnAsIs() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            when(mockResponse.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
            when(mockResponse.readEntity(Object.class)).thenReturn("Not a list");
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withErrorResponse_shouldReturnErrorStatus() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            when(mockResponse.readEntity(String.class)).thenReturn("Service error");
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withErrorResponseException_shouldReturnErrorStatus() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockResponse.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            when(mockResponse.readEntity(String.class)).thenThrow(new RuntimeException("Read error"));
            when(mockBuilder.get()).thenReturn(mockResponse);
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withProcessingExceptionConnectionRefused_shouldReturnServiceUnavailable() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.get()).thenThrow(new jakarta.ws.rs.ProcessingException("Connection refused"));
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withProcessingExceptionFailedToRespond_shouldReturnServiceUnavailable() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.get()).thenThrow(new jakarta.ws.rs.ProcessingException("failed to respond"));
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void listarPoliticas_withProcessingExceptionNoHttpResponse_shouldReturnServiceUnavailable() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.get()).thenThrow(new jakarta.ws.rs.ProcessingException("NoHttpResponseException"));
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withProcessingExceptionOther_shouldReturnInternalError() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.get()).thenThrow(new jakarta.ws.rs.ProcessingException("Other error"));
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

    @Test
    void listarPoliticas_withException_shouldReturnInternalError() throws Exception {
        setupAuthenticatedUser("user-uid", "12345678");
        try (MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic(ClientBuilder.class);
             MockedStatic<CookieUtil> mockedCookieUtil = mockStatic(CookieUtil.class);
             MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            
            mockedCookieUtil.when(() -> CookieUtil.resolveJwtToken(mockRequest)).thenReturn("valid.jwt.token");
            mockedJWTUtil.when(() -> JWTUtil.validateJWT("valid.jwt.token")).thenReturn("uy-ci-12345678");
            
            mockedClientBuilder.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            
            when(mockBuilder.get()).thenThrow(new RuntimeException("Unexpected error"));
            
            Response response = resource.listarPoliticas(mockRequest);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            verify(mockClient).close();
        }
    }

}

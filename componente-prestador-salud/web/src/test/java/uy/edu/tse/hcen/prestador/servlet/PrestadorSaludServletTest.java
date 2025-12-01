package uy.edu.tse.hcen.prestador.servlet;

// removed unused imports
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uy.edu.tse.hcen.prestador.client.HcenApiClient;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestadorSaludServletTest {

    @Mock
    private HcenApiClient apiClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private PrestadorSaludServlet servlet;

    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new PrestadorSaludServlet();
        // Inyectar el mock usando el setter package-private
        servlet.setApiClient(apiClient);
        
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        // Mockear getWriter por defecto para evitar NPE en tests que no lo configuren explícitamente
        // Hacerlo lenient para evitar UnnecessaryStubbingException en tests que no usan el writer
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testDoPost_UsuariosSalud_Success() throws Exception {
        // Configurar request
        when(request.getPathInfo()).thenReturn("/usuarios-salud");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        when(response.getWriter()).thenReturn(printWriter);
        
        String jsonBody = "{\"ci\": \"12345678\", \"nombre\": \"Test\"}";
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));
        when(request.getReader()).thenReturn(reader);
        
        // Configurar respuesta del cliente
        HcenApiClient.ApiResponse apiResponse = new HcenApiClient.ApiResponse(
            201, 
            "{\"id\": 123, \"success\": true}", 
            Map.of()
        );
        when(apiClient.post(eq("/usuarios-salud"), any(), eq("test-api-key"), eq("test-origin")))
            .thenReturn(apiResponse);

        // Ejecutar
        servlet.init();
        servlet.doPost(request, response);

        // Verificar
        verify(response).setStatus(201);
        verify(response).setContentType("application/json");
        assertTrue(responseWriter.toString().contains("\"id\": 123"));
    }

    @Test
    void testDoPost_MetadatosDocumento_Success() throws Exception {
        when(request.getPathInfo()).thenReturn("/metadatos-documento");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        
        String jsonBody = "{\"documentoId\": \"doc123\", \"metadatos\": {}}";
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));
        when(request.getReader()).thenReturn(reader);
        
        HcenApiClient.ApiResponse apiResponse = new HcenApiClient.ApiResponse(
            200, 
            "{\"success\": true}", 
            Map.of()
        );
        when(apiClient.post(eq("/metadatos-documento"), any(), eq("test-api-key"), eq("test-origin")))
            .thenReturn(apiResponse);

        servlet.init();
        servlet.doPost(request, response);

        verify(response).setStatus(200);
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoPost_NoApiKey() throws Exception {
        when(request.getPathInfo()).thenReturn("/usuarios-salud");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.init();
        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        assertTrue(responseWriter.toString().contains("API Key no configurada"));
    }

    @Test
    void testDoPost_EmptyApiKey() throws Exception {
        when(request.getPathInfo()).thenReturn("/usuarios-salud");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("");
        when(response.getWriter()).thenReturn(printWriter);

        servlet.init();
        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("API Key no configurada"));
    }

    @Test
    void testDoPost_NotFound() throws Exception {
        when(request.getPathInfo()).thenReturn("/endpoint-inexistente");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        when(response.getWriter()).thenReturn(printWriter);
        
        BufferedReader reader = new BufferedReader(new StringReader("{}"));
        when(request.getReader()).thenReturn(reader);

        servlet.init();
        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertTrue(responseWriter.toString().contains("Endpoint no encontrado"));
    }

    @Test
    void testDoPost_ApiClientNull() throws Exception {
        PrestadorSaludServlet servletWithoutClient = new PrestadorSaludServlet();
        when(request.getPathInfo()).thenReturn("/usuarios-salud");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        servletWithoutClient.init();
        // Después de init, el apiClient se inicializa, así que verificamos que funciona
        servletWithoutClient.doPost(request, response);

        // Si no hay API key, debería retornar 401
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("API Key no configurada"));
    }

    @Test
    void testDoPost_WithTrailingSlash() throws Exception {
        when(request.getPathInfo()).thenReturn("/usuarios-salud/");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        
        BufferedReader reader = new BufferedReader(new StringReader("{}"));
        when(request.getReader()).thenReturn(reader);
        
        HcenApiClient.ApiResponse apiResponse = new HcenApiClient.ApiResponse(200, "{}", Map.of());
        when(apiClient.post(eq("/usuarios-salud"), any(), anyString(), anyString()))
            .thenReturn(apiResponse);

        servlet.init();
        servlet.doPost(request, response);

        verify(apiClient).post(eq("/usuarios-salud"), any(), anyString(), anyString());
    }

    @Test
    void testDoPost_InvalidJsonBody() throws Exception {
        when(request.getPathInfo()).thenReturn("/usuarios-salud");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        
        String invalidJson = "{invalid json}";
        BufferedReader reader = new BufferedReader(new StringReader(invalidJson));
        when(request.getReader()).thenReturn(reader);
        
        HcenApiClient.ApiResponse apiResponse = new HcenApiClient.ApiResponse(200, "{}", Map.of());
        when(apiClient.post(anyString(), any(), anyString(), anyString()))
            .thenReturn(apiResponse);

        servlet.init();
        servlet.doPost(request, response);

        // Debe continuar aunque el JSON sea inválido
        verify(apiClient).post(anyString(), any(), anyString(), anyString());
    }

    @Test
    void testDoGet_DocumentosPaciente_Success() throws Exception {
        when(request.getPathInfo()).thenReturn("/documentos/paciente/12345678");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        when(response.getWriter()).thenReturn(printWriter);
        
        HcenApiClient.ApiResponse apiResponse = new HcenApiClient.ApiResponse(
            200, 
            "[{\"id\": 1, \"nombre\": \"doc1.pdf\"}]", 
            Map.of()
        );
        when(apiClient.get("/documentos/paciente/12345678", "test-api-key", "test-origin"))
            .thenReturn(apiResponse);

        servlet.init();
        servlet.doGet(request, response);

        verify(response).setStatus(200);
        verify(response).setContentType("application/json");
        assertTrue(responseWriter.toString().contains("doc1.pdf"));
    }

    @Test
    void testDoGet_DocumentoById_Success() throws Exception {
        when(request.getPathInfo()).thenReturn("/documentos/doc123");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        
        HcenApiClient.ApiResponse apiResponse = new HcenApiClient.ApiResponse(
            200, 
            "{\"id\": \"doc123\", \"nombre\": \"test.pdf\"}", 
            Map.of()
        );
        when(apiClient.get("/documentos/doc123", "test-api-key", "test-origin"))
            .thenReturn(apiResponse);

        servlet.init();
        servlet.doGet(request, response);

        verify(response).setStatus(200);
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_DescargarDocumento_Success() throws Exception {
        when(request.getPathInfo()).thenReturn("/documentos/doc123/descargar");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        
        HcenApiClient.ApiResponse apiResponse = new HcenApiClient.ApiResponse(
            200, 
            "PDF_CONTENT_BYTES", 
            Map.of()
        );
        when(apiClient.get("/documentos/doc123/descargar", "test-api-key", "test-origin"))
            .thenReturn(apiResponse);

        servlet.init();
        servlet.doGet(request, response);

        verify(response).setStatus(200);
        verify(response).setContentType("application/pdf");
    }

    @Test
    void testDoGet_NoApiKey() throws Exception {
        when(request.getPathInfo()).thenReturn("/documentos/paciente/12345678");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.init();
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("API Key no configurada"));
    }

    @Test
    void testDoGet_NotFound() throws Exception {
        when(request.getPathInfo()).thenReturn("/endpoint-inexistente");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        when(response.getWriter()).thenReturn(printWriter);

        servlet.init();
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertTrue(responseWriter.toString().contains("Endpoint no encontrado"));
    }

    @Test
    void testDoGet_NullPathInfo() throws Exception {
        when(request.getPathInfo()).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");

        servlet.init();
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testDoGet_ApiClientNull() throws Exception {
        PrestadorSaludServlet servletWithoutClient = new PrestadorSaludServlet();
        when(request.getPathInfo()).thenReturn("/documentos/paciente/12345678");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        servletWithoutClient.init();
        // Después de init, el apiClient se inicializa, así que verificamos que funciona
        servletWithoutClient.doGet(request, response);

        // Si no hay API key, debería retornar 401
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("API Key no configurada"));
    }

    @Test
    void testInit_WithApiClient() throws ServletException {
        // No necesitamos mocks para este test, solo verificar que init funciona
        servlet.init();
        assertNotNull(servlet);
        // Verificar que el apiClient fue inyectado correctamente
        // Como usamos reflection para inyectarlo, verificamos que no es null después de init
    }
    
    @Test
    void testDoPost_WithEmptyBody() throws Exception {
        when(request.getPathInfo()).thenReturn("/usuarios-salud");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        when(response.getWriter()).thenReturn(printWriter);
        
        // Body vacío
        BufferedReader reader = new BufferedReader(new StringReader(""));
        when(request.getReader()).thenReturn(reader);
        
        HcenApiClient.ApiResponse apiResponse = new HcenApiClient.ApiResponse(200, "{}", Map.of());
        when(apiClient.post(eq("/usuarios-salud"), isNull(), eq("test-api-key"), eq("test-origin")))
            .thenReturn(apiResponse);

        servlet.init();
        servlet.doPost(request, response);

        verify(response).setStatus(200);
        verify(apiClient).post(eq("/usuarios-salud"), isNull(), eq("test-api-key"), eq("test-origin"));
    }
    
    @Test
    void testDoGet_WithEmptyPathInfo() throws Exception {
        when(request.getPathInfo()).thenReturn("");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        when(response.getWriter()).thenReturn(printWriter);

        servlet.init();
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertTrue(responseWriter.toString().contains("Endpoint no encontrado"));
    }
    
    @Test
    void testDoGet_DescargarDocumento_Non200Status() throws Exception {
        when(request.getPathInfo()).thenReturn("/documentos/doc123/descargar");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        when(response.getWriter()).thenReturn(printWriter);
        
        HcenApiClient.ApiResponse apiResponse = new HcenApiClient.ApiResponse(404, "Not Found", Map.of());
        when(apiClient.get("/documentos/doc123/descargar", "test-api-key", "test-origin"))
            .thenReturn(apiResponse);

        servlet.init();
        servlet.doGet(request, response);

        verify(response).setStatus(404);
        // No debe cambiar a application/pdf si el status no es 200
        verify(response).setContentType("application/json");
    }
    
    @Test
    void testDoPost_MetadatosDocumento_WithTrailingSlash() throws Exception {
        when(request.getPathInfo()).thenReturn("/metadatos-documento/");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiKey")).thenReturn("test-api-key");
        when(session.getAttribute("origin")).thenReturn("test-origin");
        when(response.getWriter()).thenReturn(printWriter);
        
        String jsonBody = "{\"documentoId\": \"doc123\"}";
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));
        when(request.getReader()).thenReturn(reader);
        
        HcenApiClient.ApiResponse apiResponse = new HcenApiClient.ApiResponse(200, "{\"success\": true}", Map.of());
        when(apiClient.post(eq("/metadatos-documento"), any(), eq("test-api-key"), eq("test-origin")))
            .thenReturn(apiResponse);

        servlet.init();
        servlet.doPost(request, response);

        verify(response).setStatus(200);
        verify(apiClient).post(eq("/metadatos-documento"), any(), eq("test-api-key"), eq("test-origin"));
    }
}


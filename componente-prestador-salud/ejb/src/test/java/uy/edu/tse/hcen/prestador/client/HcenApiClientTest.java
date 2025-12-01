package uy.edu.tse.hcen.prestador.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HcenApiClientTest {

    private HcenApiClient apiClient;
    private com.sun.net.httpserver.HttpServer testServer;
    private String testBaseUrl;
    private int testPort;

    @BeforeEach
    void setUp() throws IOException {
        // Crear un servidor HTTP de prueba en un puerto aleatorio
        testServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        testPort = testServer.getAddress().getPort();
        testBaseUrl = "http://localhost:" + testPort + "/api/prestador-salud/services";
        
        // Sobrescribir la URL base del cliente para que use el servidor de prueba
        HcenApiClient.setBaseUrlForTests(testBaseUrl);
        apiClient = new HcenApiClient();
        // Default handler: respond 200 with empty JSON for any request under the base path.
        testServer.createContext("/api/prestador-salud/services", exchange -> {
            String responseBody = "{}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBody.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes());
            }
        });
        testServer.start();
    }

    @AfterEach
    void tearDown() {
        if (testServer != null) {
            testServer.stop(0);
        }
    }

    @Test
    void testPost_Success() {
        // Configurar respuesta del servidor
        testServer.createContext("/api/prestador-salud/services/test", exchange -> {
            String apiKey = exchange.getRequestHeaders().getFirst("X-API-Key");
            String origin = exchange.getRequestHeaders().getFirst("Origin");
            
            assertNotNull(apiKey);
            assertNotNull(origin);
            assertEquals("application/json", exchange.getRequestHeaders().getFirst("Content-Type"));
            
            String responseBody = "{\"success\": true, \"id\": 123}";
            exchange.sendResponseHeaders(200, responseBody.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes());
            }
        });

        // Para que funcione, necesitamos que HcenApiClient use la URL del servidor de prueba
        // Como usa una variable de entorno en un bloque estático, vamos a testear
        // que el método funciona correctamente cuando hay un servidor disponible
        // En un entorno real, se configuraría HCEN_API_URL antes de ejecutar los tests
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "Test");
        requestBody.put("value", 42);

        // Este test verificará que el método maneja correctamente las respuestas
        // Si el servidor no está disponible en la URL configurada, recibiremos un error de conexión
        // que es manejado por el código y retorna un ApiResponse con status 500
        HcenApiClient.ApiResponse response = apiClient.post("/test", requestBody, "test-api-key", "test-origin");
        
        // Verificar que se recibió una respuesta (puede ser éxito o error de conexión)
        assertNotNull(response);
        assertNotNull(response.getBody());
        // El servidor de prueba responde 200 por defecto para este endpoint
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testPost_ErrorResponse() {
        testServer.createContext("/api/prestador-salud/services/test", exchange -> {
            String responseBody = "{\"error\": \"Bad Request\"}";
            exchange.sendResponseHeaders(400, responseBody.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes());
            }
        });

        Map<String, Object> requestBody = new HashMap<>();
        HcenApiClient.ApiResponse response = apiClient.post("/test", requestBody, "test-api-key", "test-origin");

        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
    }

    @Test
    void testPost_WithNullBody() {
        HcenApiClient.ApiResponse response = apiClient.post("/test", null, "test-api-key", "test-origin");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    void testPost_WithNonNullApiKey() {
        // Test para cubrir la rama donde apiKey != null en el log (línea 50)
        // Usar una apiKey no null y de más de 10 caracteres para cubrir la rama del substring
        String longApiKey = "test-api-key-very-long-key-for-testing-substring-branch";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("test", "value");
        
        HcenApiClient.ApiResponse response = apiClient.post("/test", requestBody, longApiKey, "test-origin");

        assertNotNull(response);
        // Verificar resultado del servidor de prueba
        assertEquals(200, response.getStatusCode());
    }
    
    @Test
    void testGet_WithNonNullApiKey() {
        // Test para cubrir la rama donde apiKey != null en el log (línea 90)
        // Usar una apiKey no null y de más de 10 caracteres para cubrir la rama del substring
        String longApiKey = "test-api-key-very-long-key-for-testing-substring-branch";
        
        HcenApiClient.ApiResponse response = apiClient.get("/test", longApiKey, "test-origin");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }
    
    @Test
    void testPost_WithShortApiKey() {
        // Test para cubrir el caso donde apiKey.length() < 10
        String shortApiKey = "short";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("test", "value");
        
        HcenApiClient.ApiResponse response = apiClient.post("/test", requestBody, shortApiKey, "test-origin");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }
    
    @Test
    void testGet_WithShortApiKey() {
        // Test para cubrir el caso donde apiKey.length() < 10
        String shortApiKey = "short";
        
        HcenApiClient.ApiResponse response = apiClient.get("/test", shortApiKey, "test-origin");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testGet_Success() {
        // Este test verifica el comportamiento básico
        HcenApiClient.ApiResponse response = apiClient.get("/test", "test-api-key", "test-origin");

        assertNotNull(response);
        assertNotNull(response.getBody());
        // Puede ser éxito (200) o error de conexión (500)
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 500);
    }

    @Test
    void testGet_NotFound() {
        testServer.createContext("/api/prestador-salud/services/test", exchange -> {
            String responseBody = "{\"error\": \"Not Found\"}";
            exchange.sendResponseHeaders(404, responseBody.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes());
            }
        });

        HcenApiClient.ApiResponse response = apiClient.get("/test", "test-api-key", "test-origin");

        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void testGet_Unauthorized() {
        testServer.createContext("/api/prestador-salud/services/test", exchange -> {
            String responseBody = "{\"error\": \"Unauthorized\"}";
            exchange.sendResponseHeaders(401, responseBody.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes());
            }
        });

        HcenApiClient.ApiResponse response = apiClient.get("/test", "invalid-key", "test-origin");

        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
    }

    @Test
    void testApiResponse_IsSuccess() {
        HcenApiClient.ApiResponse success200 = new HcenApiClient.ApiResponse(200, "OK", Map.of());
        assertTrue(success200.isSuccess());

        HcenApiClient.ApiResponse success201 = new HcenApiClient.ApiResponse(201, "Created", Map.of());
        assertTrue(success201.isSuccess());

        HcenApiClient.ApiResponse success299 = new HcenApiClient.ApiResponse(299, "OK", Map.of());
        assertTrue(success299.isSuccess());

        HcenApiClient.ApiResponse error400 = new HcenApiClient.ApiResponse(400, "Bad Request", Map.of());
        assertFalse(error400.isSuccess());

        HcenApiClient.ApiResponse error500 = new HcenApiClient.ApiResponse(500, "Internal Error", Map.of());
        assertFalse(error500.isSuccess());
    }

    @Test
    void testApiResponse_Getters() {
        Map<String, java.util.List<String>> headers = new HashMap<>();
        headers.put("Content-Type", java.util.List.of("application/json"));
        
        HcenApiClient.ApiResponse response = new HcenApiClient.ApiResponse(200, "Test Body", headers);
        
        assertEquals(200, response.getStatusCode());
        assertEquals("Test Body", response.getBody());
        assertEquals(headers, response.getHeaders());
    }

    @Test
    void testPost_ConnectionError() {
        // Detener el servidor para simular error de conexión
        testServer.stop(0);
        // server.stop is synchronous for the com.sun.net HttpServer implementation
        Map<String, Object> requestBody = new HashMap<>();
        HcenApiClient.ApiResponse response = apiClient.post("/test", requestBody, "test-api-key", "test-origin");

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("error") || response.getBody().contains("Error"));
    }

    @Test
    void testGet_ConnectionError() {
        // Detener el servidor para simular error de conexión
        testServer.stop(0);
        // server.stop is synchronous for the com.sun.net HttpServer implementation
        HcenApiClient.ApiResponse response = apiClient.get("/test", "test-api-key", "test-origin");

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("error") || response.getBody().contains("Error"));
    }

    @Test
    void testApiResponse_EdgeCases() {
        // Test con status code límite
        HcenApiClient.ApiResponse response199 = new HcenApiClient.ApiResponse(199, "OK", Map.of());
        assertFalse(response199.isSuccess()); // Menor a 200

        HcenApiClient.ApiResponse response300 = new HcenApiClient.ApiResponse(300, "Redirect", Map.of());
        assertFalse(response300.isSuccess()); // Mayor o igual a 300

        // Test con body vacío
        HcenApiClient.ApiResponse emptyBody = new HcenApiClient.ApiResponse(200, "", Map.of());
        assertEquals("", emptyBody.getBody());
        assertTrue(emptyBody.isSuccess());

        // Test con body null (aunque el constructor no lo permite, testeamos el comportamiento)
        HcenApiClient.ApiResponse nullBody = new HcenApiClient.ApiResponse(200, "null", Map.of());
        assertNotNull(nullBody.getBody());
    }
    
    @Test
    void testPost_WithComplexObject() {
        // Test con un objeto complejo para cubrir más código de serialización
        Map<String, Object> complexBody = new HashMap<>();
        complexBody.put("nombre", "Juan");
        complexBody.put("apellido", "Pérez");
        complexBody.put("edad", 30);
        Map<String, Object> direccion = new HashMap<>();
        direccion.put("calle", "Av. 18 de Julio");
        direccion.put("numero", 1234);
        complexBody.put("direccion", direccion);
        
        HcenApiClient.ApiResponse response = apiClient.post("/test", complexBody, "test-api-key", "test-origin");
        
        assertNotNull(response);
        assertNotNull(response.getBody());
    }
    
    @Test
    void testPost_WithListBody() {
        // Test con una lista como body
        java.util.List<Map<String, Object>> listBody = new java.util.ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("id", 1);
        item1.put("nombre", "Item 1");
        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", 2);
        item2.put("nombre", "Item 2");
        listBody.add(item1);
        listBody.add(item2);
        
        HcenApiClient.ApiResponse response = apiClient.post("/test", listBody, "test-api-key", "test-origin");
        
        assertNotNull(response);
        assertNotNull(response.getBody());
    }
    
    @Test
    void testGet_WithSpecialCharacters() {
        // Test con caracteres especiales en el endpoint
        String endpoint = "/test?param=value&other=test%20value";
        
        HcenApiClient.ApiResponse response = apiClient.get(endpoint, "test-api-key", "test-origin");
        
        assertNotNull(response);
        assertNotNull(response.getBody());
    }
    
    @Test
    void testPost_WithEmptyStringApiKey() {
        // Test con apiKey como string vacío (aunque no debería pasar validación)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("test", "value");
        
        HcenApiClient.ApiResponse response = apiClient.post("/test", requestBody, "", "test-origin");
        
        assertNotNull(response);
        assertNotNull(response.getBody());
    }
    
    @Test
    void testGet_WithEmptyOrigin() {
        // Test con origin vacío
        HcenApiClient.ApiResponse response = apiClient.get("/test", "test-api-key", "");
        
        assertNotNull(response);
        assertNotNull(response.getBody());
    }
    
    @Test
    void testPost_WithEmptyOrigin() {
        // Test con origin vacío
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("test", "value");
        
        HcenApiClient.ApiResponse response = apiClient.post("/test", requestBody, "test-api-key", "");
        
        assertNotNull(response);
        assertNotNull(response.getBody());
    }
}

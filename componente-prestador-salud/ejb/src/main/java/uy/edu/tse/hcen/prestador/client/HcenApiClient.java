package uy.edu.tse.hcen.prestador.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.List;

/**
 * Cliente HTTP para consumir los servicios de HCEN.
 * Bean CDI ApplicationScoped para ser inyectado en otros componentes.
 */
@ApplicationScoped
public class HcenApiClient {
    
    private static final Logger LOGGER = Logger.getLogger(HcenApiClient.class);
    
    // Base URL configurable via environment variable; tests may override via setter
    private static String baseURL = (System.getenv() != null)
        ? System.getenv().getOrDefault("HCEN_API_URL", "http://host.docker.internal:8080/hcen/api/prestador-salud/services")
        : "http://host.docker.internal:8080/hcen/api/prestador-salud/services";

    static {
        LOGGER.info(" [HCEN-API-CLIENT] URL base configurada");
        if (System.getenv("HCEN_API_URL") != null) {
            LOGGER.info(" [HCEN-API-CLIENT] Variable de entorno HCEN_API_URL está presente");
        }
    }
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public HcenApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    // Package-private setter used by tests to override the base URL
    static void setBaseUrlForTests(String url) {
        if (url != null && !url.isBlank()) {
            baseURL = url;
            LOGGER.info(" [HCEN-API-CLIENT] URL base sobreescrita para tests");
        }
    }
    
    /**
     * Realiza una petición POST a un endpoint de HCEN.
     */
    public ApiResponse post(String endpoint, Object body, String apiKey, String origin) {
        String fullUrl = baseURL + endpoint;
        LOGGER.info("📤 [HCEN-API-CLIENT] POST a: " + fullUrl);
        LOGGER.info("📤 [HCEN-API-CLIENT] Headers - X-API-Key: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null"));
        LOGGER.info("📤 [HCEN-API-CLIENT] Headers - Origin: " + origin);
        
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            LOGGER.debugf("📤 [HCEN-API-CLIENT] Body: %s", jsonBody);

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            if (apiKey != null) {
                reqBuilder.header("X-API-Key", apiKey);
            }
            if (origin != null) {
                reqBuilder.header("Origin", origin);
            }

            HttpRequest request = reqBuilder.build();
            
            LOGGER.info("📤 [HCEN-API-CLIENT] Enviando petición POST...");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            LOGGER.info("📥 [HCEN-API-CLIENT] Respuesta recibida - Status: " + response.statusCode());
            LOGGER.debugf("📥 [HCEN-API-CLIENT] Respuesta body: %s", response.body());
            
            return new ApiResponse(
                response.statusCode(),
                response.body(),
                response.headers().map()
            );
            
        } catch (InterruptedException ie) {
            LOGGER.error("❌ [HCEN-API-CLIENT] Petición POST interrumpida a " + fullUrl, ie);
            Thread.currentThread().interrupt();
            return new ApiResponse(500, "{\"error\": \"internal\"}", Map.of());
        } catch (IOException e) {
            LOGGER.error("❌ [HCEN-API-CLIENT] Error en petición POST a " + fullUrl, e);
            return new ApiResponse(500, "{\"error\": \"internal\"}", Map.of());
        }
    }
    
    /**
     * Realiza una petición GET a un endpoint de HCEN.
     */
    public ApiResponse get(String endpoint, String apiKey, String origin) {
        String fullUrl = baseURL + endpoint;
        LOGGER.info("📤 [HCEN-API-CLIENT] GET a: " + fullUrl);
        LOGGER.info("📤 [HCEN-API-CLIENT] Headers - X-API-Key: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null"));
        LOGGER.info("📤 [HCEN-API-CLIENT] Headers - Origin: " + origin);
        
        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofSeconds(30))
                .GET();

            if (apiKey != null) {
                reqBuilder.header("X-API-Key", apiKey);
            }
            if (origin != null) {
                reqBuilder.header("Origin", origin);
            }

            HttpRequest request = reqBuilder.build();
            
            LOGGER.info("📤 [HCEN-API-CLIENT] Enviando petición GET...");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            LOGGER.info("📥 [HCEN-API-CLIENT] Respuesta recibida - Status: " + response.statusCode());
            LOGGER.debug("📥 [HCEN-API-CLIENT] Respuesta body: " + response.body());
            
            return new ApiResponse(
                response.statusCode(),
                response.body(),
                response.headers().map()
            );
            
        } catch (InterruptedException ie) {
            LOGGER.error("❌ [HCEN-API-CLIENT] Petición GET interrumpida a " + fullUrl, ie);
            Thread.currentThread().interrupt();
            return new ApiResponse(500, "{\"error\": \"internal\"}", Map.of());
        } catch (IOException e) {
            LOGGER.error("❌ [HCEN-API-CLIENT] Error en petición GET a " + fullUrl, e);
            return new ApiResponse(500, "{\"error\": \"internal\"}", Map.of());
        }
    }
    
    /**
     * Clase para encapsular la respuesta de la API.
     */
    public static class ApiResponse {
        private final int statusCode;
        private final String body;
        private final Map<String, List<String>> headers;
        
        public ApiResponse(int statusCode, String body, Map<String, List<String>> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getBody() {
            return body;
        }
        
        public Map<String, List<String>> getHeaders() {
            return headers;
        }
        
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}


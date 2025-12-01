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

/**
 * Cliente HTTP para consumir los servicios de HCEN.
 */
@ApplicationScoped
public class HcenApiClient {
    
    private static final Logger LOGGER = Logger.getLogger(HcenApiClient.class);
    
    private static final String BASE_URL = System.getenv().getOrDefault(
        "HCEN_API_URL", 
        "http://host.docker.internal:8080/api/prestador-salud/services"
    );
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public HcenApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Realiza una petición POST a un endpoint de HCEN.
     */
    public ApiResponse post(String endpoint, Object body, String apiKey, String origin) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("X-API-Key", apiKey)
                .header("Origin", origin)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(30))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return new ApiResponse(
                response.statusCode(),
                response.body(),
                response.headers().map()
            );
            
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error en petición POST: " + e.getMessage(), e);
            return new ApiResponse(500, "{\"error\": \"" + e.getMessage() + "\"}", Map.of());
        }
    }
    
    /**
     * Realiza una petición GET a un endpoint de HCEN.
     */
    public ApiResponse get(String endpoint, String apiKey, String origin) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("X-API-Key", apiKey)
                .header("Origin", origin)
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return new ApiResponse(
                response.statusCode(),
                response.body(),
                response.headers().map()
            );
            
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error en petición GET: " + e.getMessage(), e);
            return new ApiResponse(500, "{\"error\": \"" + e.getMessage() + "\"}", Map.of());
        }
    }
    
    /**
     * Clase para encapsular la respuesta de la API.
     */
    public static class ApiResponse {
        private final int statusCode;
        private final String body;
        private final Map<String, java.util.List<String>> headers;
        
        public ApiResponse(int statusCode, String body, Map<String, java.util.List<String>> headers) {
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
        
        public Map<String, java.util.List<String>> getHeaders() {
            return headers;
        }
        
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}


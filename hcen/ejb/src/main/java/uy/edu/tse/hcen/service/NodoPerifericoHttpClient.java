package uy.edu.tse.hcen.service;

import jakarta.ejb.Stateless;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cliente HTTP para comunicarse con el componente periférico multi-tenant.
 * 
 * Encapsula las llamadas HTTP para inicialización, actualización y eliminación
 * de clínicas en el nodo periférico.
 */
@Stateless
public class NodoPerifericoHttpClient {

    private static final Logger LOGGER = Logger.getLogger(NodoPerifericoHttpClient.class.getName());
    
    private final HttpClient httpClient;
    
    public NodoPerifericoHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * DTO para enviar datos de inicialización al componente periférico.
     */
    public static class NodoInitPayload {
        public Long id;
        public String rut;
        public String nombre;
        public String departamento;
        public String localidad;
        public String direccion;
        public String nodoPerifericoUrlBase;
        public String nodoPerifericoUsuario;
        public String nodoPerifericoPassword;
        public String contacto;
        public String url;
    }

    /**
     * DTO para recibir la respuesta del endpoint /config/init.
     */
    public static class InitResponse {
        public boolean success;
        public String adminNickname;
        public String activationUrl;
        public String activationToken;
        public String tokenExpiresAt;
        public String errorMessage;
    }

    /**
     * Llama al endpoint /config/init del componente periférico para inicializar un nuevo tenant.
     * 
     * @param baseUrl URL base del nodo periférico (ej: http://localhost:8081)
     * @param payload Datos de la clínica a inicializar
     * @return InitResponse con datos de activación si exitoso, o con error si falla
     */
    public InitResponse initializeTenant(String baseUrl, NodoInitPayload payload) {
        InitResponse result = new InitResponse();
        result.success = false;
        
        if (baseUrl == null || baseUrl.isBlank()) {
            LOGGER.warning("Cannot initialize tenant: baseUrl is null or empty");
            result.errorMessage = "baseUrl is null or empty";
            return result;
        }

        try {
            // Construir URL completa
            String url = normalizeBaseUrl(baseUrl) + "/hcen-web/api/config/init";
            
            // Convertir payload a JSON manualmente (simple)
            String jsonPayload = payloadToJson(payload);
            
            LOGGER.info(String.format("Sending init request to %s for clinic: %s (RUT: %s)", 
                                     url, payload.nombre, payload.rut));
            LOGGER.fine("Payload: " + jsonPayload);
            
            // Construir request HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            
            // Enviar request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            int statusCode = response.statusCode();
            String responseBody = response.body();
            
            LOGGER.info(String.format("Response from peripheral node: status=%d, body=%s", 
                                     statusCode, responseBody));
            
            if (statusCode >= 200 && statusCode < 300) {
                LOGGER.info("Tenant initialized successfully in peripheral node");
                result.success = true;
                
                // Parsear respuesta JSON para extraer datos de activación
                result.adminNickname = extractJsonField(responseBody, "adminNickname");
                result.activationUrl = extractJsonField(responseBody, "activationUrl");
                result.activationToken = extractJsonField(responseBody, "activationToken");
                result.tokenExpiresAt = extractJsonField(responseBody, "tokenExpiresAt");
                
                return result;
            } else {
                LOGGER.warning(String.format("Failed to initialize tenant. Status: %d, Response: %s", 
                                            statusCode, responseBody));
                result.errorMessage = "HTTP " + statusCode + ": " + responseBody;
                return result;
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Request interrupted while initializing tenant", e);
            result.errorMessage = "Request interrupted: " + e.getMessage();
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing tenant in peripheral node", e);
            result.errorMessage = "Error: " + e.getMessage();
            return result;
        }
    }

    /**
     * Llama al endpoint /config/update para actualizar la configuración de una clínica.
     * 
     * @param baseUrl URL base del nodo periférico
     * @param payload Datos actualizados
     * @return true si exitoso
     */
    public boolean updateTenant(String baseUrl, NodoInitPayload payload) {
        if (baseUrl == null || baseUrl.isBlank()) {
            LOGGER.warning("Cannot update tenant: baseUrl is null or empty");
            return false;
        }

        try {
            String url = normalizeBaseUrl(baseUrl) + "/hcen-web/api/config/update";
            String jsonPayload = payloadToJson(payload);
            
            LOGGER.info(String.format("Sending update request to %s for clinic ID: %s", url, payload.id));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            int statusCode = response.statusCode();
            LOGGER.info(String.format("Update response: status=%d", statusCode));
            
            return statusCode >= 200 && statusCode < 300;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Request interrupted while updating tenant", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating tenant in peripheral node", e);
            return false;
        }
    }

    /**
     * Llama al endpoint /config/delete para eliminar una clínica.
     * 
     * @param baseUrl URL base del nodo periférico
     * @param clinicId ID de la clínica a eliminar
     * @return true si exitoso
     */
    public boolean deleteTenant(String baseUrl, Long clinicId) {
        if (baseUrl == null || baseUrl.isBlank()) {
            LOGGER.warning("Cannot delete tenant: baseUrl is null or empty");
            return false;
        }

        try {
            String url = normalizeBaseUrl(baseUrl) + "/hcen-web/api/config/delete";
            String jsonPayload = String.format("{\"id\": %d}", clinicId);
            
            LOGGER.info(String.format("Sending delete request to %s for clinic ID: %s", url, clinicId));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            int statusCode = response.statusCode();
            LOGGER.info(String.format("Delete response: status=%d", statusCode));
            
            return statusCode >= 200 && statusCode < 300;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Request interrupted while deleting tenant", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting tenant in peripheral node", e);
            return false;
        }
    }

    /**
     * Normaliza la URL base eliminando trailing slashes.
     */
    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) return "";
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /**
     * Convierte el payload a JSON de forma simple (sin usar librerías externas).
     * En producción se podría usar Jackson o JSON-B.
     */
    private String payloadToJson(NodoInitPayload payload) {
        StringBuilder json = new StringBuilder("{");
        
        appendJsonField(json, "id", payload.id, true);
        appendJsonField(json, "rut", payload.rut, false);
        appendJsonField(json, "nombre", payload.nombre, false);
        appendJsonField(json, "departamento", payload.departamento, false);
        appendJsonField(json, "localidad", payload.localidad, false);
        appendJsonField(json, "direccion", payload.direccion, false);
        appendJsonField(json, "nodoPerifericoUrlBase", payload.nodoPerifericoUrlBase, false);
        appendJsonField(json, "nodoPerifericoUsuario", payload.nodoPerifericoUsuario, false);
        appendJsonField(json, "nodoPerifericoPassword", payload.nodoPerifericoPassword, false);
        appendJsonField(json, "contacto", payload.contacto, false);
        appendJsonField(json, "url", payload.url, false);
        
        // Remover última coma
        if (json.charAt(json.length() - 1) == ',') {
            json.setLength(json.length() - 1);
        }
        
        json.append("}");
        return json.toString();
    }

    /**
     * Agrega un campo al JSON si no es null.
     */
    private void appendJsonField(StringBuilder json, String key, Object value, boolean isNumeric) {
        if (value != null) {
            json.append("\"").append(key).append("\":");
            if (isNumeric) {
                json.append(value);
            } else {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            }
            json.append(",");
        }
    }

    /**
     * Escapa caracteres especiales en JSON.
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    /**
     * Extrae el valor de un campo de un JSON simple.
     * Usa regex básico (no es un parser completo, solo para campos simples).
     */
    private String extractJsonField(String json, String fieldName) {
        if (json == null || fieldName == null) return null;
        
        try {
            // Buscar patrón: "fieldName":"valor" o "fieldName":valor
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\""
            );
            java.util.regex.Matcher matcher = pattern.matcher(json);
            
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            // Si no tiene comillas, buscar valor numérico o sin comillas
            pattern = java.util.regex.Pattern.compile(
                "\"" + fieldName + "\"\\s*:\\s*([^,}\\]]+)"
            );
            matcher = pattern.matcher(json);
            
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            
        } catch (Exception e) {
            LOGGER.warning("Error extracting field " + fieldName + " from JSON: " + e.getMessage());
        }
        
        return null;
    }
}


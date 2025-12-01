package uy.edu.tse.hcen.prestador.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uy.edu.tse.hcen.prestador.client.HcenApiClient;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet principal para consumir los servicios de HCEN.
 */
public class PrestadorSaludServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(PrestadorSaludServlet.class);
    
    private final HcenApiClient apiClient = new HcenApiClient();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String apiKey = (String) request.getSession().getAttribute("apiKey");
        String origin = (String) request.getSession().getAttribute("origin");
        
        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"API Key no configurada. Configure primero en /config\"}");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        
        // Leer body de la petición
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            body.append(line);
        }
        
        Object requestBody = null;
        if (body.length() > 0) {
            try {
                requestBody = objectMapper.readValue(body.toString(), Object.class);
            } catch (Exception e) {
                LOGGER.warn("Error parseando body JSON: " + e.getMessage());
            }
        }
        
        HcenApiClient.ApiResponse apiResponse;
        
        if (pathInfo.equals("/usuarios-salud") || pathInfo.equals("/usuarios-salud/")) {
            // Alta de usuario en INUS
            apiResponse = apiClient.post("/usuarios-salud", requestBody, apiKey, origin);
            
        } else if (pathInfo.equals("/metadatos-documento") || pathInfo.equals("/metadatos-documento/")) {
            // Registrar metadatos de documento
            apiResponse = apiClient.post("/metadatos-documento", requestBody, apiKey, origin);
            
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Endpoint no encontrado\"}");
            return;
        }
        
        // Retornar respuesta
        response.setStatus(apiResponse.getStatusCode());
        response.setContentType("application/json");
        response.getWriter().write(apiResponse.getBody());
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String apiKey = (String) request.getSession().getAttribute("apiKey");
        String origin = (String) request.getSession().getAttribute("origin");
        
        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"API Key no configurada. Configure primero en /config\"}");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }
        
        HcenApiClient.ApiResponse apiResponse;
        
        if (pathInfo.startsWith("/documentos/paciente/")) {
            // Listar documentos por CI de paciente
            String ci = pathInfo.substring("/documentos/paciente/".length());
            apiResponse = apiClient.get("/documentos/paciente/" + ci, apiKey, origin);
            
        } else if (pathInfo.startsWith("/documentos/") && pathInfo.endsWith("/descargar")) {
            // Descargar documento
            String id = pathInfo.substring("/documentos/".length(), pathInfo.length() - "/descargar".length());
            apiResponse = apiClient.get("/documentos/" + id + "/descargar", apiKey, origin);
            
        } else if (pathInfo.startsWith("/documentos/")) {
            // Obtener metadatos de documento
            String id = pathInfo.substring("/documentos/".length());
            apiResponse = apiClient.get("/documentos/" + id, apiKey, origin);
            
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Endpoint no encontrado\"}");
            return;
        }
        
        // Retornar respuesta
        response.setStatus(apiResponse.getStatusCode());
        response.setContentType("application/json");
        if (apiResponse.getStatusCode() == 200 && pathInfo.endsWith("/descargar")) {
            // Para descargas, retornar el contenido directamente
            response.setContentType("application/pdf");
        }
        response.getWriter().write(apiResponse.getBody());
    }
}


package uy.edu.tse.hcen.prestador.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uy.edu.tse.hcen.prestador.client.HcenApiClient;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Servlet principal para consumir los servicios de HCEN.
 * Usa inyección CDI para obtener el cliente API.
 */
public class PrestadorSaludServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PrestadorSaludServlet.class);
    private static final String APPLICATION_JSON = "application/json";
    private static final String DOWNLOAD = "/descargar";
    private static final String DOCUMENTOS_PACIENTE_PREFIX = "/documentos/paciente/";
    private static final String DOCUMENTOS_PREFIX = "/documentos/";
    private static final String ERROR_KEY = "error";

    @Inject
    private HcenApiClient apiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Package-private setter for tests to inject a mock client without reflection
    void setApiClient(HcenApiClient client) {
        this.apiClient = client;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        LOGGER.info("Inicializando PrestadorSaludServlet...");
        if (apiClient == null) {
            LOGGER.warn("HcenApiClient no inyectado via CDI, intentando obtenerlo manualmente");
            try {
                jakarta.enterprise.inject.spi.CDI<Object> cdi = jakarta.enterprise.inject.spi.CDI.current();
                apiClient = cdi.select(HcenApiClient.class).get();
                LOGGER.info("HcenApiClient obtenido via CDI manual");
            } catch (Exception e) {
                LOGGER.warn("No se pudo obtener HcenApiClient via CDI, creando instancia directa", e);
                try {
                    apiClient = new HcenApiClient();
                } catch (Exception ex) {
                    LOGGER.error("No se pudo crear instancia directa de HcenApiClient", ex);
                    throw new ServletException("No se pudo inicializar HcenApiClient", ex);
                }
            }
        } else {
            LOGGER.info("HcenApiClient inyectado correctamente via @Inject");
        }
        LOGGER.info("PrestadorSaludServlet inicializado correctamente");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.infof("PrestadorSaludServlet doPost llamado para: %s, pathInfo: %s", request.getRequestURI(), request.getPathInfo());

        if (apiClient == null) {
            LOGGER.error("apiClient es null!");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Map.of(ERROR_KEY, "Error interno: apiClient no inicializado"));
            return;
        }

        String apiKey = getSessionString(request, "apiKey");
        String origin = getSessionString(request, "origin");

        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                Map.of(ERROR_KEY, "API Key no configurada. Configure primero en /config"));
            return;
        }

        String pathInfo = Objects.toString(request.getPathInfo(), "");

        // Leer body de la petición y manejar IOException según Sonar (S1989)
        String rawBody = null;
        try {
            rawBody = readRequestBody(request);
        } catch (IOException e) {
            LOGGER.warn("Error leyendo body de la petición", e);
                writeJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of(ERROR_KEY, "Body inválido o lectura fallida"));
            return;
        }
        Object requestBody = null;
        if (rawBody != null && !rawBody.isBlank()) {
            try {
                requestBody = objectMapper.readValue(rawBody, Object.class);
            } catch (Exception e) {
                LOGGER.warn("Error parseando body JSON", e);
            }
        }

        HcenApiClient.ApiResponse apiResponse;

        if (pathInfo.equals("/usuarios-salud") || pathInfo.equals("/usuarios-salud/")) {
            apiResponse = apiClient.post("/usuarios-salud", requestBody, apiKey, origin);
        } else if (pathInfo.equals("/metadatos-documento") || pathInfo.equals("/metadatos-documento/")) {
            apiResponse = apiClient.post("/metadatos-documento", requestBody, apiKey, origin);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, Map.of(ERROR_KEY, "Endpoint no encontrado"));
            return;
        }

        if (apiResponse == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of(ERROR_KEY, "internal"));
            return;
        }

        response.setStatus(apiResponse.getStatusCode());
        // Si la respuesta es un archivo para descargar, usar content type pdf, y manejar IOException
        if (apiResponse.getStatusCode() == 200 && pathInfo.endsWith(DOWNLOAD)) {
            writeRawResponse(response, apiResponse.getStatusCode(), "application/pdf", apiResponse.getBody());
        } else {
            writeJsonResponse(response, apiResponse.getStatusCode(), apiResponse.getBody());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.infof("PrestadorSaludServlet doGet llamado para: %s, pathInfo: %s", request.getRequestURI(), request.getPathInfo());

        if (apiClient == null) {
            LOGGER.error("apiClient es null!");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Map.of(ERROR_KEY, "Error interno: apiClient no inicializado"));
            return;
        }

        String apiKey = getSessionString(request, "apiKey");
        String origin = getSessionString(request, "origin");

        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                Map.of(ERROR_KEY, "API Key no configurada. Configure primero en /config"));
            return;
        }

        String pathInfo = Objects.toString(request.getPathInfo(), "");

        HcenApiClient.ApiResponse apiResponse;

        if (pathInfo.startsWith(DOCUMENTOS_PACIENTE_PREFIX)) {
            String ci = pathInfo.substring(DOCUMENTOS_PACIENTE_PREFIX.length());
            apiResponse = apiClient.get(DOCUMENTOS_PACIENTE_PREFIX + ci, apiKey, origin);
        } else if (pathInfo.startsWith(DOCUMENTOS_PREFIX) && pathInfo.endsWith(DOWNLOAD)) {
            String id = pathInfo.substring(DOCUMENTOS_PREFIX.length(), pathInfo.length() - DOWNLOAD.length());
            apiResponse = apiClient.get(DOCUMENTOS_PREFIX + id + DOWNLOAD, apiKey, origin);
        } else if (pathInfo.startsWith(DOCUMENTOS_PREFIX)) {
            String id = pathInfo.substring(DOCUMENTOS_PREFIX.length());
            apiResponse = apiClient.get(DOCUMENTOS_PREFIX + id, apiKey, origin);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, Map.of(ERROR_KEY, "Endpoint no encontrado"));
            return;
        }

        if (apiResponse == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of(ERROR_KEY, "internal"));
            return;
        }

        response.setStatus(apiResponse.getStatusCode());
        if (apiResponse.getStatusCode() == 200 && pathInfo.endsWith(DOWNLOAD)) {
            writeRawResponse(response, apiResponse.getStatusCode(), "application/pdf", apiResponse.getBody());
        } else {
            writeJsonResponse(response, apiResponse.getStatusCode(), apiResponse.getBody());
        }
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        return request.getReader().lines().collect(Collectors.joining());
    }

    private String getSessionString(HttpServletRequest request, String name) {
        Object v = request.getSession().getAttribute(name);
        if (v instanceof String s) {
            return s;
        }
        return null;
    }

    private void writeJsonResponse(HttpServletResponse response, int status, Object body) {
        try {
            response.setContentType(APPLICATION_JSON);
            if (body instanceof String s) {
                response.getWriter().write(s);
            } else {
                response.getWriter().write(objectMapper.writeValueAsString(body));
            }
        } catch (IOException e) {
            LOGGER.error("Error al escribir respuesta JSON", e);
            try {
                response.setContentType(APPLICATION_JSON);
                response.getWriter().write("{\"" + ERROR_KEY + "\": \"Error interno al escribir respuesta\"}");
            } catch (IOException ex) {
                LOGGER.error("No se pudo escribir la respuesta de error", ex);
            }
        }
    }

    private void writeRawResponse(HttpServletResponse response, int status, String contentType, String body) {
        try {
            response.setContentType(contentType);
            response.getWriter().write(body != null ? body : "");
        } catch (IOException e) {
            LOGGER.error("Error al escribir respuesta raw", e);
            try {
                response.setContentType(APPLICATION_JSON);
                response.getWriter().write("{\"" + ERROR_KEY + "\": \"Error interno al escribir respuesta\"}");
            } catch (IOException ex) {
                LOGGER.error("No se pudo escribir la respuesta de error", ex);
            }
        }
    }
}


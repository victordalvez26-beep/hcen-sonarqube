package uy.edu.tse.hcen.utils;

import java.util.logging.Logger;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utilidad para obtener la URL base del servicio de políticas de acceso.
 * 
 * El servicio de políticas está desplegado en el mismo WildFly (mismo EAR)
 * con el contexto /hcen/hcen-politicas-service.
 * 
 * La URL se construye automáticamente basándose en la URL base del servidor:
 * 1. Si POLITICAS_SERVICE_URL está configurada → se usa directamente
 * 2. Si HCEN_BACKEND_BASE_URL está configurada → se usa para construir la URL
 * 3. Si GUBUY_REDIRECT_URI está configurada → se extrae la URL base del servidor
 * 4. Por defecto → http://localhost:8080/hcen/hcen-politicas-service/api (solo desarrollo)
 * 
 * Esta clase centraliza todas las referencias a la URL del servicio de políticas para
 * facilitar la configuración en diferentes ambientes (desarrollo, producción, etc.).
 */
public class PoliticasServiceUrlUtil {
    
    private static final Logger LOG = Logger.getLogger(PoliticasServiceUrlUtil.class.getName());
    
    /**
     * Nombre de la variable de entorno para la URL del servicio de políticas.
     */
    private static final String ENV_POLITICAS_SERVICE_URL = "POLITICAS_SERVICE_URL";
    
    /**
     * Nombre de la variable de entorno para la URL base del backend HCEN.
     */
    private static final String ENV_HCEN_BACKEND_BASE_URL = "HCEN_BACKEND_BASE_URL";
    
    /**
     * Nombre de la variable de entorno para la URL de callback de Gub.uy (contiene la URL base del servidor).
     */
    private static final String ENV_GUBUY_REDIRECT_URI = "GUBUY_REDIRECT_URI";
    
    /**
     * URL por defecto del servicio de políticas (solo para desarrollo local).
     * En desarrollo local, el context-root es / (no /hcen), por lo que el servicio está en /hcen-politicas-service
     */
    private static final String DEFAULT_POLITICAS_SERVICE_URL = "http://localhost:8080/hcen-politicas-service/api";
    
    /**
     * Contexto del servicio de políticas en el servidor.
     * NOTA: Este contexto se concatena a una base URL que ya incluye /hcen/.
     * Por ejemplo: https://dominio.com/hcen + /hcen-politicas-service/api = https://dominio.com/hcen/hcen-politicas-service/api
     */
    private static final String POLITICAS_SERVICE_CONTEXT = "/hcen-politicas-service/api";
    
    /**
     * Obtiene la URL base del servicio de políticas de acceso.
     * 
     * La URL se construye automáticamente basándose en la configuración disponible.
     * 
     * @return La URL base del servicio de políticas
     */
    public static String getBaseUrl() {
        // 1. Verificar si está configurada explícitamente
        String envUrl = System.getenv(ENV_POLITICAS_SERVICE_URL);
        if (envUrl != null && !envUrl.isBlank()) {
            LOG.info("Usando POLITICAS_SERVICE_URL desde variable de entorno: " + envUrl);
            return envUrl;
        }
        String sysPropUrl = System.getProperty(ENV_POLITICAS_SERVICE_URL);
        if (sysPropUrl != null && !sysPropUrl.isBlank()) {
            LOG.info("Usando POLITICAS_SERVICE_URL desde propiedad del sistema: " + sysPropUrl);
            return sysPropUrl;
        }
        
        // 2. Intentar construir desde HCEN_BACKEND_BASE_URL
        String backendBaseUrl = getEnvOrProperty(ENV_HCEN_BACKEND_BASE_URL);
        if (backendBaseUrl != null && !backendBaseUrl.isBlank()) {
            String politicasUrl = buildPoliticasUrlFromBase(backendBaseUrl);
            LOG.info("Construyendo URL de políticas desde HCEN_BACKEND_BASE_URL: " + politicasUrl);
            return politicasUrl;
        }
        
        // 3. Intentar extraer la URL base desde GUBUY_REDIRECT_URI
        String gubuyRedirectUri = getEnvOrProperty(ENV_GUBUY_REDIRECT_URI);
        if (gubuyRedirectUri != null && !gubuyRedirectUri.isBlank()) {
            try {
                String serverBaseUrl = extractServerBaseUrl(gubuyRedirectUri);
                if (serverBaseUrl != null) {
                    String politicasUrl = buildPoliticasUrlFromBase(serverBaseUrl);
                    LOG.info("Construyendo URL de políticas desde GUBUY_REDIRECT_URI: " + politicasUrl);
                    return politicasUrl;
                }
            } catch (Exception e) {
                LOG.warning("Error extrayendo URL base desde GUBUY_REDIRECT_URI: " + e.getMessage());
            }
        }
        
        // 4. Usar URL por defecto (solo desarrollo)
        LOG.warning("No se pudo determinar la URL base del servidor. Usando URL por defecto (localhost). " +
                    "Para producción, configura POLITICAS_SERVICE_URL o HCEN_BACKEND_BASE_URL.");
        return DEFAULT_POLITICAS_SERVICE_URL;
    }
    
    /**
     * Obtiene una variable de entorno o propiedad del sistema.
     * Intenta primero la variable de entorno, luego la propiedad del sistema.
     */
    private static String getEnvOrProperty(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return System.getProperty(key);
    }
    
    /**
     * Extrae la URL base del servidor desde una URL completa.
     * Ejemplo: https://env-6105410.web.elasticloud.uy/hcen/api/auth/login/callback
     *          → https://env-6105410.web.elasticloud.uy
     */
    private static String extractServerBaseUrl(String fullUrl) {
        try {
            URI uri = new URI(fullUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            
            if (scheme == null || host == null) {
                return null;
            }
            
            if (port != -1 && port != 80 && port != 443) {
                return scheme + "://" + host + ":" + port;
            } else {
                return scheme + "://" + host;
            }
        } catch (URISyntaxException e) {
            LOG.warning("Error parseando URL: " + fullUrl + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Construye la URL del servicio de políticas desde la URL base del servidor.
     * 
     * Si la URL base es HTTPS, la convierte a HTTP para llamadas internas al mismo servidor.
     * Esto evita problemas de certificados SSL para llamadas internas.
     * 
     * Si la baseUrl ya incluye /hcen/, usa solo /hcen-politicas-service/api.
     * Si no, asume que la baseUrl es solo el dominio y agrega /hcen/hcen-politicas-service/api.
     */
    private static String buildPoliticasUrlFromBase(String baseUrl) {
        // Asegurar que no termine con /
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        // Si la URL es HTTPS, significa que estamos en producción con SSL externo.
        // Para llamadas internas al mismo servidor, debemos usar HTTP en el mismo host.
        // Si el servidor no acepta HTTP directamente, se debe configurar POLITICAS_SERVICE_URL explícitamente.
        if (baseUrl.startsWith("https://")) {
            // Convertir HTTPS a HTTP para llamadas internas (mismo host)
            // Esto evita problemas de certificados SSL para llamadas internas
            baseUrl = baseUrl.replaceFirst("^https://", "http://");
            LOG.info("Convertiendo HTTPS a HTTP para llamada interna al mismo servidor: " + baseUrl);
            LOG.warning("Si esta URL no funciona, configura POLITICAS_SERVICE_URL explícitamente con la URL interna correcta.");
        }
        
        // Determinar el contexto a usar según el ambiente
        // En desarrollo local (localhost:8080), el context-root es /, entonces el servicio está en /hcen-politicas-service
        // En producción (con /hcen), el servicio está en /hcen/hcen-politicas-service
        String contextToUse;
        if (baseUrl.endsWith("/hcen")) {
            // La baseUrl ya incluye /hcen, usar contexto sin /hcen/
            contextToUse = POLITICAS_SERVICE_CONTEXT; // Ya es /hcen-politicas-service/api
        } else {
            // La baseUrl es solo el dominio, agregar /hcen/ antes del contexto
            contextToUse = "/hcen" + POLITICAS_SERVICE_CONTEXT; // /hcen/hcen-politicas-service/api
        }
        
        return baseUrl + contextToUse;
    }
    
    /**
     * Construye una URL completa para un path dado, usando la URL base del servicio de políticas.
     * 
     * @param path El path relativo al base URL (ej: "/politicas" o "/politicas/listar")
     * @return La URL completa
     */
    public static String buildUrl(String path) {
        String baseUrl = getBaseUrl();
        // Asegurarse de que el path comience con /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        // Si la baseUrl ya termina en /api, no duplicar
        if (baseUrl.endsWith("/api")) {
            return baseUrl + path;
        }
        // Si la baseUrl termina en /api/, quitar el / final para evitar doble slash
        if (baseUrl.endsWith("/api/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        // Si no tiene /api, agregarlo
        if (!baseUrl.contains("/api")) {
            return baseUrl + "/api" + path;
        }
        return baseUrl + path;
    }
}


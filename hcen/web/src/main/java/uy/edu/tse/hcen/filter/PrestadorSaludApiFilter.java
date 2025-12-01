package uy.edu.tse.hcen.filter;

import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import uy.edu.tse.hcen.model.PrestadorSalud;
import uy.edu.tse.hcen.service.PrestadorSaludService;

import java.net.URI;

/**
 * Filtro de seguridad para validar peticiones de prestadores de salud.
 * 
 * Valida:
 * 1. API Key en header X-API-Key
 * 2. Origen de la petición coincide con la URL registrada del prestador
 */
@Provider
@PrestadorSaludApiSecured
public class PrestadorSaludApiFilter implements ContainerRequestFilter {
    
    private static final Logger LOGGER = Logger.getLogger(PrestadorSaludApiFilter.class);
    
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String ORIGIN_HEADER = "Origin";
    
    @EJB
    private PrestadorSaludService prestadorService;
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        try {
            // 1. Obtener API Key del header
            String apiKey = requestContext.getHeaderString(API_KEY_HEADER);
            
            if (apiKey == null || apiKey.isEmpty()) {
                LOGGER.warn("Petición rechazada: API Key no proporcionada");
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"API Key requerida en header X-API-Key\"}")
                        .build()
                );
                return;
            }
            
            // 2. Buscar prestador por API Key
            PrestadorSalud prestador = prestadorService.obtenerPorApiKey(apiKey);
            
            if (prestador == null) {
                LOGGER.warn("Petición rechazada: API Key inválida");
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"API Key inválida\"}")
                        .build()
                );
                return;
            }
            
            // 3. Verificar que el prestador esté activo
            if (prestador.getEstado() != uy.edu.tse.hcen.model.EstadoPrestador.ACTIVO) {
                LOGGER.warn("Petición rechazada: Prestador no activo (ID: " + prestador.getId() + ")");
                requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"Prestador no activo\"}")
                        .build()
                );
                return;
            }
            
            // 4. Validar origen de la petición
            String origin = requestContext.getHeaderString(ORIGIN_HEADER);
            String referer = requestContext.getHeaderString("Referer");
            
            // Si no hay Origin, intentar con Referer
            String requestOrigin = origin != null ? origin : referer;
            
            if (prestador.getUrl() != null && !prestador.getUrl().isEmpty()) {
                if (!validarOrigen(requestOrigin, prestador.getUrl())) {
                    LOGGER.warn(String.format(
                        "Petición rechazada: Origen no autorizado. Origen: %s, URL registrada: %s, Prestador ID: %d",
                        requestOrigin, prestador.getUrl(), prestador.getId()
                    ));
                    requestContext.abortWith(
                        Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"error\": \"Origen de petición no autorizado. Debe coincidir con la URL registrada del prestador\"}")
                            .build()
                    );
                    return;
                }
            }
            
            // 5. Agregar información del prestador al contexto para uso en el recurso
            // Establecer en ContainerRequestContext
            requestContext.setProperty("prestadorId", prestador.getId());
            requestContext.setProperty("prestadorNombre", prestador.getNombre());
            
            // También establecer en HttpServletRequest para acceso desde el recurso
            // En RestEasy, podemos obtener HttpServletRequest desde las propiedades
            try {
                Object httpRequestObj = requestContext.getProperty("org.jboss.resteasy.spi.HttpRequest");
                if (httpRequestObj != null) {
                    // Usar reflexión para obtener HttpServletRequest si está disponible
                    try {
                        java.lang.reflect.Method getHttpServletRequestMethod = 
                            httpRequestObj.getClass().getMethod("getHttpServletRequest");
                        HttpServletRequest servletRequest = (HttpServletRequest) getHttpServletRequestMethod.invoke(httpRequestObj);
                        if (servletRequest != null) {
                            servletRequest.setAttribute("prestadorId", prestador.getId());
                            servletRequest.setAttribute("prestadorNombre", prestador.getNombre());
                        }
                    } catch (Exception e) {
                        LOGGER.debug("No se pudo obtener HttpServletRequest mediante reflexión: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                // Si no podemos obtener HttpServletRequest, continuar sin él
                // El recurso puede intentar obtener desde ContainerRequestContext
                LOGGER.debug("No se pudo establecer atributos en HttpServletRequest: " + e.getMessage());
            }
            
            LOGGER.info(String.format(
                "Petición autorizada - Prestador: %s (ID: %d), Origen: %s",
                prestador.getNombre(), prestador.getId(), requestOrigin
            ));
            
        } catch (Exception e) {
            LOGGER.error("Error en filtro de seguridad: " + e.getMessage(), e);
            requestContext.abortWith(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error interno de seguridad\"}")
                    .build()
            );
        }
    }
    
    /**
     * Valida que el origen de la petición coincida con la URL registrada del prestador.
     * 
     * Compara solo el dominio/host sin el puerto, permitiendo variaciones en protocolo y puerto.
     * Esto permite flexibilidad en diferentes ambientes (desarrollo, staging, producción).
     */
    private boolean validarOrigen(String requestOrigin, String registeredUrl) {
        if (requestOrigin == null || requestOrigin.isEmpty()) {
            // Si no hay origen, permitir (puede ser una petición directa sin navegador)
            // En producción, podrías querer ser más estricto
            return true;
        }
        
        try {
            // Extraer solo el dominio/host sin el puerto de ambas URLs
            String originHostOnly = extraerHostSinPuerto(requestOrigin);
            String registeredHostOnly = extraerHostSinPuerto(registeredUrl);
            
            if (originHostOnly == null || registeredHostOnly == null) {
                return false;
            }
            
            // Comparar solo el host (case-insensitive), ignorando el puerto
            boolean matches = originHostOnly.equalsIgnoreCase(registeredHostOnly);
            
            if (matches) {
                LOGGER.debug(String.format(
                    "Origen validado (solo host, ignorando puerto): %s -> %s",
                    requestOrigin, registeredUrl
                ));
            }
            
            return matches;
            
        } catch (Exception e) {
            LOGGER.warn("Error validando origen: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extrae solo el nombre del host/dominio sin el puerto de una URL.
     * Ejemplo: "http://localhost:8083/api" -> "localhost"
     *          "https://clinica.com:8080" -> "clinica.com"
     */
    private String extraerHostSinPuerto(String url) {
        try {
            if (url == null || url.isEmpty()) {
                return null;
            }
            
            // Si no tiene protocolo, agregarlo temporalmente para parsear
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            URI uri = new URI(url);
            String host = uri.getHost();
            
            return host;
            
        } catch (Exception e) {
            LOGGER.warn("Error extrayendo host de URL: " + url + " - " + e.getMessage());
            return null;
        }
    }
    
}


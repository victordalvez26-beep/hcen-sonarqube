package uy.edu.tse.hcen.rest.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Filtro JAX-RS para agregar headers CORS a todas las respuestas.
 * 
 * ⚠️ IMPORTANTE: Este filtro NO se llama manualmente. JAX-RS lo ejecuta AUTOMÁTICAMENTE.
 * 
 * 🔄 CÓMO FUNCIONA:
 * 1. Está registrado en RestApplication.getClasses() (línea 18)
 * 2. JAX-RS detecta que implementa ContainerResponseFilter y tiene @Provider
 * 3. JAX-RS ejecuta filter() AUTOMÁTICAMENTE después de cada request JAX-RS
 * 4. Se ejecuta DESPUÉS de que el endpoint construye Response.ok()
 * 5. Intercepta la respuesta ANTES de enviarla al cliente
 * 6. Agrega headers CORS a la respuesta
 * 
 * 📍 DÓNDE SE EJECUTA:
 * - Se ejecuta para TODOS los endpoints JAX-RS (GET, POST, PUT, DELETE, etc.)
 * - Se ejecuta después del endpoint pero antes de enviar la respuesta
 * - NO se ejecuta para OPTIONS (manejado por CorsRequestFilter)
 * 
 * 🔍 PARA VERLO EN ACCIÓN:
 * - Revisa los logs de WildFly
 * - Busca "🔵 [CORS-RESPONSE-FILTER]" para ver cuándo se ejecuta
 * - Busca "✅ [CORS-RESPONSE-FILTER]" para ver cuándo agrega headers
 */
@Provider
public class CorsResponseFilter implements ContainerResponseFilter {
    
    private static final Logger LOGGER = Logger.getLogger(CorsResponseFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String origin = requestContext.getHeaderString("Origin");
        
        LOGGER.info(String.format("🔵 [CORS-RESPONSE-FILTER] Ejecutándose para %s %s desde origin: %s", 
                method, path, origin));
        
        // No agregar headers CORS para solicitudes OPTIONS (ya manejadas por CorsRequestFilter)
        if ("OPTIONS".equals(method)) {
            LOGGER.info("🔵 [CORS-RESPONSE-FILTER] OPTIONS request detectado - saltando (manejado por CorsRequestFilter)");
            return;
        }
        
        // Verificar si el header ya existe antes de agregarlo (evitar duplicados)
        if (responseContext.getHeaders().containsKey("Access-Control-Allow-Origin")) {
            LOGGER.info("🔵 [CORS-RESPONSE-FILTER] Headers CORS ya existentes - saltando para evitar duplicados");
            return; // Ya fueron agregados por otro filtro
        }
        
        String allowedOrigin = getAllowedOrigin(origin);
        
        // Establecer headers CORS usando putSingle para evitar duplicados
        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", allowedOrigin);
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", 
            "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Profesional-Id, X-Paciente-CI");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", "3600");
        
        LOGGER.info(String.format("✅ [CORS-RESPONSE-FILTER] Headers CORS agregados - Origin: %s -> Allowed: %s para %s %s", 
                origin, allowedOrigin, method, path));
    }

    private String getAllowedOrigin(String origin) {
        if (origin == null) {
            return "http://localhost:3000"; // Default para desarrollo
        }
        
        // Desarrollo local
        if (origin.startsWith("http://localhost:") || 
            origin.startsWith("http://127.0.0.1:") ||
            origin.contains("localhost")) {
            return origin;
        }
        
        // Elastic Cloud (producción)
        if (origin.contains(".web.elasticloud.uy")) {
            LOGGER.fine("Origin de Elastic Cloud permitido: " + origin);
            return origin;
        }
        
        // Vercel (producción - cross-domain completo)
        if (origin.contains(".vercel.app") || origin.contains("vercel.com")) {
            LOGGER.fine("Origin de Vercel permitido: " + origin);
            return origin;
        }
        
        // AWS EC2 (producción - dominios de Amazon AWS)
        if (origin.contains(".amazonaws.com") || 
            origin.contains(".compute.amazonaws.com") ||
            origin.contains("ec2-") ||
            origin.contains("amazonaws")) {
            LOGGER.fine("Origin de AWS EC2 permitido: " + origin);
            return origin;
        }
        
        // Default para desarrollo
        return "http://localhost:3000";
    }
}


package uy.edu.tse.hcen.rest.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Filtro JAX-RS para manejar solicitudes CORS preflight (OPTIONS).
 * 
 * ⚠️ IMPORTANTE: Este filtro NO se llama manualmente. JAX-RS lo ejecuta AUTOMÁTICAMENTE.
 * 
 * 🔄 CÓMO FUNCIONA:
 * 1. Está registrado en RestApplication.getClasses() (línea 17)
 * 2. JAX-RS detecta que implementa ContainerRequestFilter y tiene @Provider
 * 3. La anotación @PreMatching hace que se ejecute ANTES de matchear el endpoint
 * 4. JAX-RS ejecuta filter() AUTOMÁTICAMENTE al inicio de cada request
 * 5. Si es OPTIONS, retorna la respuesta inmediatamente (abortWith)
 * 6. Si no es OPTIONS, continúa con el request normal
 * 
 * 📍 DÓNDE SE EJECUTA:
 * - Se ejecuta para TODOS los requests JAX-RS ANTES de llegar al endpoint
 * - Maneja especialmente las requests OPTIONS (preflight CORS)
 * - Se ejecuta ANTES que cualquier otro filtro o endpoint
 * 
 * 🔍 PARA VERLO EN ACCIÓN:
 * - Revisa los logs de WildFly
 * - Busca "🟢 [CORS-REQUEST-FILTER]" para ver cuándo se ejecuta
 * - Busca "✅ [CORS-REQUEST-FILTER]" para ver cuándo maneja OPTIONS
 */
@Provider
@PreMatching
public class CorsRequestFilter implements ContainerRequestFilter {
    
    private static final Logger LOGGER = Logger.getLogger(CorsRequestFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String origin = requestContext.getHeaderString("Origin");
        
        LOGGER.info(String.format("🟢 [CORS-REQUEST-FILTER] Ejecutándose para %s %s desde origin: %s", 
                method, path, origin));
        
        // Manejar solicitudes OPTIONS (preflight)
        if ("OPTIONS".equals(method)) {
            // Determinar el origen permitido
            String allowedOrigin = getAllowedOrigin(origin);
            
            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder.header("Access-Control-Allow-Origin", allowedOrigin);
            responseBuilder.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            responseBuilder.header("Access-Control-Allow-Headers", 
                "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Profesional-Id, X-Paciente-CI");
            responseBuilder.header("Access-Control-Max-Age", "3600");
            responseBuilder.header("Access-Control-Allow-Credentials", "true");
            
            LOGGER.info(String.format("✅ [CORS-REQUEST-FILTER] OPTIONS preflight manejado - Origin: %s -> Allowed: %s para path: %s", 
                    origin, allowedOrigin, path));
            
            requestContext.abortWith(responseBuilder.build());
        } else {
            LOGGER.info(String.format("🟢 [CORS-REQUEST-FILTER] No es OPTIONS (%s) - continuando con el request", method));
        }
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


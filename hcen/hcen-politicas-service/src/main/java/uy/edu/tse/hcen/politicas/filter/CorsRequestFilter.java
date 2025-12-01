package uy.edu.tse.hcen.politicas.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * Filtro JAX-RS para manejar solicitudes CORS preflight (OPTIONS).
 * Tiene alta prioridad para ejecutarse antes que otros filtros.
 */
@Provider
@PreMatching
public class CorsRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Manejar solicitudes OPTIONS (preflight)
        if ("OPTIONS".equals(requestContext.getMethod())) {
            String origin = requestContext.getHeaderString("Origin");
            
            Response.ResponseBuilder responseBuilder = Response.ok();
            
            // Establecer headers CORS
            if (origin != null && isAllowedOrigin(origin)) {
                responseBuilder.header("Access-Control-Allow-Origin", origin);
                responseBuilder.header("Access-Control-Allow-Credentials", "true");
            } else if (origin != null) {
                // Si hay un origin pero no está permitido, no permitir (sin header)
                // Esto causará que el navegador rechace la petición
                responseBuilder.header("Access-Control-Allow-Origin", "null");
            } else {
                // Sin origin header, probablemente no es una petición del navegador
                responseBuilder.header("Access-Control-Allow-Origin", "*");
            }
            
            responseBuilder.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            responseBuilder.header("Access-Control-Allow-Headers", 
                "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Profesional-Id, X-Paciente-CI, X-Requested-With");
            responseBuilder.header("Access-Control-Max-Age", "3600");
            
            requestContext.abortWith(responseBuilder.build());
        }
    }

    private boolean isAllowedOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        
        // Desarrollo local
        if (origin.startsWith("http://localhost:") || 
            origin.startsWith("http://127.0.0.1:") ||
            origin.contains("localhost")) {
            return true;
        }
        
        // Elastic Cloud (producción)
        if (origin.contains(".web.elasticloud.uy")) {
            return true;
        }
        
        // Vercel (producción)
        if (origin.contains(".vercel.app") || origin.contains("vercel.com")) {
            return true;
        }
        
        // AWS EC2 (producción - dominios de Amazon AWS)
        if (origin.contains(".amazonaws.com") || 
            origin.contains(".compute.amazonaws.com") ||
            origin.contains("ec2-") ||
            origin.contains("amazonaws")) {
            return true;
        }
        
        return false;
    }
}


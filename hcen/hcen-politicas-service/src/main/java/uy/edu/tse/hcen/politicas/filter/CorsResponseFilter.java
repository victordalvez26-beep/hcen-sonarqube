package uy.edu.tse.hcen.politicas.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * Filtro JAX-RS para agregar headers CORS a todas las respuestas.
 */
@Provider
public class CorsResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // No agregar headers CORS para solicitudes OPTIONS (ya manejadas por CorsRequestFilter)
        if ("OPTIONS".equals(requestContext.getMethod())) {
            return;
        }
        
        // Verificar si el header ya existe antes de agregarlo (evitar duplicados)
        if (responseContext.getHeaders().containsKey("Access-Control-Allow-Origin")) {
            return; // Ya fueron agregados por otro filtro
        }
        
        String origin = requestContext.getHeaderString("Origin");
        
        // Si no hay Origin header, probablemente es una petición backend-to-backend
        // No aplicar CORS en ese caso (solo aplicar CORS para peticiones desde navegadores)
        if (origin == null) {
            return;
        }
        
        // Establecer headers CORS usando putSingle para evitar duplicados
        if (isAllowedOrigin(origin)) {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        } else {
            // Si hay un origin pero no está permitido, no agregar headers CORS
            // Esto causará que el navegador rechace la petición por CORS
            return;
        }
        
        // Usar putSingle para evitar duplicados
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", 
            "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Profesional-Id, X-Paciente-CI");
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", "3600");
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


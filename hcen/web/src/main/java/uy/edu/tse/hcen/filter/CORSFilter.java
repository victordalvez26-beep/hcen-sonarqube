package uy.edu.tse.hcen.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

@WebFilter("/*")
public class CORSFilter implements Filter {
    
    private static final Logger LOGGER = Logger.getLogger(CORSFilter.class.getName());
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("CORS Filter inicializado");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Permitir múltiples orígenes (desarrollo y producción)
        String origin = httpRequest.getHeader("Origin");
        String allowedOrigin = null;
        
        if (origin != null) {
            // Desarrollo local
            if (origin.startsWith("http://localhost:") || 
                origin.startsWith("http://127.0.0.1:") ||
                origin.contains("localhost")) {
                allowedOrigin = origin;
            }
            // Elastic Cloud (producción)
            else if (origin.contains(".web.elasticloud.uy")) {
                allowedOrigin = origin;
                LOGGER.fine("Origin de Elastic Cloud permitido: " + origin);
            }
            // Vercel (producción - cross-domain completo)
            else if (origin.contains(".vercel.app") || origin.contains("vercel.com")) {
                allowedOrigin = origin;
                LOGGER.fine("Origin de Vercel permitido: " + origin);
            }
            // AWS EC2 (producción - dominios de Amazon AWS)
            else if (origin.contains(".amazonaws.com") || 
                     origin.contains(".compute.amazonaws.com") ||
                     origin.contains("ec2-") ||
                     origin.contains("amazonaws")) {
                allowedOrigin = origin;
                LOGGER.fine("Origin de AWS EC2 permitido: " + origin);
            }
        }
        
        // Si no se encontró un origen válido, usar localhost por defecto (desarrollo)
        if (allowedOrigin == null) {
            allowedOrigin = "http://localhost:3000";
        }
        
        httpResponse.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        httpResponse.setHeader("Access-Control-Allow-Headers", 
                "Content-Type, Authorization, X-Requested-With, Accept, X-Paciente-CI, X-Profesional-Id");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        LOGGER.info("CORS Filter destruido");
    }
}


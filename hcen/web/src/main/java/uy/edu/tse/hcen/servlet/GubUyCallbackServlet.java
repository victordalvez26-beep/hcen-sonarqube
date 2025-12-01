package uy.edu.tse.hcen.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import uy.edu.tse.hcen.config.GubUyConfig;
import uy.edu.tse.hcen.service.GubUyCallbackService;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

// La configuración del servlet está en web.xml para tener mayor prioridad
// @WebServlet(urlPatterns = {"/", "/callback"})
public class GubUyCallbackServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(GubUyCallbackServlet.class.getName());
    
    @EJB
    private GubUyCallbackService callbackService;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        
        LOGGER.info("=== GubUyCallbackServlet.doGet ===");
        LOGGER.info("Request URI: " + path);
        LOGGER.info("Query String: " + queryString);
        
        if (path != null && (path.startsWith("/api/") || path.startsWith("api/"))) {
            LOGGER.info("Ignorando petición a /api/* - delegando a recursos REST");
            return;
        }
        
 
        
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String error = request.getParameter("error");
        
        LOGGER.info("Code: " + (code != null ? "presente" : "null"));
        LOGGER.info("Error: " + (error != null ? error : "null"));
        
        if ((code == null || code.trim().isEmpty()) && error == null) {
            LOGGER.info("No hay código ni error - mostrando página de bienvenida");
            response.setContentType("text/html");
            response.getWriter().println("<html><body>");
            response.getWriter().println("<h1>Backend HCEN API</h1>");
            response.getWriter().println("<p>El servidor está funcionando correctamente.</p>");
            response.getWriter().println("<p><a href='" + GubUyConfig.FRONTEND_URL + "'>Ir al Frontend</a></p>");
            response.getWriter().println("</body></html>");
            return;
        }
        
        // Delegar el procesamiento al servicio compartido
        try {
            LOGGER.info("Procesando callback con código/error/state");
            String redirectUrl = callbackService.processCallback(code, error, state, response);
            LOGGER.info("Redirect URL: " + redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            // Manejar error específico de menor de edad
            if (e.getMessage() != null && e.getMessage().contains("MENOR_DE_EDAD")) {
                LOGGER.warning("Redirigiendo por error MENOR_DE_EDAD");
                response.sendRedirect(GubUyConfig.FRONTEND_URL + "?error=menor_de_edad");
                return;
            }
            
            LOGGER.severe("Error en callback: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(GubUyConfig.FRONTEND_URL + "?error=" + 
                    URLEncoder.encode("internal_error", StandardCharsets.UTF_8.toString()));
        }
    }
}


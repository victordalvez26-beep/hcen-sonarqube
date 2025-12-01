package uy.edu.tse.hcen.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uy.edu.tse.hcen.config.GubUyConfig;
import uy.edu.tse.hcen.util.CookieUtil;

import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(urlPatterns = "/logout")
public class LogoutCallbackServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(LogoutCallbackServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String state = request.getParameter("state");
        
        LOGGER.info("Callback de logout recibido - State: " + state);
        
        // Eliminar cookie con configuración cross-site (mismos atributos que la original)
        String requestUrl = request.getRequestURL().toString();
        String originHeader = request.getHeader("Origin"); // Detectar si frontend está en localhost
        String deleteCookieHeader = CookieUtil.buildDeleteCookieHeader(requestUrl, originHeader);
        response.setHeader("Set-Cookie", deleteCookieHeader);
        
        LOGGER.info("Logout completado - Cookie eliminada con configuración cross-site");
        
        // Verificar si el logout fue por un error específico (ej: menor de edad)
        if ("error:menor_de_edad".equals(state)) {
            LOGGER.warning("Redirigiendo al frontend con error: menor_de_edad");
            response.sendRedirect(GubUyConfig.FRONTEND_URL + "?error=menor_de_edad");
        } else {
            response.sendRedirect(GubUyConfig.FRONTEND_URL + "?logout=success");
        }
    }
}


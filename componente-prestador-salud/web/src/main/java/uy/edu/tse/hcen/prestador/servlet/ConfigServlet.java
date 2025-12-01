package uy.edu.tse.hcen.prestador.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet para gestionar la configuración (API Key y URL de origen).
 */
public class ConfigServlet extends HttpServlet {
    
    private static final org.jboss.logging.Logger LOGGER = org.jboss.logging.Logger.getLogger(ConfigServlet.class);
    private static final String TEXT_PLAIN = "text/plain";
    
    @Override
    public void init() throws ServletException {
        super.init();
        LOGGER.info("ConfigServlet inicializado correctamente");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.info("ConfigServlet doGet llamado para: " + request.getRequestURI());
        // Mostrar formulario de configuración
        try {
            request.getRequestDispatcher("/config.html").forward(request, response);
        } catch (ServletException e) {
            LOGGER.error("Error al forwardear a /config.html", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.setContentType(TEXT_PLAIN);
                response.getWriter().write("Error interno al procesar la petición");
            } catch (IOException ex) {
                LOGGER.error("No se pudo escribir la respuesta de error", ex);
            }
        } catch (IOException e) {
            LOGGER.error("I/O error al forwardear a /config.html", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.setContentType(TEXT_PLAIN);
                response.getWriter().write("Error de E/S al procesar la petición");
            } catch (IOException ex) {
                LOGGER.error("No se pudo escribir la respuesta de error", ex);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String apiKey = request.getParameter("apiKey");
        String origin = request.getParameter("origin");

        // Guardar en sesión
        request.getSession().setAttribute("apiKey", apiKey);
        request.getSession().setAttribute("origin", origin);

        // Redirigir al inicio
        try {
            response.sendRedirect(request.getContextPath() + "/");
        } catch (IOException e) {
            LOGGER.error("Error al redirigir al inicio", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.setContentType(TEXT_PLAIN);
                response.getWriter().write("Error al redirigir");
            } catch (IOException ex) {
                LOGGER.error("No se pudo escribir la respuesta de error tras fallo en redirect", ex);
            }
        }
    }
}


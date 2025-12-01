package uy.edu.tse.hcen.prestador.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet para gestionar la configuración (API Key y URL de origen).
 */
public class ConfigServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Mostrar formulario de configuración
        request.getRequestDispatcher("/config.html").forward(request, response);
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
        response.sendRedirect(request.getContextPath() + "/");
    }
}


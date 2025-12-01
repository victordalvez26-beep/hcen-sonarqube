package uy.edu.tse.hcen.rest;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.config.GubUyConfig;
import uy.edu.tse.hcen.service.GubUyCallbackService;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Endpoint REST alternativo para callback de Gub.uy.
 * Ruta: /api/auth/login/callback
 * 
 * Usa el mismo servicio que GubUyCallbackServlet para garantizar comportamiento idéntico.
 * URL más descriptiva y profesional para configurar en Gub.uy.
 */
@Path("/auth/login/callback")
public class LoginCallbackResource {
    
    private static final Logger LOGGER = Logger.getLogger(LoginCallbackResource.class.getName());
    
    @Inject
    private GubUyCallbackService callbackService;
    
    /**
     * Callback de Gub.uy - Delega al servicio compartido.
     */
    @GET
    public Response handleCallback(
            @QueryParam("code") String code,
            @QueryParam("error") String error,
            @QueryParam("state") String state,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        
        try {
            // Caso especial: Sin parámetros - Mostrar página de bienvenida
            if (code == null && error == null) {
                String html = "<html><body>" +
                    "<h1>Backend HCEN API</h1>" +
                    "<p>El servidor está funcionando correctamente.</p>" +
                    "<p><a href='" + GubUyConfig.FRONTEND_URL + "'>Ir al Frontend</a></p>" +
                    "</body></html>";
                return Response.ok(html).type("text/html").build();
            }
            
            // Delegar el procesamiento al servicio compartido (idéntico al servlet)
            String redirectUrl = callbackService.processCallback(code, error, state, response);
            return Response.seeOther(URI.create(redirectUrl)).build();
            
        } catch (Exception e) {
            // Manejar error específico de menor de edad
            if (e.getMessage() != null && e.getMessage().contains("MENOR_DE_EDAD")) {
                LOGGER.warning("Redirigiendo por error MENOR_DE_EDAD");
                try {
                    String redirectUrl = GubUyConfig.FRONTEND_URL + "?error=menor_de_edad";
                    return Response.seeOther(URI.create(redirectUrl)).build();
                } catch (Exception ex) {
                    return Response.serverError().entity("Error interno").build();
                }
            }

            LOGGER.severe("Error en callback: " + e.getMessage());
            e.printStackTrace();
            try {
                String redirectUrl = GubUyConfig.FRONTEND_URL + "?error=" + 
                    URLEncoder.encode("internal_error", StandardCharsets.UTF_8.toString());
                return Response.seeOther(URI.create(redirectUrl)).build();
            } catch (Exception ex) {
                return Response.serverError().entity("Error interno: " + e.getMessage()).build();
            }
        }
    }
}

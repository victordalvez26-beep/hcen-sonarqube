package uy.edu.tse.hcen.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.service.EmailService;

import java.util.Map;

/**
 * Endpoint de prueba para verificar el envío de emails.
 */
@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmailTestResource {

    @EJB
    private EmailService emailService;

    @GET
    @Path("/send-email")
    public Response testEmail() {
        System.out.println("=== EMAIL TEST ENDPOINT CALLED ===");
        try {
            System.out.println("Attempting to send test email...");
            boolean sent = emailService.sendTestEmail("matiasfernan67@gmail.com");
            System.out.println("Send result: " + sent);
            
            if (sent) {
                return Response.ok()
                    .entity(Map.of("success", true, "message", "Email enviado correctamente - revisa tu bandeja"))
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("success", false, "message", "Error al enviar email - ver logs de WildFly"))
                    .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "success", false, 
                        "message", "Exception: " + e.getMessage(),
                        "class", e.getClass().getName()
                    ))
                    .build();
        }
    }
}


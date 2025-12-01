package uy.edu.tse.hcen.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import java.util.Map;
import java.util.logging.Logger;
import uy.edu.tse.hcen.utils.PoliticasServiceUrlUtil;

/**
 * Recurso REST para acceder a registros de acceso a historias clínicas.
 * Este recurso actúa como proxy hacia el servicio de políticas para los registros de acceso.
 * Permite acceder desde el contexto /hcen sin necesidad de usar la URL directa del servicio.
 */
@Path("/registros")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrosAccesoResource {
    
    private static final Logger LOG = Logger.getLogger(RegistrosAccesoResource.class.getName());
    
    private Client createClientWithTimeout() {
        return ClientBuilder.newClient();
    }
    
    /**
     * GET /api/documentos/registros/paciente/{ci}
     * 
     * Obtiene los registros de acceso a la historia clínica de un paciente.
     * Proxy hacia el servicio de políticas: GET /api/registros/paciente/{ci}
     */
    @GET
    @Path("/paciente/{ci}")
    public Response listarRegistrosPorPaciente(@PathParam("ci") String ci) {
        Client client = null;
        try {
            String url = PoliticasServiceUrlUtil.buildUrl("/registros/paciente/" + ci);
            LOG.info("Consultando registros de acceso para paciente CI: " + ci + " - URL: " + url);
            
            client = createClientWithTimeout();
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            int status = response.getStatus();
            if (status == Response.Status.OK.getStatusCode()) {
                Object entity = response.readEntity(Object.class);
                client.close();
                LOG.info("Registros obtenidos exitosamente para paciente CI: " + ci);
                return Response.ok(entity).build();
            } else {
                String errorMsg = response.hasEntity() ? response.readEntity(String.class) : "Error desconocido";
                client.close();
                LOG.warning("Error obteniendo registros: HTTP " + status + " - " + errorMsg);
                return Response.status(status).entity(Map.of("error", errorMsg != null ? errorMsg : "Error desconocido")).build();
            }
        } catch (jakarta.ws.rs.ProcessingException e) {
            if (client != null) {
                client.close();
            }
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("failed to respond") || 
                                         errorMessage.contains("Connection refused"))) {
                String politicasUrl = PoliticasServiceUrlUtil.getBaseUrl();
                LOG.severe("El servicio de políticas no está disponible en " + politicasUrl);
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(Map.of("error", "El servicio de políticas no está disponible. Por favor, verifique que el servicio esté corriendo en " + politicasUrl))
                        .build();
            }
            LOG.severe("Error obteniendo registros: " + errorMessage);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener registros: " + errorMessage))
                    .build();
        } catch (Exception e) {
            if (client != null) {
                client.close();
            }
            LOG.severe("Error obteniendo registros: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener registros: " + e.getMessage()))
                    .build();
        }
    }
}



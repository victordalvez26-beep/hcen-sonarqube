package uy.edu.tse.hcen.rest;

import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.dao.PatientSummaryDAO;
import uy.edu.tse.hcen.model.PatientSummary;
import uy.edu.tse.hcen.util.CookieUtil;
import uy.edu.tse.hcen.util.JWTUtil;

import java.util.logging.Logger;

@Path("/patient-summary")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientSummaryResource {
    
    private static final Logger LOGGER = Logger.getLogger(PatientSummaryResource.class.getName());
    
    @EJB
    private PatientSummaryDAO patientSummaryDAO;
    
    /**
     * GET /api/patient-summary
     * 
     * Obtiene el resumen digital del paciente autenticado.
     * El UID se extrae del JWT en la cookie de sesión o header Authorization.
     */
    @GET
    public Response getPatientSummary(@Context HttpServletRequest request) {
        try {
            // Obtener JWT de la cookie o del header Authorization (Bearer)
            String jwtToken = CookieUtil.resolveJwtToken(request);
            
            if (jwtToken == null) {
                LOGGER.warning("No se encontró JWT en la cookie ni en el header Authorization");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"No autenticado\"}")
                        .build();
            }
            
            // Validar JWT y extraer userUid
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                LOGGER.warning("JWT inválido o expirado");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Token inválido o expirado\"}")
                        .build();
            }
            
            LOGGER.info("Obteniendo resumen de paciente para UID: " + userUid);
            
            // Buscar el resumen del paciente
            PatientSummary summary = patientSummaryDAO.findByUid(userUid);
            
            if (summary == null) {
                // Si no existe, devolver un resumen vacío con arrays vacíos
                LOGGER.info("No se encontró resumen para el paciente: " + userUid + ". Devolviendo estructura vacía.");
                String emptySummary = "{\"allergies\":[],\"conditions\":[],\"medications\":[],\"immunizations\":[],\"observations\":[],\"procedures\":[]}";
                return Response.ok(emptySummary).build();
            }
            
            // Construir JSON de respuesta
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");
            
            // Allergies
            jsonResponse.append("\"allergies\":");
            jsonResponse.append(summary.getAllergies() != null && !summary.getAllergies().trim().isEmpty() 
                    ? summary.getAllergies() : "[]");
            jsonResponse.append(",");
            
            // Conditions
            jsonResponse.append("\"conditions\":");
            jsonResponse.append(summary.getConditions() != null && !summary.getConditions().trim().isEmpty() 
                    ? summary.getConditions() : "[]");
            jsonResponse.append(",");
            
            // Medications
            jsonResponse.append("\"medications\":");
            jsonResponse.append(summary.getMedications() != null && !summary.getMedications().trim().isEmpty() 
                    ? summary.getMedications() : "[]");
            jsonResponse.append(",");
            
            // Immunizations
            jsonResponse.append("\"immunizations\":");
            jsonResponse.append(summary.getImmunizations() != null && !summary.getImmunizations().trim().isEmpty() 
                    ? summary.getImmunizations() : "[]");
            jsonResponse.append(",");
            
            // Observations
            jsonResponse.append("\"observations\":");
            jsonResponse.append(summary.getObservations() != null && !summary.getObservations().trim().isEmpty() 
                    ? summary.getObservations() : "[]");
            jsonResponse.append(",");
            
            // Procedures
            jsonResponse.append("\"procedures\":");
            jsonResponse.append(summary.getProcedures() != null && !summary.getProcedures().trim().isEmpty() 
                    ? summary.getProcedures() : "[]");
            
            jsonResponse.append("}");
            
            LOGGER.info("Resumen de paciente obtenido exitosamente para UID: " + userUid);
            return Response.ok(jsonResponse.toString()).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo resumen de paciente: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error interno del servidor\"}")
                    .build();
        }
    }
}


package uy.edu.tse.hcen.rest;

import jakarta.ejb.EJB;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.service.ReportesService;
import uy.edu.tse.hcen.util.JWTUtil;
import uy.edu.tse.hcen.utils.PoliticasServiceUrlUtil;

/**
 * Recurso REST para reportes agregados orientados a administradores del HCEN.
 */
@Path("/reportes")
@Produces(MediaType.APPLICATION_JSON)
public class ReportesResource {

    private static final Logger LOGGER = Logger.getLogger(ReportesResource.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @EJB
    private ReportesService reportesService;

    @EJB
    private UserDAO userDAO;
    
    // Constructor para logging de inicialización
    public ReportesResource() {
        LOGGER.info("ReportesResource inicializado");
    }
    
    @GET
    @Path("/test")
    public Response test() {
        LOGGER.info("GET /reportes/test - Endpoint de prueba accedido");
        return Response.ok(Map.of("status", "ok", "message", "ReportesResource está funcionando")).build();
    }

    @GET
    @Path("/documentos/evolucion")
    public Response obtenerEvolucionDocumentos(
            @Context HttpServletRequest request,
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin) {
        Response security = ensureAdmin(request);
        if (security != null) {
            return security;
        }
        try {
            LocalDate inicio = parseDate(fechaInicio);
            LocalDate fin = parseDate(fechaFin);
            List<Map<String, Object>> resultado = reportesService.obtenerEvolucionDocumentos(inicio, fin);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo evolución de documentos: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener evolución de documentos: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/documentos/especialidad")
    public Response obtenerDocumentosPorEspecialidad(@Context HttpServletRequest request) {
        Response security = ensureAdmin(request);
        if (security != null) {
            return security;
        }
        try {
            List<Map<String, Object>> resultado = reportesService.obtenerDocumentosPorEspecialidad();
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo documentos por especialidad: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener documentos por especialidad"))
                    .build();
        }
    }

    @GET
    @Path("/documentos/formato")
    public Response obtenerDocumentosPorFormato(@Context HttpServletRequest request) {
        Response security = ensureAdmin(request);
        if (security != null) {
            return security;
        }
        try {
            List<Map<String, Object>> resultado = reportesService.obtenerDocumentosPorFormato();
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo documentos por formato: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener documentos por formato"))
                    .build();
        }
    }

    @GET
    @Path("/documentos/tenant")
    public Response obtenerDocumentosPorTenant(@Context HttpServletRequest request) {
        Response seguridad = ensureAdmin(request);
        if (seguridad != null) {
            return seguridad;
        }
        try {
            List<Map<String, Object>> resultado = reportesService.obtenerDocumentosPorTenant();
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo documentos por tenant: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener documentos por tenant"))
                    .build();
        }
    }

    @GET
    @Path("/documentos/resumen")
    public Response obtenerResumenDocumentos(@Context HttpServletRequest request) {
        LOGGER.info("GET /reportes/documentos/resumen - Iniciando");
        Response seguridad = ensureAdmin(request);
        if (seguridad != null) {
            LOGGER.warning("GET /reportes/documentos/resumen - Acceso denegado");
            return seguridad;
        }
        try {
            if (reportesService == null) {
                LOGGER.severe("ReportesService es null - problema de inyección");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(Map.of("error", "Servicio no disponible"))
                        .build();
            }
            Map<String, Object> resultado = reportesService.obtenerResumenDocumentos();
            LOGGER.info("GET /reportes/documentos/resumen - Éxito");
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo resumen de documentos: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener resumen de documentos: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/usuarios/resumen")
    public Response obtenerResumenUsuarios(@Context HttpServletRequest request) {
        Response seguridad = ensureAdmin(request);
        if (seguridad != null) {
            return seguridad;
        }
        try {
            Map<String, Object> resultado = reportesService.obtenerResumenUsuarios();
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo resumen de usuarios: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener resumen de usuarios"))
                    .build();
        }
    }

    @GET
    @Path("/usuarios/profesionales")
    public Response obtenerProfesionales(@Context HttpServletRequest request) {
        Response seguridad = ensureAdmin(request);
        if (seguridad != null) {
            return seguridad;
        }
        try {
            List<Map<String, Object>> resultado = reportesService.obtenerProfesionalesDetalle();
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo profesionales: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener profesionales"))
                    .build();
        }
    }

    private Response ensureAdmin(HttpServletRequest request) {
        try {
            String jwtToken = extractJwtFromCookie(request);
            if (jwtToken == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "No autenticado"))
                        .build();
            }

            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Token inválido"))
                        .build();
            }

            User user = userDAO.findByUid(userUid);
            if (user == null || user.getRol() == null || !"AD".equals(user.getRol().getCodigo())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Acceso restringido a administradores"))
                        .build();
            }
            return null;
        } catch (Exception e) {
            LOGGER.severe("Error validando sesión del administrador: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error validando permisos"))
                    .build();
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("hcen_session".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // ========== PROXY PARA REPORTES DE POLÍTICAS ==========
    // Estos endpoints actúan como proxy hacia el servicio de políticas
    
    private Client createClient() {
        return ClientBuilder.newClient();
    }

    @GET
    @Path("/politicas/resumen")
    public Response obtenerResumenPoliticas(@Context HttpServletRequest request) {
        Response security = ensureAdmin(request);
        if (security != null) {
            return security;
        }
        Client client = null;
        try {
            String url = PoliticasServiceUrlUtil.buildUrl("/reportes/politicas/resumen");
            client = createClient();
            jakarta.ws.rs.core.Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            int status = response.getStatus();
            Object entity = response.readEntity(Object.class);
            client.close();
            
            if (status == Response.Status.OK.getStatusCode()) {
                return Response.ok(entity).build();
            } else {
                LOGGER.warning("Error obteniendo resumen de políticas: HTTP " + status);
                return Response.status(status).entity(entity).build();
            }
        } catch (Exception e) {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ignored) {}
            }
            LOGGER.severe("Error obteniendo resumen de políticas: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener resumen de políticas: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/politicas/alcance")
    public Response obtenerPoliticasPorAlcance(@Context HttpServletRequest request) {
        Response security = ensureAdmin(request);
        if (security != null) {
            return security;
        }
        Client client = null;
        try {
            String url = PoliticasServiceUrlUtil.buildUrl("/reportes/politicas/alcance");
            client = createClient();
            jakarta.ws.rs.core.Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            int status = response.getStatus();
            Object entity = response.readEntity(Object.class);
            client.close();
            
            if (status == Response.Status.OK.getStatusCode()) {
                return Response.ok(entity).build();
            } else {
                return Response.status(status).entity(entity).build();
            }
        } catch (Exception e) {
            if (client != null) {
                client.close();
            }
            LOGGER.warning("Error obteniendo políticas por alcance: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("error", "El servicio de políticas no está disponible"))
                    .build();
        }
    }

    @GET
    @Path("/politicas/duracion")
    public Response obtenerPoliticasPorDuracion(@Context HttpServletRequest request) {
        Response security = ensureAdmin(request);
        if (security != null) {
            return security;
        }
        Client client = null;
        try {
            String url = PoliticasServiceUrlUtil.buildUrl("/reportes/politicas/duracion");
            client = createClient();
            jakarta.ws.rs.core.Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            int status = response.getStatus();
            Object entity = response.readEntity(Object.class);
            client.close();
            
            if (status == Response.Status.OK.getStatusCode()) {
                return Response.ok(entity).build();
            } else {
                return Response.status(status).entity(entity).build();
            }
        } catch (Exception e) {
            if (client != null) {
                client.close();
            }
            LOGGER.warning("Error obteniendo políticas por duración: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("error", "El servicio de políticas no está disponible"))
                    .build();
        }
    }

    @GET
    @Path("/politicas/evolucion")
    public Response obtenerEvolucionPoliticas(
            @Context HttpServletRequest request,
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin) {
        Response security = ensureAdmin(request);
        if (security != null) {
            return security;
        }
        Client client = null;
        try {
            StringBuilder urlBuilder = new StringBuilder(PoliticasServiceUrlUtil.buildUrl("/reportes/politicas/evolucion"));
            if (fechaInicio != null || fechaFin != null) {
                urlBuilder.append("?");
                if (fechaInicio != null) {
                    urlBuilder.append("fechaInicio=").append(java.net.URLEncoder.encode(fechaInicio, StandardCharsets.UTF_8));
                }
                if (fechaFin != null) {
                    if (fechaInicio != null) {
                        urlBuilder.append("&");
                    }
                    urlBuilder.append("fechaFin=").append(java.net.URLEncoder.encode(fechaFin, StandardCharsets.UTF_8));
                }
            }
            
            String url = urlBuilder.toString();
            client = createClient();
            jakarta.ws.rs.core.Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            int status = response.getStatus();
            Object entity = response.readEntity(Object.class);
            client.close();
            
            if (status == Response.Status.OK.getStatusCode()) {
                return Response.ok(entity).build();
            } else {
                return Response.status(status).entity(entity).build();
            }
        } catch (Exception e) {
            if (client != null) {
                client.close();
            }
            LOGGER.warning("Error obteniendo evolución de políticas: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("error", "El servicio de políticas no está disponible"))
                    .build();
        }
    }
}




package uy.edu.tse.hcen.rest;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.config.GubUyConfig;
import uy.edu.tse.hcen.dao.UserSessionDAO;
import uy.edu.tse.hcen.model.UserSession;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.service.AuthService;
import uy.edu.tse.hcen.util.JWTUtil;
import uy.edu.tse.hcen.util.CookieUtil;

import java.net.URI;
import java.net.URLEncoder;
import java.util.logging.Logger;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    
    private static final Logger LOGGER = Logger.getLogger(AuthResource.class.getName());
    
    @EJB
    private AuthService authService;
    
    @Inject
    private UserSessionDAO userSessionDAO;
    
    @EJB
    private uy.edu.tse.hcen.service.AuthTokenService authTokenService;
    
    // Verificar sesión activa
    // GET /api/auth/session
    @GET
    @Path("/session")
    public Response checkSession(@Context HttpServletRequest request) {
        try {
            String jwtToken = extractJwtFromCookie(request);
            
            if (jwtToken == null) {
                return addCorsHeaders(Response.ok("{\"authenticated\":false}"), request).build();
            }
            
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                LOGGER.info("JWT inválido o expirado");
                return addCorsHeaders(Response.ok("{\"authenticated\":false}"), request).build();
            }
            
            UserSession session = userSessionDAO.findByJwtToken(jwtToken);
            if (session == null || session.isExpired()) {
                LOGGER.info("Sesión no encontrada o expirada");
                return addCorsHeaders(Response.ok("{\"authenticated\":false}"), request).build();
            }
            
            User user = authService.getUserInfo(session.getAccessToken());
            
            if (user == null) {
                LOGGER.warning("No se pudo obtener info del usuario");
                return addCorsHeaders(Response.ok("{\"authenticated\":false}"), request).build();
            }
            
            LOGGER.info("DEBUG - Usuario codDocum: '" + user.getCodDocum() + "'");
            
            String nombreCompleto = buildFullName(user.getPrimerNombre(), user.getSegundoNombre(), user.getPrimerApellido(), user.getSegundoApellido());
            
            String jsonResponse = String.format(
                "{\"authenticated\":true,\"uid\":\"%s\",\"rol\":\"%s\",\"profileCompleted\":%b}",
                user.getUid(),
                user.getRol() != null ? user.getRol().getCodigo() : "",
                user.isProfileCompleted()
            );
            
            LOGGER.info("Sesión válida para usuario: " + user.getUid());
            return addCorsHeaders(Response.ok(jsonResponse), request).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error verificando sesión: " + e.getMessage());
            e.printStackTrace();
            return addCorsHeaders(Response.ok("{\"authenticated\":false}"), request).build();
        }
    }
    
    // Cerrar sesión
    // GET /api/auth/logout
    @GET
    @Path("/logout_hcen")
    public Response logoutHcen(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            String jwtToken = extractJwtFromCookie(request);
            String idToken = null;
            
            if (jwtToken != null) {
                UserSession session = userSessionDAO.findByJwtToken(jwtToken);
                
                if (session != null) {
                    idToken = session.getIdToken();
                    boolean deleted = userSessionDAO.deleteByJwtToken(jwtToken);
                    if (deleted) {
                        LOGGER.info("Sesión eliminada de la base de datos");
                    }
                }
            }
            
            // Eliminar cookie con configuración cross-site (mismos atributos que la original)
            String requestUrl = request.getRequestURL().toString();
            String originHeader = request.getHeader("Origin"); // Detectar si frontend está en localhost
            String deleteCookieHeader = CookieUtil.buildDeleteCookieHeader(requestUrl, originHeader);
            response.setHeader("Set-Cookie", deleteCookieHeader);
            
            LOGGER.info("Cookie eliminada con configuración cross-site");
            
            if (idToken != null && !idToken.isEmpty()) {
                LOGGER.info("Redirigiendo a gub.uy para logout");
                
                String state = java.util.UUID.randomUUID().toString();
                String logoutUrl = GubUyConfig.LOGOUT_ENDPOINT +
                        "?id_token_hint=" + URLEncoder.encode(idToken, "UTF-8") +
                        "&post_logout_redirect_uri=" + URLEncoder.encode(GubUyConfig.POST_LOGOUT_REDIRECT_URI, "UTF-8") +
                        "&state=" + state;
                
                return Response.seeOther(URI.create(logoutUrl)).build();
            } else {
                LOGGER.info("Logout local");
                return Response.seeOther(URI.create(GubUyConfig.FRONTEND_URL + "?logout=success")).build();
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error cerrando sesión: " + e.getMessage());
            e.printStackTrace();
            return Response.serverError()
                    .entity("{\"error\":\"Error cerrando sesión\"}")
                    .build();
        }
    }
    
    @GET
    @Path("/logout")
    public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            LOGGER.info("Logout local");
            return Response.seeOther(URI.create(GubUyConfig.FRONTEND_URL + "?logout=success")).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error cerrando sesión: " + e.getMessage());
            e.printStackTrace();
            return Response.serverError()
                    .entity("{\"error\":\"Error cerrando sesión\"}")
                    .build();
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
    
    private String buildFullName(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido) {
        StringBuilder nombre = new StringBuilder();
        
        if (primerNombre != null && !primerNombre.trim().isEmpty()) {
            nombre.append(primerNombre.trim());
        }
        
        if (segundoNombre != null && !segundoNombre.trim().isEmpty()) {
            if (nombre.length() > 0) nombre.append(" ");
            nombre.append(segundoNombre.trim());
        }
        
        if (primerApellido != null && !primerApellido.trim().isEmpty()) {
            if (nombre.length() > 0) nombre.append(" ");
            nombre.append(primerApellido.trim());
        }
        
        if (segundoApellido != null && !segundoApellido.trim().isEmpty()) {
            if (nombre.length() > 0) nombre.append(" ");
            nombre.append(segundoApellido.trim());
        }
        
        return nombre.length() > 0 ? nombre.toString() : "Usuario";
    }
    
    /**
     * Agrega headers CORS explícitos para cross-site entre Vercel y Elastic Cloud.
     * Este método asegura que el endpoint /api/auth/session tenga headers CORS específicos
     * como respaldo adicional a los filtros globales.
     */
    private Response.ResponseBuilder addCorsHeaders(Response.ResponseBuilder responseBuilder, HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String allowedOrigin = getAllowedOriginForCors(origin);
        
        LOGGER.info(String.format("🔵 [AUTH-RESOURCE] Agregando headers CORS explícitos - Origin: %s -> Allowed: %s", 
                origin, allowedOrigin));
        
        // Headers CORS esenciales para cross-site entre Vercel y Elastic Cloud
        responseBuilder.header("Access-Control-Allow-Origin", allowedOrigin);
        responseBuilder.header("Access-Control-Allow-Credentials", "true");
        responseBuilder.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        responseBuilder.header("Access-Control-Allow-Headers", 
            "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Profesional-Id, X-Paciente-CI");
        responseBuilder.header("Access-Control-Max-Age", "3600");
        responseBuilder.header("Access-Control-Expose-Headers", "Content-Type, Authorization");
        
        return responseBuilder;
    }
    
    /**
     * Determina el origen permitido para CORS basado en el Origin del request.
     * Permite localhost, Vercel, Elastic Cloud y AWS EC2.
     */
    private String getAllowedOriginForCors(String origin) {
        if (origin == null) {
            return "http://localhost:3000"; // Default para desarrollo
        }
        
        // Desarrollo local
        if (origin.startsWith("http://localhost:") || 
            origin.startsWith("http://127.0.0.1:") ||
            origin.contains("localhost")) {
            return origin;
        }
        
        // Elastic Cloud (producción)
        if (origin.contains(".web.elasticloud.uy")) {
            LOGGER.fine("Origin de Elastic Cloud permitido: " + origin);
            return origin;
        }
        
        // Vercel (producción - cross-domain completo)
        if (origin.contains(".vercel.app") || origin.contains("vercel.com")) {
            LOGGER.fine("Origin de Vercel permitido: " + origin);
            return origin;
        }
        
        // AWS EC2 (producción - dominios de Amazon AWS)
        if (origin.contains(".amazonaws.com") || 
            origin.contains(".compute.amazonaws.com") ||
            origin.contains("ec2-") ||
            origin.contains("amazonaws")) {
            LOGGER.fine("Origin de AWS EC2 permitido: " + origin);
            return origin;
        }
        
        // Default para desarrollo
        return "http://localhost:3000";
    }
    
    /**
     * Intercambia un token temporal por el JWT real.
     * POST /api/auth/exchange-token
     * Body: { "token": "abc123..." }
     * Response: { "jwt": "JWT_REAL", "expires": 86400 }
     */
    @POST
    @Path("/exchange-token")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exchangeToken(@Context HttpServletRequest request, 
                                  @Context HttpServletResponse response,
                                  java.util.Map<String, String> body) {
        try {
            String tempToken = body != null ? body.get("token") : null;
            
            if (tempToken == null || tempToken.trim().isEmpty()) {
                LOGGER.warning("Token temporal no proporcionado");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Token temporal requerido\"}")
                        .build();
            }
            
            LOGGER.info("Intercambiando token temporal...");
            String jwtToken = authTokenService.exchangeToken(tempToken);
            
            if (jwtToken == null) {
                LOGGER.warning("Token temporal inválido o expirado");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Token temporal inválido o expirado\"}")
                        .build();
            }
            
            // Establecer cookie JWT con configuración cross-site para Elastic Cloud
            long expirationSeconds = 24 * 60 * 60; // 24 horas
            String requestUrl = request.getRequestURL().toString();
            String originHeader = request.getHeader("Origin"); // Detectar si frontend está en localhost
            String cookieHeader = CookieUtil.buildSessionCookieHeader(jwtToken, expirationSeconds, requestUrl, originHeader);
            response.setHeader("Set-Cookie", cookieHeader);
            
            LOGGER.info("Token intercambiado exitosamente - Cookie configurada para cross-site");
            
            // Retornar JWT en el body de la respuesta
            String jsonResponse = String.format(
                "{\"jwt\":\"%s\",\"expires\":%d}",
                escapeJson(jwtToken),
                expirationSeconds
            );
            
            return Response.ok(jsonResponse).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error intercambiando token: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error interno del servidor\"}")
                    .build();
        }
    }
    
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}

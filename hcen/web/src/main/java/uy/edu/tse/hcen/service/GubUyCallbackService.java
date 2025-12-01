package uy.edu.tse.hcen.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.servlet.http.HttpServletResponse;
import uy.edu.tse.hcen.config.GubUyConfig;
import uy.edu.tse.hcen.dao.UserSessionDAO;
import uy.edu.tse.hcen.dto.TokenResponse;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.model.UserSession;
import uy.edu.tse.hcen.util.JWTUtil;
import uy.edu.tse.hcen.util.CookieUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Servicio compartido para procesar el callback de Gub.uy.
 * Usado tanto por GubUyCallbackServlet como por LoginCallbackResource.
 */
@Stateless
public class GubUyCallbackService {
    
    private static final Logger LOGGER = Logger.getLogger(GubUyCallbackService.class.getName());
    
    @EJB
    private uy.edu.tse.hcen.service.AuthService authService;
    
    @EJB
    private UserSessionDAO userSessionDAO;
    
    @EJB
    private uy.edu.tse.hcen.service.AuthTokenService authTokenService;
    
    /**
     * Maneja el logout forzado para usuarios menores de edad.
     */
    private String handleMinorUserLogout(TokenResponse tokenResponse, String state) throws java.io.UnsupportedEncodingException {
        LOGGER.warning("Usuario menor de edad detectado. Iniciando logout forzado en Gub.uy");
        String idToken = tokenResponse.getId_token();
        if (idToken != null && !idToken.isEmpty()) {
            // Usar la URI de redirección registrada en Gub.uy para evitar rechazo
            String postLogoutRedirectUri = GubUyConfig.POST_LOGOUT_REDIRECT_URI;
            
            // Pasar el error a través del parámetro 'state'
            String logoutState = "error:menor_de_edad";
            
            String logoutUrl = GubUyConfig.LOGOUT_ENDPOINT +
                    "?id_token_hint=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8.toString()) +
                    "&post_logout_redirect_uri=" + URLEncoder.encode(postLogoutRedirectUri, StandardCharsets.UTF_8.toString()) +
                    "&state=" + URLEncoder.encode(logoutState, StandardCharsets.UTF_8.toString());
            
            LOGGER.info("Redirigiendo a Gub.uy para logout: " + logoutUrl);
            return logoutUrl;
        }
        return null;
    }

    /**
     * Procesa el callback de Gub.uy y retorna la URL de redirect.
     * 
     * @param code Código de autorización
     * @param error Error (si hubo)
     * @param state Estado del flujo OAuth (opcional, para reenviarlo al frontend)
     * @param response HttpServletResponse para setear cookies
     * @return URL de redirect al frontend
     */
    public String processCallback(String code, String error, String state, HttpServletResponse response) throws Exception {
        
        // Caso 1: Error desde Gub.uy
        if (error != null) {
            LOGGER.severe("Error en autenticación: " + error);
            return GubUyConfig.FRONTEND_URL + "?error=" + 
                URLEncoder.encode(error, StandardCharsets.UTF_8.toString());
        }
        
        // Caso 2: Código de autorización faltante
        if (code == null || code.isEmpty()) {
            LOGGER.severe("Código de autorización no encontrado");
            return GubUyConfig.FRONTEND_URL + "?error=" + 
                URLEncoder.encode("missing_code", StandardCharsets.UTF_8.toString());
        }
        
        // Log del código (solo primeros caracteres para seguridad)
        String codePreview = code.length() >= 10 ? code.substring(0, 10) + "..." : code + "...";
        LOGGER.info("Procesando callback con código: " + codePreview);
        
        // Intercambiar código por tokens
        TokenResponse tokenResponse = authService.exchangeCodeForTokens(code);
        if (tokenResponse == null || tokenResponse.getAccess_token() == null) {
            LOGGER.severe("Error intercambiando código por token");
            return GubUyConfig.FRONTEND_URL + "?error=" + 
                URLEncoder.encode("token_exchange_failed", StandardCharsets.UTF_8.toString());
        }
        
        LOGGER.info("Token obtenido exitosamente");
        
        // Obtener información del usuario
        User user = null;
        try {
            user = authService.getUserInfo(tokenResponse.getAccess_token());
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("MENOR_DE_EDAD")) {
                String logoutUrl = handleMinorUserLogout(tokenResponse, state);
                if (logoutUrl != null) {
                    return logoutUrl;
                }
                // Si no hay URL de logout (ej: sin id_token), re-lanzamos
                LOGGER.warning("No se encontró id_token para logout forzado.");
            }
            throw e; // Re-lanzar excepciones
        }

        if (user == null || user.getUid() == null) {
            LOGGER.severe("No se pudo obtener la información del usuario");
            return GubUyConfig.FRONTEND_URL + "?error=" + 
                URLEncoder.encode("userinfo_failed", StandardCharsets.UTF_8.toString());
        }
        
        LOGGER.info("Información del usuario obtenida: " + user.getPrimerNombre() + " " + user.getPrimerApellido());
        
        // Generar JWT
        long expirationSeconds = 24 * 60 * 60;
        String jwtToken = JWTUtil.generateJWT(user.getUid(), expirationSeconds);
        LOGGER.info("JWT generado para usuario: " + user.getUid());
        
        // Guardar sesión
        UserSession userSession = new UserSession(
                user.getUid(),
                jwtToken,
                tokenResponse.getAccess_token(),
                tokenResponse.getRefresh_token() != null ? tokenResponse.getRefresh_token() : "",
                LocalDateTime.now().plusSeconds(expirationSeconds)
        );
        userSession.setIdToken(tokenResponse.getId_token());
        userSessionDAO.save(userSession);
        LOGGER.info("Sesión guardada en base de datos");
        
        // Setear cookie JWT con configuración cross-site para Elastic Cloud
        // Usa SameSite=None + Secure para permitir cross-domain cookies
        // Pasar FRONTEND_URL como originHeader para detectar si es localhost
        String originHeader = GubUyConfig.FRONTEND_URL != null && 
                              (GubUyConfig.FRONTEND_URL.startsWith("http://localhost") || 
                               GubUyConfig.FRONTEND_URL.startsWith("http://127.0.0.1")) 
                              ? GubUyConfig.FRONTEND_URL : null;
        String cookieHeader = CookieUtil.buildSessionCookieHeader(jwtToken, expirationSeconds, GubUyConfig.FRONTEND_URL, originHeader);
        response.setHeader("Set-Cookie", cookieHeader);
        
        LOGGER.info("Cookie JWT seteada con configuración cross-site");
        
        // Generar token temporal de un solo uso para pasar al frontend
        // El frontend lo intercambiará por el JWT real
        String tempToken = null;
        try {
            if (authTokenService == null) {
                LOGGER.severe("AuthTokenService es NULL - no se inyectó correctamente");
                // Fallback: usar JWT directamente (menos seguro pero funcional)
                tempToken = jwtToken;
            } else {
                tempToken = authTokenService.generateTempToken(jwtToken, user.getUid());
                LOGGER.info("Token temporal generado para intercambio seguro");
            }
        } catch (Exception e) {
            LOGGER.severe("Error generando token temporal: " + e.getMessage());
            e.printStackTrace();
            // Fallback: usar JWT directamente (menos seguro pero funcional)
            tempToken = jwtToken;
            LOGGER.warning("Usando JWT directamente como fallback");
        }
        
        // Retornar URL de redirect con token temporal en query param
        // El frontend intercambiará este token por el JWT real
        // Esto es más seguro que pasar el JWT directamente
        LOGGER.info("Autenticación exitosa");
        StringBuilder redirectUrl = new StringBuilder();
        redirectUrl.append(GubUyConfig.FRONTEND_URL);
        redirectUrl.append("?login=success");
        redirectUrl.append("&token=").append(URLEncoder.encode(tempToken, StandardCharsets.UTF_8.toString()));
        
        // Incluir state si está presente
        if (state != null && !state.trim().isEmpty()) {
            redirectUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8.toString()));
            LOGGER.info("State incluido en redirect: " + state);
        }
        
        return redirectUrl.toString();
    }
}


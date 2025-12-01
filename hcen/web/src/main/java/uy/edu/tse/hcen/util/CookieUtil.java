package uy.edu.tse.hcen.util;

import uy.edu.tse.hcen.config.GubUyConfig;

import java.util.logging.Logger;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilidad para construir headers Set-Cookie con configuración cross-site para Elastic Cloud.
 * Soporta cookies cross-domain entre diferentes subdominios de Elastic Cloud.
 */
public class CookieUtil {
    
    private static final Logger LOGGER = Logger.getLogger(CookieUtil.class.getName());
    
    /**
     * Construye el header Set-Cookie para una cookie JWT de sesión con configuración cross-site.
     * 
     * @param jwtToken El token JWT a almacenar
     * @param maxAgeSeconds Tiempo de expiración en segundos
     * @param requestUrl URL del request (para detectar HTTPS)
     * @param originHeader Header Origin del request (para detectar si el frontend está en localhost)
     * @return String con el header Set-Cookie completo
     */
    public static String buildSessionCookieHeader(String jwtToken, long maxAgeSeconds, String requestUrl, String originHeader) {
        StringBuilder cookie = new StringBuilder();
        cookie.append("hcen_session=").append(jwtToken);
        cookie.append("; Path=/");
        cookie.append("; Max-Age=").append(maxAgeSeconds);
        cookie.append("; HttpOnly=true");
        
        // Detectar si el frontend está en localhost (HTTP)
        boolean isFrontendLocalhost = originHeader != null && 
                                      (originHeader.startsWith("http://localhost:") || 
                                       originHeader.startsWith("http://127.0.0.1:"));
        
        // Para cross-site cookies, necesitamos SameSite=None y Secure
        // Secure es requerido cuando SameSite=None (solo funciona con HTTPS)
        // PERO: si el frontend está en localhost (HTTP), no podemos usar Secure
        boolean isHttps = requestUrl != null && requestUrl.toLowerCase().startsWith("https://");
        String frontendUrl = GubUyConfig.FRONTEND_URL != null ? GubUyConfig.FRONTEND_URL.toLowerCase() : "";
        
        // Detectar si estamos en Elastic Cloud
        boolean isElasticCloud = frontendUrl.contains(".web.elasticloud.uy") || 
                                  (requestUrl != null && requestUrl.contains(".web.elasticloud.uy"));
        
        // Detectar si el frontend está en vercel.com (cross-domain completo)
        boolean isFrontendVercel = originHeader != null && 
                                   (originHeader.contains(".vercel.app") || originHeader.contains("vercel.com"));
        
        // Si requestUrl es null, usar FRONTEND_URL para detectar si es HTTPS
        if (!isHttps && requestUrl == null && frontendUrl.startsWith("https://")) {
            isHttps = true;
        }
        
        // Si el frontend está en localhost (HTTP), no usar Secure aunque el backend esté en HTTPS
        // Esto permite desarrollo mixto: backend en servidor (HTTPS) + frontend en localhost (HTTP)
        boolean useSecure = isHttps && !isFrontendLocalhost;
        
        if (isElasticCloud || (isHttps && !isFrontendLocalhost) || isFrontendVercel) {
            // Cross-site: SameSite=None + Secure (solo si frontend también está en HTTPS)
            cookie.append("; SameSite=None");
            if (useSecure || isFrontendVercel) {
                cookie.append("; Secure");
            }
            
            // Solo agregar Domain si es Elastic Cloud (mismo dominio raíz)
            // Para vercel.com ↔ elastic.uy (dominios diferentes), NO usar Domain
            if (isElasticCloud && !isFrontendVercel) {
                cookie.append("; Domain=.web.elasticloud.uy");
            }
            
            LOGGER.info(String.format("Cookie cross-site configurada - SameSite=None, Secure=%s, Domain=%s, FrontendLocalhost=%s, FrontendVercel=%s", 
                    useSecure || isFrontendVercel, isElasticCloud && !isFrontendVercel ? ".web.elasticloud.uy" : "none", isFrontendLocalhost, isFrontendVercel));
        } else {
            // Localhost: SameSite=Lax (funciona bien en desarrollo)
            cookie.append("; SameSite=Lax");
            LOGGER.info("Cookie local configurada - SameSite=Lax");
        }
        
        return cookie.toString();
    }
    
    /**
     * Construye el header Set-Cookie para una cookie JWT de sesión con configuración cross-site.
     * Versión sobrecargada sin originHeader (backward compatibility).
     * 
     * @param jwtToken El token JWT a almacenar
     * @param maxAgeSeconds Tiempo de expiración en segundos
     * @param requestUrl URL del request (para detectar HTTPS)
     * @return String con el header Set-Cookie completo
     */
    public static String buildSessionCookieHeader(String jwtToken, long maxAgeSeconds, String requestUrl) {
        return buildSessionCookieHeader(jwtToken, maxAgeSeconds, requestUrl, null);
    }
    
    /**
     * Construye el header Set-Cookie para eliminar una cookie (logout).
     * Incluye los mismos atributos que la cookie original para asegurar eliminación cross-site.
     * 
     * @param requestUrl URL del request (para detectar HTTPS)
     * @param originHeader Header Origin del request (para detectar si el frontend está en localhost)
     * @return String con el header Set-Cookie para eliminar la cookie
     */
    public static String buildDeleteCookieHeader(String requestUrl, String originHeader) {
        StringBuilder cookie = new StringBuilder();
        cookie.append("hcen_session="); // Valor vacío
        cookie.append("; Path=/");
        cookie.append("; Max-Age=0"); // Expira inmediatamente
        cookie.append("; HttpOnly=true");
        
        // Detectar si el frontend está en localhost (HTTP)
        boolean isFrontendLocalhost = originHeader != null && 
                                      (originHeader.startsWith("http://localhost:") || 
                                       originHeader.startsWith("http://127.0.0.1:"));
        
        // Detectar si el frontend está en vercel.com (cross-domain completo)
        boolean isFrontendVercel = originHeader != null && 
                                   (originHeader.contains(".vercel.app") || originHeader.contains("vercel.com"));
        
        // Mismos atributos que la cookie original para asegurar eliminación
        boolean isHttps = requestUrl != null && requestUrl.toLowerCase().startsWith("https://");
        String frontendUrl = GubUyConfig.FRONTEND_URL != null ? GubUyConfig.FRONTEND_URL.toLowerCase() : "";
        boolean isElasticCloud = frontendUrl.contains(".web.elasticloud.uy") || 
                                  (requestUrl != null && requestUrl.contains(".web.elasticloud.uy"));
        
        // Si el frontend está en localhost (HTTP), no usar Secure aunque el backend esté en HTTPS
        boolean useSecure = isHttps && !isFrontendLocalhost;
        
        if (isElasticCloud || (isHttps && !isFrontendLocalhost) || isFrontendVercel) {
            cookie.append("; SameSite=None");
            if (useSecure || isFrontendVercel) {
                cookie.append("; Secure");
            }
            // Solo agregar Domain si es Elastic Cloud (mismo dominio raíz)
            // Para vercel.com ↔ elastic.uy (dominios diferentes), NO usar Domain
            if (isElasticCloud && !isFrontendVercel) {
                cookie.append("; Domain=.web.elasticloud.uy");
            }
        } else {
            cookie.append("; SameSite=Lax");
        }
        
        return cookie.toString();
    }
    
    /**
     * Construye el header Set-Cookie para eliminar una cookie (logout).
     * Versión sobrecargada sin originHeader (backward compatibility).
     * 
     * @param requestUrl URL del request (para detectar HTTPS)
     * @return String con el header Set-Cookie para eliminar la cookie
     */
    public static String buildDeleteCookieHeader(String requestUrl) {
        return buildDeleteCookieHeader(requestUrl, null);
    }

    private static String extractJwtFromCookie(HttpServletRequest request) {
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

    public static String resolveJwtToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String jwtToken = extractJwtFromCookie(request);
        if ((jwtToken == null || jwtToken.isEmpty()) && authHeader != null) {
            String trimmedHeader = authHeader.trim();
            if (trimmedHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
                jwtToken = trimmedHeader.substring(7).trim();
            } else if (!trimmedHeader.isEmpty()) {
                jwtToken = trimmedHeader;
            }
        }
        return (jwtToken != null && !jwtToken.isEmpty()) ? jwtToken : null;
    }
}


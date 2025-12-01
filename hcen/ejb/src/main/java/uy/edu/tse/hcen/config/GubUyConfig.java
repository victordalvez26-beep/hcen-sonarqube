package uy.edu.tse.hcen.config;

/**
 * Configuración para integración con ID Uruguay (Gub.uy).
 * 
 * Permite configurar URLs mediante variables de entorno para soportar
 * diferentes ambientes (local, producción) sin cambiar código.
 * 
 * Variables de entorno:
 * - GUBUY_REDIRECT_URI: URL de callback (default: http://localhost:8080)
 * - FRONTEND_URL: URL del frontend (default: http://localhost:3000)
 * - POST_LOGOUT_REDIRECT_URI: URL post-logout (default: http://localhost:8080/logout)
 */
public class GubUyConfig {
    
    public static final String CLIENT_ID = "890192";
    public static final String CLIENT_SECRET = "457d52f181bf11804a3365b49ae4d29a2e03bbabe74997a2f510b179";
    
    public static final String AUTHORIZATION_ENDPOINT = "https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize";
    public static final String TOKEN_ENDPOINT = "https://auth-testing.iduruguay.gub.uy/oidc/v1/token";
    public static final String USERINFO_ENDPOINT = "https://auth-testing.iduruguay.gub.uy/oidc/v1/userinfo";
    public static final String LOGOUT_ENDPOINT = "https://auth-testing.iduruguay.gub.uy/oidc/v1/logout";
    public static final String SCOPES = "openid personal_info email";
    
    // URLs configurables por ambiente mediante variables de entorno
    // PRODUCCIÓN: Usa Elastic Cloud con context path /hcen
    public static final String REDIRECT_URI = getEnvOrDefault(
        "GUBUY_REDIRECT_URI", 
        "https://env-6105410.web.elasticloud.uy/hcen/api/auth/login/callback"
    );
    
    public static final String FRONTEND_URL = getEnvOrDefault(
        "FRONTEND_URL", 
        "https://env-7952794.web.elasticloud.uy"
    );
    
    public static final String POST_LOGOUT_REDIRECT_URI = getEnvOrDefault(
        "POST_LOGOUT_REDIRECT_URI", 
        "https://env-7952794.web.elasticloud.uy/logout"
    );
    
    /**
     * Obtiene una variable de entorno o retorna un valor por defecto.
     * @param envVarName Nombre de la variable de entorno
     * @param defaultValue Valor por defecto si no está definida
     * @return Valor de la variable de entorno o el default
     */
    private static String getEnvOrDefault(String envVarName, String defaultValue) {
        String value = System.getenv(envVarName);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}


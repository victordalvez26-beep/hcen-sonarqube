package uy.edu.tse.hcen.config;

public class AppConfig {
    // Backend
    public static final String BACKEND_URL = "https://env-6105410.web.elasticloud.uy";
    public static final String FRONTEND_URL = "https://hcen-frontend-git-feature-login-victordalvez26-6494s-projects.vercel.app";
    public static final String LOGOUT_URL = BACKEND_URL + "/hcen/api/auth/logout_hcen";
    public static final String SESSION_STATUS_URL = BACKEND_URL + "/hcen/api/auth/session";
    public static final String AUTH_SESSION_URL = BACKEND_URL + "/hcen/api/auth/exchange-token";
    public static final String USER_DATA_URL = BACKEND_URL + "/hcen/api/users/data";
    public static final String UPDATE_USER_DATA_URL = BACKEND_URL + "/hcen/api/users/update";
    public static final String REGISTER_DEVICE_TOKEN_URL = BACKEND_URL + "/hcen/api/notifications/device-token";
    public static final String MANAGE_NOTIFICATIONS_URL = BACKEND_URL + "/hcen/api/notifications/preferences";
    public static final String SUMMARY_URL = BACKEND_URL + "/hcen/api/patient-summary";
    public static final String DOCS_URL = BACKEND_URL + "/hcen/api/metadatos-documento/usuario";
    public static final String HISTORY_PDF_URL = BACKEND_URL + "/hcen/api/metadatos-documento/paciente/historia";

    // Gubuy
    public static final String AUTH_GUBUY_URL = "https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize";
    public static final String CLIENT_ID_GUBUY = "890192";
    public static final String REDIRECT_URI = BACKEND_URL + "/hcen/api/auth/login/callback";
    public static final String SCOPE_GUBUY = "openid personal_info email";
    public static final String ID_DIGITAL = "https://www.gub.uy/agencia-gobierno-electronico-sociedad-informacion-conocimiento/identidad-digital";

    // Preferences
    public static final String PREFS_DATA = "secure_prefs";
    public static final String PREFS_PUSH_NOTIFICATIONS = "push_notifications_prefs";
    public static final String PREFS_USER_NOTIFICATIONS = "user_notifications_prefs";
    public static final String PREFS_TOKEN_FCM = "fcm_token";

}


package uy.edu.tse.hcen.utils;

/**
 * Utility class to build URLs for the INUS Service.
 */
public class InusServiceUrlUtil {

    private static final String DEFAULT_INUS_URL = "http://localhost:8082/pdi"; // Default local URL
    private static final String ENV_INUS_URL = "INUS_SERVICE_URL";

    public static String getBaseUrl() {
        String url = System.getenv(ENV_INUS_URL);
        if (url == null || url.isBlank()) {
            url = System.getProperty(ENV_INUS_URL);
        }
        
        if (url == null || url.isBlank()) {
            return DEFAULT_INUS_URL;
        }
        
        // Remove trailing slash if present
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String buildUrl(String path) {
        String baseUrl = getBaseUrl();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return baseUrl + path;
    }
}


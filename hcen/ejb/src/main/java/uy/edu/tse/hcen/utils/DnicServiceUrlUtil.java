package uy.edu.tse.hcen.utils;

import java.util.logging.Logger;

public class DnicServiceUrlUtil {
    private static final Logger LOGGER = Logger.getLogger(DnicServiceUrlUtil.class.getName());
    // Asumiendo que PDI corre en el puerto 8083 localmente
    private static final String DEFAULT_DNIC_SOAP_URL = "http://localhost:8083/ws"; 

    public static String getServiceUrl() {
        String baseUrl = System.getenv("DNIC_SERVICE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            LOGGER.warning("Environment variable DNIC_SERVICE_URL not set. Using default: " + DEFAULT_DNIC_SOAP_URL);
            baseUrl = DEFAULT_DNIC_SOAP_URL;
        }
        return baseUrl;
    }
}


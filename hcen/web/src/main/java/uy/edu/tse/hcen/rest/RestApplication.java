package uy.edu.tse.hcen.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import uy.edu.tse.hcen.filter.PrestadorSaludApiFilter;
import uy.edu.tse.hcen.rest.filter.CorsRequestFilter;
import uy.edu.tse.hcen.rest.filter.CorsResponseFilter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@ApplicationPath("/api")
public class RestApplication extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(RestApplication.class.getName());
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        // Registrar filtros CORS de JAX-RS (deben estar primero para que se ejecuten antes de los recursos)
        LOGGER.info("[REST-APPLICATION] Registrando filtros CORS explícitamente:");
        LOGGER.info("   1. CorsRequestFilter - Maneja OPTIONS (preflight)");
        classes.add(CorsRequestFilter.class);
        LOGGER.info("   2. CorsResponseFilter - Agrega headers CORS a todas las respuestas");
        classes.add(CorsResponseFilter.class);
        classes.add(PrestadorSaludApiFilter.class);
        
        // Registrar explícitamente los recursos principales
        LOGGER.info("[REST-APPLICATION] Registrando recursos REST:");
        classes.add(MetadatosDocumentoResource.class);
        classes.add(NodoPerifericoResource.class);
        classes.add(ConfigResource.class);
        classes.add(UsuarioSaludResource.class);
        classes.add(PrestadorSaludResource.class);
        classes.add(UserResource.class);
        classes.add(AuthResource.class);
        classes.add(PoliticasAccesoResource.class);
        classes.add(ReportesResource.class);
        classes.add(NotificationResource.class);
        classes.add(LoginCallbackResource.class);
        classes.add(PatientSummaryResource.class);
        classes.add(PrestadorSaludApiResource.class);
        
        LOGGER.info(String.format("[REST-APPLICATION] Total de clases registradas: %d", classes.size()));
        
        return classes;
    }
}

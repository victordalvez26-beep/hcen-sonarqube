package uy.edu.tse.hcen.politicas;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import uy.edu.tse.hcen.politicas.rest.PoliticaAccesoResource;
import uy.edu.tse.hcen.politicas.rest.SolicitudAccesoResource;
import uy.edu.tse.hcen.politicas.rest.RegistroAccesoResource;
import uy.edu.tse.hcen.politicas.rest.ReportesResource;
import uy.edu.tse.hcen.politicas.filter.CorsRequestFilter;
import uy.edu.tse.hcen.politicas.filter.CorsResponseFilter;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class PoliticasRestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // Filtros CORS (deben registrarse primero)
        classes.add(CorsRequestFilter.class);
        classes.add(CorsResponseFilter.class);
        // Recursos REST
        classes.add(PoliticaAccesoResource.class);
        classes.add(SolicitudAccesoResource.class);
        classes.add(RegistroAccesoResource.class);
        classes.add(ReportesResource.class);
        return classes;
    }
}







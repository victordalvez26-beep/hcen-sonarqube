package uy.gub.agesic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Plataforma de Interoperabilidad (PDI) - Simulador
 * 
 * Incluye:
 * - PDI/DNIC: Servicio Básico de Información (SOAP)
 * - INUS: Datos Patronímicos de Usuarios (REST)
 * 
 * Endpoints:
 * - WSDL: http://localhost:8083/ws/dnic.wsdl
 * - SOAP: http://localhost:8083/ws
 * - REST INUS: http://localhost:8083/api/inus/usuarios
 * 
 * @author TSE 2025
 */
@SpringBootApplication
@ComponentScan(basePackages = {"uy.gub.agesic", "uy.gub.agesic.pdi", "uy.gub.agesic.inus"})
@EnableJpaRepositories(basePackages = "uy.gub.agesic.inus.repository")
@EntityScan(basePackages = "uy.gub.agesic.inus.model")
public class PdiApplication {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  PDI - Plataforma de Interoperabilidad   ");
        System.out.println("  DNIC Servicio Básico de Información     ");
        System.out.println("  Simulador para TSE 2025                 ");
        System.out.println("===========================================");
        
        SpringApplication.run(PdiApplication.class, args);
        
        System.out.println("\n PDI iniciado correctamente");
    }
}
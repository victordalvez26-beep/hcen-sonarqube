package uy.edu.tse.hcen.filter;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar endpoints que requieren autenticación de prestador de salud.
 * 
 * Uso:
 * @PrestadorSaludApiSecured
 * @Path("/api/prestador-salud/...")
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PrestadorSaludApiSecured {
}


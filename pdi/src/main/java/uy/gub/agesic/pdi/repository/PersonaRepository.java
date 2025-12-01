package uy.gub.agesic.pdi.repository;

import org.springframework.stereotype.Repository;
import uy.gub.agesic.pdi.model.PersonaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio en memoria para simular la base de datos de DNIC
 * En un sistema real, esto consultaría la base de datos del Registro Civil
 */
@Repository
public class PersonaRepository {

    private static final Logger logger = LoggerFactory.getLogger(PersonaRepository.class);
    private static final String GONZALEZ = "González";
    private static final String MONTEVIDEO = "Montevideo";
    private static final String PASAPORTE = "PASAPORTE";
    private static final String CI = "CI";
    private static final String UY = "UY";
    
    private final List<PersonaData> personas = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Cargar datos de prueba
        
        // Personas mayores de edad
        personas.add(new PersonaData(
            CI,
            "50830691",
            "Victor",
            "David",
            "Alvez",
            GONZALEZ,
            LocalDate.of(2000, 12, 26),
            "M",
            UY,  // Código de nacionalidad
            MONTEVIDEO,
            MONTEVIDEO
        ));
        
        personas.add(new PersonaData(
            CI,
            "41500075",
            "Juan",
            "Carlos",
            "Pérez",
            GONZALEZ,
            LocalDate.of(1985, 3, 15),
            "M",
            UY,  // Código de nacionalidad
            MONTEVIDEO,
            MONTEVIDEO
        ));

        personas.add(new PersonaData(
            CI,
            "25850303",
            "María",
            "Laura",
            "López",
            "García",
            LocalDate.of(1990, 7, 22),
            "F",
            UY,  // Código de nacionalidad
            "Canelones",
            "Las Piedras"
        ));

        personas.add(new PersonaData(
            CI,
            "58076354",
            "Carlos",
            "Alberto",
            "Rodríguez",
            "Martínez",
            LocalDate.of(1978, 11, 8),
            "M",
            UY,  // Código de nacionalidad
            "Maldonado",
            "Punta del Este"
        ));

        personas.add(new PersonaData(
            CI,
            "38974671",
            "Ana",
            "Sofía",
            "Fernández",
            "Suárez",
            LocalDate.of(1995, 5, 30),
            "F",
            UY,  // Código de nacionalidad
            "Colonia",
            "Colonia del Sacramento"
        ));

        // Persona menor de edad (para pruebas de validación)
        personas.add(new PersonaData(
            CI,
            "39178531",
            "Pedro",
            "José",
            GONZALEZ,
            "Díaz",
            LocalDate.of(2010, 9, 12),
            "M",
            UY,  // Código de nacionalidad
            MONTEVIDEO,
            MONTEVIDEO
        ));

        // Persona extranjera (Brasil)
        personas.add(new PersonaData(
            PASAPORTE,
            "26347848",
            "Roberto",
            null,
            "Silva",
            "Santos",
            LocalDate.of(1982, 4, 18),
            "M",
            "BR",  // Código de nacionalidad
            null,
            null
        ));

        // Persona extranjera (Argentina)
        personas.add(new PersonaData(
            PASAPORTE,
            "AB789456",
            "Martín",
            "Eduardo",
            "Rodríguez",
            "López",
            LocalDate.of(1988, 6, 10),
            "M",
            "AR",  // Código de nacionalidad
            null,
            null
        ));

        // Persona extranjera (Chile)
        personas.add(new PersonaData(
            PASAPORTE,
            "CH456123",
            "Carla",
            "Isabel",
            GONZALEZ,
            "Muñoz",
            LocalDate.of(1992, 11, 25),
            "F",
            "CL",  // Código de nacionalidad
            null,
            null
        ));

        logger.info("PersonaRepository inicializado con {} personas", personas.size());
    }

    /**
     * Buscar persona por tipo y número de documento
     */
    public Optional<PersonaData> findByDocumento(String tipoDocumento, String numeroDocumento) {
        return personas.stream()
                .filter(p -> p.getTipoDocumento().equalsIgnoreCase(tipoDocumento) 
                          && p.getNumeroDocumento().equals(numeroDocumento))
                .findFirst();
    }

    /**
     * Buscar persona solo por número de documento (para REST API)
     */
    public Optional<PersonaData> findByDocumento(String numeroDocumento) {
        return personas.stream()
                .filter(p -> p.getNumeroDocumento().equals(numeroDocumento))
                .findFirst();
    }

    /**
     * Obtener todas las personas (para testing)
     */
    public List<PersonaData> findAll() {
        return new ArrayList<>(personas);
    }

    /**
     * Agregar o actualizar persona (para testing)
     */
    public void addPersona(PersonaData persona) {
        // Eliminar si ya existe para evitar duplicados y permitir actualizaciones
        personas.removeIf(p -> p.getTipoDocumento().equalsIgnoreCase(persona.getTipoDocumento()) 
                            && p.getNumeroDocumento().equals(persona.getNumeroDocumento()));
        
        personas.add(persona);
    }
}



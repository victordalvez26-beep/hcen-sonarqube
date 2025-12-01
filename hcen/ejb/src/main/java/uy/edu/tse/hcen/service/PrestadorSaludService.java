package uy.edu.tse.hcen.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import uy.edu.tse.hcen.model.EstadoPrestador;
import uy.edu.tse.hcen.model.PrestadorSalud;
import uy.edu.tse.hcen.repository.PrestadorSaludRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar Prestadores de Salud.
 */
@Stateless
public class PrestadorSaludService {
    
    private static final Logger LOGGER = Logger.getLogger(PrestadorSaludService.class);
    
    @Inject
    private PrestadorSaludRepository repository;
    
    @Inject
    private EmailService emailService;
    
    // URL base para el formulario de registro del prestador (en HCEN Frontend)
    private static final String BASE_URL = System.getenv().getOrDefault(
        "HCEN_API_URL", 
        "http://host.docker.internal:8080/api/prestador-salud/services"
    );
    private static final String REGISTRO_BASE_URL = BASE_URL + "/registro-prestador";
    
    /**
     * Crea una invitación para un nuevo prestador de salud.
     * 
     * @param nombre Nombre del prestador
     * @param contacto Email de contacto
     * @return El prestador creado con token de invitación
     */
    public PrestadorSalud crearInvitacion(String nombre, String contacto) {
        
        LOGGER.info("Creando invitación para prestador: " + nombre + " - " + contacto);
        
        // 1. Crear prestador con estado PENDIENTE_REGISTRO
        PrestadorSalud prestador = new PrestadorSalud(nombre, contacto);
        
        // 2. Generar token de invitación
        String token = UUID.randomUUID().toString();
        prestador.setInvitationToken(token);
        
        // 3. Construir URL de registro
        String registroUrl = REGISTRO_BASE_URL + "?token=" + token;
        prestador.setInvitationUrl(registroUrl);
        
        // 4. Token expira en 7 días
        prestador.setTokenExpiresAt(LocalDateTime.now().plusDays(7));
        
        // 5. Guardar en BD
        repository.persist(prestador);
        
        LOGGER.info("Prestador creado con ID: " + prestador.getId());
        
        // 6. Enviar email de invitación
        try {
            emailService.sendPrestadorInvitationEmail(
                contacto,
                nombre,
                registroUrl
            );
            LOGGER.info("Email de invitación enviado a: " + contacto);
        } catch (Exception e) {
            LOGGER.error("Error al enviar email de invitación: " + e.getMessage(), e);
            // Continuar aunque falle el email (el admin puede reenviar el link manualmente)
        }
        
        return prestador;
    }
    
    /**
     * Completa el registro de un prestador con todos sus datos.
     * 
     * @param token Token de invitación
     * @param rut RUT del prestador
     * @param url URL del servidor del prestador
     * @param departamento Departamento
     * @param localidad Localidad
     * @param direccion Dirección
     * @param telefono Teléfono
     * @return El prestador actualizado
     */
    public PrestadorSalud completarRegistro(
        String token,
        String rut,
        String url,
        String departamento,
        String localidad,
        String direccion,
        String telefono
    ) {
        
        LOGGER.info("Completando registro de prestador con token: " + token);
        
        // 1. Buscar prestador por token
        PrestadorSalud prestador = repository.findByInvitationToken(token);
        
        if (prestador == null) {
            throw new IllegalArgumentException("Token de invitación inválido");
        }
        
        // 2. Verificar que el token no haya expirado
        if (prestador.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token de invitación ha expirado");
        }
        
        // 3. Actualizar datos del prestador
        prestador.setRut(rut);
        prestador.setUrl(url);
        
        // Convertir departamento string a enum
        if (departamento != null && !departamento.isEmpty()) {
            try {
                prestador.setDepartamento(
                    uy.edu.tse.hcen.model.Departamento.valueOf(departamento)
                );
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Departamento inválido: " + departamento);
            }
        }
        
        prestador.setLocalidad(localidad);
        prestador.setDireccion(direccion);
        prestador.setTelefono(telefono);
        
        // 4. Cambiar estado a ACTIVO
        prestador.setEstado(EstadoPrestador.ACTIVO);
        
        // 5. Generar API Key para autenticación de servicios
        String apiKey = generarApiKey();
        prestador.setApiKey(apiKey);
        LOGGER.info("API Key generada para prestador ID: " + prestador.getId());
        
        // 6. Limpiar token (ya fue usado)
        prestador.setInvitationToken(null);
        prestador.setInvitationUrl(null);
        prestador.setTokenExpiresAt(null);
        
        // 7. Guardar
        repository.merge(prestador);
        
        LOGGER.info("Registro completado para prestador ID: " + prestador.getId() + ", URL: " + url);
        
        return prestador;
    }
    
    /**
     * Obtiene todos los prestadores.
     */
    public List<PrestadorSalud> listarTodos() {
        return repository.findAll();
    }
    
    /**
     * Obtiene un prestador por ID.
     */
    public PrestadorSalud obtenerPorId(Long id) {
        return repository.findById(id);
    }
    
    /**
     * Actualiza un prestador existente.
     */
    public PrestadorSalud actualizar(PrestadorSalud prestador) {
        return repository.merge(prestador);
    }
    
    /**
     * Obtiene un prestador por su API Key.
     */
    public PrestadorSalud obtenerPorApiKey(String apiKey) {
        return repository.findByApiKey(apiKey);
    }
    
    /**
     * Genera una API Key segura para el prestador.
     * Formato: ps_<base64(random 32 bytes)>
     */
    private String generarApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return "ps_" + base64;
    }
}


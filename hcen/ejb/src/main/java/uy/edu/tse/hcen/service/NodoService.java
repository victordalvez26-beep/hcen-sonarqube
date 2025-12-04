package uy.edu.tse.hcen.service;

import uy.edu.tse.hcen.model.EstadoNodoPeriferico;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uy.edu.tse.hcen.model.NodoPeriferico;
import uy.edu.tse.hcen.repository.NodoPerifericoRepository;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio para gestión de nodos periféricos.
 * 
 * CAMBIO: Reemplazamos mensajería RabbitMQ por llamadas HTTP directas al componente periférico.
 * Esto simplifica el flujo y proporciona feedback inmediato sobre el éxito/fracaso de la operación.
 */
@Stateless
public class NodoService {

    private static final Logger logger = Logger.getLogger(NodoService.class.getName());

    @Inject
    private NodoPerifericoRepository repo;

    @Inject
    private uy.edu.tse.hcen.repository.UserClinicAssociationRepository associationRepo;

    @Inject
    private NodoPerifericoHttpClient httpClient;

    @Inject
    private EmailService emailService;

    /**
     * Busca un nodo por su ID.
     * @param id ID del nodo
     * @return NodoPeriferico o null si no existe
     */
    public NodoPeriferico find(Long id) {
        return repo.find(id);
    }

    /**
     * Obtiene las asociaciones de clínicas para un usuario.
     * @param userId ID del usuario
     * @return Lista de asociaciones
     */
    public java.util.List<uy.edu.tse.hcen.model.UserClinicAssociation> findAssociationsByUser(Long userId) {
        return associationRepo.findByUser(userId);
    }

    /**
     * Crea el nodo en BD e inmediatamente llama al componente periférico para inicializarlo.
     * 
     * Flujo:
     * 1. Persiste el nodo con estado PENDIENTE
     * 2. Hace llamada HTTP al endpoint /config/init del componente periférico
     * 3. Si exitoso → actualiza estado a ACTIVO
     * 4. Si falla → actualiza estado a ERROR_MENSAJERIA
     * 
     * @param nodo Datos del nodo a crear
     * @return Nodo creado con su estado actualizado
     */
    @Transactional
    public NodoPeriferico createAndNotify(NodoPeriferico nodo) {
        logger.info(String.format("Creating clinic invitation: %s (email: %s)", 
                                  nodo.getNombre(), nodo.getContacto()));
        
        // 1. Persistir el nodo con estado PENDIENTE (esperando que la clínica complete el formulario)
        if (nodo.getEstado() == null) {
            nodo.setEstado(EstadoNodoPeriferico.PENDIENTE);
        }
        NodoPeriferico created = repo.create(nodo);
        
        // 2. Generar token de invitación único
        String invitationToken = java.util.UUID.randomUUID().toString();
        created.setActivationToken(invitationToken);
        
        // 3. Generar URL de activación/registro completo (la clínica completará RUT, dirección, usuario, contraseña)
        // Usar variable de entorno PERIPHERAL_FRONTEND_URL si está configurada, sino usar localhost:3001 por defecto
        String peripheralFrontendUrl = System.getenv().getOrDefault("PERIPHERAL_FRONTEND_URL", "http://localhost:3001");
        String activationUrl = peripheralFrontendUrl + "/portal/clinica/" + created.getId() + 
                              "/activate?token=" + invitationToken;
        created.setActivationUrl(activationUrl);
        
        // 4. Extraer email del contacto
        String adminEmail = extractEmailFromContacto(created.getContacto());
        created.setAdminEmail(adminEmail);
        
        repo.update(created);
        
        // 5. Enviar email de invitación (NO crear tenant todavía - se creará cuando completen el formulario)
        if (adminEmail != null && !adminEmail.isBlank()) {
            try {
                logger.info("Sending invitation email to: " + adminEmail);
                boolean emailSent = emailService.sendInvitationEmail(
                    adminEmail,
                    created.getNombre(),
                    activationUrl,
                    String.valueOf(created.getId())
                );
                
                if (emailSent) {
                    logger.info("Invitation email sent successfully to: " + adminEmail);
                } else {
                    logger.warning("Failed to send invitation email");
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to send invitation email", e);
                // No cambiamos a ERROR porque el token sigue válido
            }
        } else {
            logger.warning("No email found in contacto field. Cannot send invitation.");
        }
        
        logger.info("Clinic invitation created with ID=" + created.getId() + ". Waiting for admin to complete registration at: " + activationUrl);
        
        return created;
    }

    @Transactional
    public NodoPeriferico updateAndNotify(NodoPeriferico nodo) {
        logger.info(String.format("Updating and notifying nodo: %s (id: %s)", 
                                  nodo.getNombre(), nodo.getId()));
        
        NodoPeriferico updated = repo.update(nodo);
        
        // Llamar al componente periférico para actualizar configuración
        NodoPerifericoHttpClient.NodoInitPayload payload = buildPayload(updated);
        
        try {
            String baseUrl = updated.getNodoPerifericoUrlBase();
            if (baseUrl != null && !baseUrl.isBlank()) {
                boolean success = httpClient.updateTenant(baseUrl, payload);
                logger.info("Update call to peripheral node: " + (success ? "SUCCESS" : "FAILED"));
            } else {
                logger.warning("No URL configured for nodo " + updated.getId());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error calling peripheral node for update", e);
        }
        
        return updated;
    }

    @Transactional
    public void deleteAndNotify(Long id) {
        logger.info("Deleting and notifying nodo id: " + id);
        
        NodoPeriferico existing = repo.find(id);
        if (existing == null) {
            logger.warning("Nodo " + id + " not found for deletion");
            return;
        }
        
        // Llamar al componente periférico antes de eliminar
        try {
            String baseUrl = existing.getNodoPerifericoUrlBase();
            if (baseUrl != null && !baseUrl.isBlank()) {
                boolean success = httpClient.deleteTenant(baseUrl, id);
                logger.info("Delete call to peripheral node: " + (success ? "SUCCESS" : "FAILED"));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error calling peripheral node for delete", e);
        }
        
        // Eliminar de BD
        repo.delete(id);
    }

    /**
     * Llama al componente periférico para un nodo existente sin modificar BD.
     * Útil para reintentar inicialización en caso de fallo.
     * 
     * @param id ID del nodo
     * @param action Acción a realizar: "init", "update", o "delete"
     */
    public void notifyPeripheralNode(Long id, String action) {
        logger.info(String.format("Notifying peripheral node for nodo id=%s, action=%s", id, action));
        
        NodoPeriferico existing = repo.find(id);
        if (existing == null) {
            throw new RuntimeException("Nodo not found: " + id);
        }
        
        String baseUrl = existing.getNodoPerifericoUrlBase();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new RuntimeException("No nodoPerifericoUrlBase configured for nodo " + id);
        }
        
        NodoPerifericoHttpClient.NodoInitPayload payload = buildPayload(existing);
        boolean success = false;
        
        try {
            if (action == null || action.equalsIgnoreCase("init")) {
                NodoPerifericoHttpClient.InitResponse response = httpClient.initializeTenant(baseUrl, payload);
                success = response != null && response.success;
            } else if (action.equalsIgnoreCase("update")) {
                success = httpClient.updateTenant(baseUrl, payload);
            } else if (action.equalsIgnoreCase("delete")) {
                success = httpClient.deleteTenant(baseUrl, id);
            } else {
                throw new IllegalArgumentException("Unknown action: " + action);
            }
            
            if (!success) {
                throw new RuntimeException("Peripheral node returned failure status");
            }
            
            logger.info("Notification successful for nodo " + id);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to notify peripheral node", e);
            throw new RuntimeException("Failed to notify peripheral node", e);
        }
    }

    /**
     * Método auxiliar para construir el payload que se envía al componente periférico.
     */
    private NodoPerifericoHttpClient.NodoInitPayload buildPayload(NodoPeriferico nodo) {
        NodoPerifericoHttpClient.NodoInitPayload payload = new NodoPerifericoHttpClient.NodoInitPayload();
        payload.id = nodo.getId();
        payload.rut = nodo.getRUT();
        payload.nombre = nodo.getNombre();
        payload.departamento = nodo.getDepartamento() != null ? nodo.getDepartamento().name() : null;
        payload.localidad = nodo.getLocalidad();
        payload.direccion = nodo.getDireccion();
        payload.nodoPerifericoUrlBase = nodo.getNodoPerifericoUrlBase();
        payload.nodoPerifericoUsuario = nodo.getNodoPerifericoUsuario();
        payload.nodoPerifericoPassword = nodo.getNodoPerifericoPassword();
        payload.contacto = nodo.getContacto();
        payload.url = nodo.getUrl();
        return payload;
    }
    
    /**
     * Actualiza el estado de un nodo.
     * Útil para marcar como ACTIVO después de inicialización exitosa.
     */
    @Transactional
    public void updateEstado(Long id, EstadoNodoPeriferico nuevoEstado) {
        NodoPeriferico nodo = repo.find(id);
        if (nodo != null) {
            logger.info(String.format("Updating estado for nodo %s from %s to %s", 
                                     id, nodo.getEstado(), nuevoEstado));
            nodo.setEstado(nuevoEstado);
            repo.update(nodo);
        } else {
            logger.warning("Cannot update estado - nodo " + id + " not found");
        }
    }

    /**
     * Extrae un email del campo de contacto.
     * Busca un patrón de email en el string de contacto.
     */
    private String extractEmailFromContacto(String contacto) {
        if (contacto == null || contacto.isBlank()) {
            return null;
        }
        
        // Regex simple para detectar email
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        );
        java.util.regex.Matcher matcher = pattern.matcher(contacto);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return null;
    }
}

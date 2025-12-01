package uy.edu.tse.hcen.rest;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.model.Rol;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.model.UserClinicAssociation;
import uy.edu.tse.hcen.repository.UserClinicAssociationRepository;
import uy.edu.tse.hcen.rest.dto.HcenUserResponse;
import uy.edu.tse.hcen.rest.dto.UsuarioSaludDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Endpoint REST para gestionar Usuarios de Salud (pacientes) en el INUS.
 * Este endpoint es invocado por los nodos periféricos cuando registran pacientes.
 * 
 * Ruta completa: /api/usuarios-salud (ApplicationPath=/api + @Path=/usuarios-salud)
 */
@Path("/usuarios-salud")
@Stateless
public class UsuarioSaludResource {
    
    private static final Logger LOGGER = Logger.getLogger(UsuarioSaludResource.class);
    
    @Inject
    private UserDAO userDAO;
    
    @Inject
    private UserClinicAssociationRepository associationRepo;
    
    @Inject
    private uy.edu.tse.hcen.service.InusIntegrationService inusService;
    
    /**
     * Crea o modifica un Usuario de Salud en el INUS central.
     * 
     * Flujo:
     * 1. Busca el usuario por documento (CI)
     * 2. Si NO existe -> crea nuevo User con rol USUARIO_SALUD
     * 3. Si existe -> actualiza solo campos NO nulos (preserva datos existentes)
     * 4. Verifica si existe asociación User-Clínica
     * 5. Si NO existe -> crea la asociación
     * 6. Devuelve el userId
     * 
     * @param dto Datos del usuario de salud
     * @return Response con userId y mensaje
     */
    @POST
    @Path("/crear-modificar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearModificarUsuarioSalud(UsuarioSaludDTO dto) {
        
        LOGGER.info("Recibiendo solicitud para crear/modificar usuario de salud - CI: " + 
                    dto.getCi() + ", Clínica: " + dto.getTenantId());
        
        try {
            // Validaciones básicas
            if (dto.getCi() == null || dto.getCi().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new HcenUserResponse(null, "CI es requerido"))
                    .build();
            }
            
            if (dto.getTenantId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new HcenUserResponse(null, "TenantId es requerido"))
                    .build();
            }

            // -----------------------------------------------------
            // 1. INTEGRACIÓN CON INUS (Master Data)
            // -----------------------------------------------------
            
            // Preparar datos recibidos para enviar a INUS
            User datosEntrada = new User();
            datosEntrada.setTipDocum("CI");
            datosEntrada.setCodDocum(dto.getCi());
            datosEntrada.setPrimerNombre(dto.getNombre());
            datosEntrada.setPrimerApellido(dto.getApellido());
            if (dto.getFechaNacimiento() != null) {
                 datosEntrada.setFechaNacimiento(Date.from(dto.getFechaNacimiento().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            datosEntrada.setDireccion(dto.getDireccion());
            datosEntrada.setTelefono(dto.getTelefono());
            datosEntrada.setEmail(dto.getEmail());
            datosEntrada.setDepartamento(dto.getDepartamento());
            datosEntrada.setLocalidad(dto.getLocalidad());
            // Valores por defecto para creación
            datosEntrada.setNacionalidad(uy.edu.tse.hcen.model.Nacionalidad.UY); 
            datosEntrada.setRol(Rol.USUARIO_SALUD);

            // 1. Intentar buscar por UID generado (Estándar actual: uy-ci-...)
            String generatedUid = uy.edu.tse.hcen.utils.UserUuidUtil.generateUuid(dto.getCi());
            LOGGER.info("Buscando usuario en INUS por UID generado: " + generatedUid);
            User inusUser = inusService.obtenerUsuarioPorUid(generatedUid);
            
            // 2. Si no se encuentra, buscar por Documento (para soportar UIDs legacy como PACIENTE_...)
            if (inusUser == null) {
                LOGGER.info("Usuario no encontrado por UID. Buscando por Documento (CI): " + dto.getCi());
                inusUser = inusService.obtenerUsuarioPorDocumento("CI", dto.getCi());
            }
            
            String finalUid;

            if (inusUser == null) {
                // No existe en INUS -> Crear
                LOGGER.info("Usuario no encontrado en INUS (ni por UID ni por Doc). Creando nuevo registro...");
                // Generar UID
                finalUid = generatedUid;
                datosEntrada.setUid(finalUid);
                
                boolean creado = inusService.crearUsuarioEnInus(datosEntrada);
                if (!creado) {
                    LOGGER.warn("⚠️ No se pudo crear el usuario en INUS. Se procederá a crear localmente pero podría haber inconsistencias.");
                } else {
                    LOGGER.info("✅ Usuario creado exitosamente en INUS.");
                }
            } else {
                // Existe en INUS -> Actualizar datos de INUS con los recibidos (PRIORIDAD CLÍNICA)
                LOGGER.info("Usuario encontrado en INUS (UID: " + inusUser.getUid() + "). Actualizando con datos de clínica...");
                finalUid = inusUser.getUid();
                
                // Actualizar inusUser con datos del DTO
                boolean huboCambios = false;
                
                if (dto.getNombre() != null && !dto.getNombre().equals(inusUser.getPrimerNombre())) {
                    inusUser.setPrimerNombre(dto.getNombre());
                    huboCambios = true;
                }
                if (dto.getApellido() != null && !dto.getApellido().equals(inusUser.getPrimerApellido())) {
                    inusUser.setPrimerApellido(dto.getApellido());
                    huboCambios = true;
                }
                
                if (dto.getFechaNacimiento() != null) {
                     Date nuevaFecha = Date.from(dto.getFechaNacimiento().atStartOfDay(ZoneId.systemDefault()).toInstant());
                     // Comparación simple, asumiendo que las fechas son el principal cambio
                     if (inusUser.getFechaNacimiento() == null || !inusUser.getFechaNacimiento().equals(nuevaFecha)) {
                         inusUser.setFechaNacimiento(nuevaFecha);
                         huboCambios = true;
                     }
                }
                
                if (dto.getDireccion() != null && !dto.getDireccion().equals(inusUser.getDireccion())) {
                    inusUser.setDireccion(dto.getDireccion());
                    huboCambios = true;
                }
                if (dto.getTelefono() != null && !dto.getTelefono().equals(inusUser.getTelefono())) {
                    inusUser.setTelefono(dto.getTelefono());
                    huboCambios = true;
                }
                if (dto.getEmail() != null && !dto.getEmail().equals(inusUser.getEmail())) {
                    inusUser.setEmail(dto.getEmail());
                    huboCambios = true;
                }
                if (dto.getDepartamento() != null && dto.getDepartamento() != inusUser.getDepartamento()) {
                    inusUser.setDepartamento(dto.getDepartamento());
                    huboCambios = true;
                }
                if (dto.getLocalidad() != null && !dto.getLocalidad().equals(inusUser.getLocalidad())) {
                    inusUser.setLocalidad(dto.getLocalidad());
                    huboCambios = true;
                }

                // Actualizar INUS
                LOGGER.info("Actualizando registro de INUS con datos de clínica...");
                boolean actualizado = inusService.actualizarUsuarioEnInus(inusUser);
                if (actualizado) LOGGER.info("✅ Usuario actualizado exitosamente en INUS.");
                else LOGGER.warn("⚠️ No se pudo actualizar el usuario en INUS.");
                
                // Usamos el objeto inusUser actualizado como la fuente para los datos locales
                datosEntrada = inusUser;
                datosEntrada.setUid(finalUid);
                if (datosEntrada.getRol() == null) datosEntrada.setRol(Rol.USUARIO_SALUD);
            }

            // -----------------------------------------------------
            // 2. ACTUALIZACIÓN LOCAL (Réplica/Caché)
            // -----------------------------------------------------
            
            // Buscar usuario local por documento (CI)
            User user = userDAO.findByDocumento(dto.getCi());
            boolean isNewUser = (user == null);
            
            if (isNewUser) {
                // 2a. NO existe localmente -> Crear nuevo User (Copia de INUS)
                LOGGER.info("Usuario no existe localmente, creando réplica con CI: " + dto.getCi());
                
                // Si datosEntrada viene de INUS, ya tiene todos los datos
                // Si es nuevo, tiene los datos del DTO
                
                // Persistir
                userDAO.persist(datosEntrada);
                user = datosEntrada; // Referencia para la asociación
                
                LOGGER.info("Usuario local creado con ID: " + user.getId());
                
            } else {
                // 2b. Existe localmente -> Actualizar con datos de INUS (Master)
                LOGGER.info("Usuario existe localmente (ID: " + user.getId() + "), sincronizando con INUS");
                
                // Asegurar que el UID coincida con INUS
                if (!finalUid.equals(user.getUid())) {
                    LOGGER.warn("Corrigiendo UID local (" + user.getUid() + ") para coincidir con INUS (" + finalUid + ")");
                    user.setUid(finalUid);
                }
                
                // Actualizar campos locales con los datos de INUS (datosEntrada)
                user.setPrimerNombre(datosEntrada.getPrimerNombre());
                user.setPrimerApellido(datosEntrada.getPrimerApellido());
                user.setFechaNacimiento(datosEntrada.getFechaNacimiento());
                user.setDireccion(datosEntrada.getDireccion());
                user.setTelefono(datosEntrada.getTelefono());
                user.setEmail(datosEntrada.getEmail());
                user.setDepartamento(datosEntrada.getDepartamento());
                user.setLocalidad(datosEntrada.getLocalidad());
                user.setNacionalidad(datosEntrada.getNacionalidad());
                
                userDAO.merge(user);
                LOGGER.info("Usuario local actualizado con ID: " + user.getId());
            }
            
            // 3. Verificar asociación User-Clínica
            UserClinicAssociation association = associationRepo.findByUserAndClinic(
                user.getId(), 
                dto.getTenantId()
            );
            
            // Replicar asociación en INUS (Dual Write)
            LOGGER.info("Replicando asociación Usuario-Clínica en INUS...");
            boolean asociacionInus = inusService.asociarUsuarioConPrestador(finalUid, dto.getTenantId(), null);
            if (asociacionInus) {
                LOGGER.info("✅ Asociación replicada exitosamente en INUS.");
            } else {
                LOGGER.warn("⚠️ No se pudo replicar la asociación en INUS (Usuario: " + finalUid + ", Prestador: " + dto.getTenantId() + ")");
            }
            
            if (association == null) {
                // 4. NO existe la asociación -> Crearla
                LOGGER.info("Creando asociación User-Clínica: userId=" + user.getId() + 
                           ", clinicId=" + dto.getTenantId());
                
                association = new UserClinicAssociation();
                association.setUserId(user.getId());
                association.setClinicTenantId(dto.getTenantId());
                association.setFechaAlta(LocalDateTime.now());
                
                associationRepo.persist(association);
                
                LOGGER.info("Asociación creada con ID: " + association.getId());
            } else {
                LOGGER.info("Asociación ya existe (ID: " + association.getId() + "), no se modifica");
            }
            
            // 5. Devolver respuesta
            String mensaje = isNewUser ? 
                "Usuario creado/sincronizado con INUS y asociado correctamente" : 
                "Usuario actualizado/sincronizado con INUS y asociado correctamente";
            
            return Response.ok(new HcenUserResponse(user.getId(), mensaje)).build();
            
        } catch (Exception e) {
            LOGGER.error("Error procesando usuario de salud: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new HcenUserResponse(null, "Error interno: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Endpoint de prueba para verificar que el servicio está funcionando.
     */
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
        return Response.ok("UsuarioSalud service is running").build();
    }
}


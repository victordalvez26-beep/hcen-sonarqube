package uy.edu.tse.hcen.rest;

import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.edu.tse.hcen.model.NotificationType;
import uy.edu.tse.hcen.model.UserNotificationPreferences;
import uy.edu.tse.hcen.repository.UserNotificationPreferencesRepository;
import uy.edu.tse.hcen.rest.dto.DeviceTokenDTO;
import uy.edu.tse.hcen.rest.dto.NotificationPreferencesDTO;
import uy.edu.tse.hcen.rest.dto.SendNotificationDTO;
import uy.edu.tse.hcen.service.NotificationService;
import uy.edu.tse.hcen.util.CookieUtil;
import uy.edu.tse.hcen.util.JWTUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Resource REST para gestionar preferencias de notificaciones y device tokens.
 */
@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationResource.class.getName());
    
    @EJB
    private UserNotificationPreferencesRepository preferencesRepository;
    
    @EJB
    private NotificationService notificationService;
    
    /**
     * Obtiene las preferencias de notificaciones del usuario autenticado.
     * GET /api/notifications/preferences
     */
    @GET
    @Path("/preferences")
    public Response getPreferences(@Context HttpServletRequest request) {
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "No autenticado"))
                    .build();
            }
            
            UserNotificationPreferences preferences = preferencesRepository.findByUserUid(userUid);
            
            if (preferences == null) {
                // Crear preferencias por defecto si no existen
                preferences = new UserNotificationPreferences(userUid);
                preferencesRepository.persist(preferences);
            }
            
            NotificationPreferencesDTO dto = toDTO(preferences);
            return Response.ok(dto).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo preferencias: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno del servidor"))
                .build();
        }
    }
    
    /**
     * Actualiza las preferencias de notificaciones del usuario autenticado.
     * PUT /api/notifications/preferences
     */
    @PUT
    @Path("/preferences")
    public Response updatePreferences(@Context HttpServletRequest request, NotificationPreferencesDTO dto) {
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "No autenticado"))
                    .build();
            }
            
            UserNotificationPreferences preferences = preferencesRepository.findByUserUid(userUid);
            
            if (preferences == null) {
                preferences = new UserNotificationPreferences(userUid);
            }
            
            // Actualizar preferencias
            preferences.setNotifyResults(dto.isNotifyResults());
            preferences.setNotifyNewAccessRequest(dto.isNotifyNewAccessRequest());
            preferences.setNotifyMedicalHistory(dto.isNotifyMedicalHistory());
            preferences.setNotifyNewAccessHistory(dto.isNotifyNewAccessHistory());
            preferences.setNotifyMaintenance(dto.isNotifyMaintenance());
            preferences.setNotifyNewFeatures(dto.isNotifyNewFeatures());
            preferences.setAllDisabled(dto.isAllDisabled());
            
            preferencesRepository.saveOrUpdate(preferences);
            
            return Response.ok(Map.of("success", true, "message", "Preferencias actualizadas correctamente")).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error actualizando preferencias: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno del servidor"))
                .build();
        }
    }
    
    /**
     * Registra o actualiza el device token de Firebase del usuario autenticado.
     * POST /api/notifications/device-token
     */
    @POST
    @Path("/device-token")
    public Response registerDeviceToken(@Context HttpServletRequest request, DeviceTokenDTO dto) {
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "No autenticado"))
                    .build();
            }
            
            if (dto.getDeviceToken() == null || dto.getDeviceToken().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El device token es requerido"))
                    .build();
            }
            
            preferencesRepository.updateDeviceToken(userUid, dto.getDeviceToken().trim());
            
            return Response.ok(Map.of("success", true, "message", "Device token registrado correctamente")).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error registrando device token: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno del servidor"))
                .build();
        }
    }
    
    /**
     * Elimina el device token del usuario autenticado.
     * DELETE /api/notifications/device-token
     */
    @DELETE
    @Path("/device-token")
    public Response removeDeviceToken(@Context HttpServletRequest request) {
        try {
            String jwtToken = CookieUtil.resolveJwtToken(request);
            String userUid = JWTUtil.validateJWT(jwtToken);
            if (userUid == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "No autenticado"))
                    .build();
            }
            
            UserNotificationPreferences preferences = preferencesRepository.findByUserUid(userUid);
            if (preferences != null) {
                preferences.setDeviceToken(null);
                preferencesRepository.saveOrUpdate(preferences);
            }
            
            return Response.ok(Map.of("success", true, "message", "Device token eliminado correctamente")).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error eliminando device token: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno del servidor"))
                .build();
        }
    }
    
    /**
     * Endpoint interno para enviar una notificación a un usuario.
     * POST /api/notifications/send
     * 
     * Nota: En producción, este endpoint debería estar protegido o ser interno.
     */
    @POST
    @Path("/send")
    public Response sendNotification(SendNotificationDTO dto) {
        try {
            if (dto.getUserUid() == null || dto.getUserUid().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El userUid es requerido"))
                    .build();
            }
            
            if (dto.getNotificationType() == null || dto.getNotificationType().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El tipo de notificación es requerido"))
                    .build();
            }
            
            if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El título es requerido"))
                    .build();
            }
            
            if (dto.getBody() == null || dto.getBody().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "El cuerpo del mensaje es requerido"))
                    .build();
            }
            
            NotificationType notificationType = NotificationType.fromCode(dto.getNotificationType());
            if (notificationType == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Tipo de notificación inválido: " + dto.getNotificationType()))
                    .build();
            }
            
            boolean sent = notificationService.sendNotification(
                dto.getUserUid(),
                notificationType,
                dto.getTitle(),
                dto.getBody()
            );
            
            if (sent) {
                return Response.ok(Map.of("success", true, "message", "Notificación enviada correctamente")).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "No se pudo enviar la notificación. Verifique las preferencias del usuario y el device token."))
                    .build();
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error enviando notificación: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error interno del servidor: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Convierte una entidad UserNotificationPreferences a DTO.
     */
    private NotificationPreferencesDTO toDTO(UserNotificationPreferences preferences) {
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();
        dto.setNotifyResults(preferences.isNotifyResults());
        dto.setNotifyNewAccessRequest(preferences.isNotifyNewAccessRequest());
        dto.setNotifyMedicalHistory(preferences.isNotifyMedicalHistory());
        dto.setNotifyNewAccessHistory(preferences.isNotifyNewAccessHistory());
        dto.setNotifyMaintenance(preferences.isNotifyMaintenance());
        dto.setNotifyNewFeatures(preferences.isNotifyNewFeatures());
        dto.setAllDisabled(preferences.isAllDisabled());
        return dto;
    }
}


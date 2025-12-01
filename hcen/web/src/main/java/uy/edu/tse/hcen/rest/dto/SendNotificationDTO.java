package uy.edu.tse.hcen.rest.dto;

/**
 * DTO para enviar una notificación a un usuario.
 */
public class SendNotificationDTO {
    
    private String userUid;
    private String notificationType; // Código del tipo de notificación
    private String title;
    private String body;
    
    public SendNotificationDTO() {
    }
    
    public SendNotificationDTO(String userUid, String notificationType, String title, String body) {
        this.userUid = userUid;
        this.notificationType = notificationType;
        this.title = title;
        this.body = body;
    }
    
    // Getters and Setters
    
    public String getUserUid() {
        return userUid;
    }
    
    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
}


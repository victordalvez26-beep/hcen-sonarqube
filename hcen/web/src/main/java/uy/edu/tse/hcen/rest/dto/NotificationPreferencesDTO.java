package uy.edu.tse.hcen.rest.dto;

/**
 * DTO para transferir las preferencias de notificaciones de un usuario.
 */
public class NotificationPreferencesDTO {
    
    private boolean notifyResults = true;
    private boolean notifyNewAccessRequest = true;
    private boolean notifyMedicalHistory = true;
    private boolean notifyNewAccessHistory = true;
    private boolean notifyMaintenance = true;
    private boolean notifyNewFeatures = true;
    private boolean allDisabled = false;
    
    public NotificationPreferencesDTO() {
    }
    
    // Getters and Setters
    
    public boolean isNotifyResults() {
        return notifyResults;
    }
    
    public void setNotifyResults(boolean notifyResults) {
        this.notifyResults = notifyResults;
    }
    
    public boolean isNotifyNewAccessRequest() {
        return notifyNewAccessRequest;
    }
    
    public void setNotifyNewAccessRequest(boolean notifyNewAccessRequest) {
        this.notifyNewAccessRequest = notifyNewAccessRequest;
    }
    
    public boolean isNotifyMedicalHistory() {
        return notifyMedicalHistory;
    }
    
    public void setNotifyMedicalHistory(boolean notifyMedicalHistory) {
        this.notifyMedicalHistory = notifyMedicalHistory;
    }
    
    public boolean isNotifyNewAccessHistory() {
        return notifyNewAccessHistory;
    }
    
    public void setNotifyNewAccessHistory(boolean notifyNewAccessHistory) {
        this.notifyNewAccessHistory = notifyNewAccessHistory;
    }
    
    public boolean isNotifyMaintenance() {
        return notifyMaintenance;
    }
    
    public void setNotifyMaintenance(boolean notifyMaintenance) {
        this.notifyMaintenance = notifyMaintenance;
    }
    
    public boolean isNotifyNewFeatures() {
        return notifyNewFeatures;
    }
    
    public void setNotifyNewFeatures(boolean notifyNewFeatures) {
        this.notifyNewFeatures = notifyNewFeatures;
    }
    
    public boolean isAllDisabled() {
        return allDisabled;
    }
    
    public void setAllDisabled(boolean allDisabled) {
        this.allDisabled = allDisabled;
    }
}


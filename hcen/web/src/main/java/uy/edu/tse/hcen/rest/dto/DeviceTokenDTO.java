package uy.edu.tse.hcen.rest.dto;

/**
 * DTO para registrar o actualizar el device token de Firebase de un usuario.
 */
public class DeviceTokenDTO {
    
    private String deviceToken;
    
    public DeviceTokenDTO() {
    }
    
    public DeviceTokenDTO(String deviceToken) {
        this.deviceToken = deviceToken;
    }
    
    public String getDeviceToken() {
        return deviceToken;
    }
    
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}


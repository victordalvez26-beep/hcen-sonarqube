package uy.edu.tse.hcen.rest.dto;

/**
 * Respuesta del HCEN al registrar un Usuario de Salud.
 */
public class HcenUserResponse {
    
    private Long userId;
    private String mensaje;
    
    // Constructors
    public HcenUserResponse() {
    }
    
    public HcenUserResponse(Long userId, String mensaje) {
        this.userId = userId;
        this.mensaje = mensaje;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    @Override
    public String toString() {
        return "HcenUserResponse{" +
                "userId=" + userId +
                ", mensaje='" + mensaje + '\'' +
                '}';
    }
}


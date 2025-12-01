package uy.edu.tse.hcen.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Token temporal de autenticación para intercambio seguro.
 * Se usa para pasar el JWT del backend al frontend de forma segura.
 * El token expira en 60 segundos y es de un solo uso.
 */
@Entity
@Table(name = "auth_tokens")
public class AuthToken implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;
    
    @Column(name = "jwt_token", nullable = false, columnDefinition = "TEXT")
    private String jwtToken;
    
    @Column(name = "user_uid", nullable = false, length = 255)
    private String userUid;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "used", nullable = false)
    private boolean used = false;
    
    public AuthToken() {
        this.createdAt = LocalDateTime.now();
    }
    
    public AuthToken(String token, String jwtToken, String userUid, LocalDateTime expiresAt) {
        this();
        this.token = token;
        this.jwtToken = jwtToken;
        this.userUid = userUid;
        this.expiresAt = expiresAt;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getJwtToken() {
        return jwtToken;
    }
    
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
    
    public String getUserUid() {
        return userUid;
    }
    
    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public boolean isUsed() {
        return used;
    }
    
    public void setUsed(boolean used) {
        this.used = used;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !used && !isExpired();
    }
}


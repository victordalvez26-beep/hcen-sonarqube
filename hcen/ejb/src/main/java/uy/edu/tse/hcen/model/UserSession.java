package uy.edu.tse.hcen.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_uid", nullable = false, length = 255)
    private String userUid;
    
    @Column(name = "jwt_token", nullable = false, columnDefinition = "TEXT", unique = true)
    private String jwtToken;
    
    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;
    
    @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
    private String refreshToken;
    
    @Column(name = "id_token", columnDefinition = "TEXT")
    private String idToken;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    public UserSession() {
        this.createdAt = LocalDateTime.now();
    }
    
    public UserSession(String userUid, String jwtToken, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this();
        this.userUid = userUid;
        this.jwtToken = jwtToken;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserUid() {
        return userUid;
    }
    
    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
    
    public String getJwtToken() {
        return jwtToken;
    }
    
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getIdToken() {
        return idToken;
    }
    
    public void setIdToken(String idToken) {
        this.idToken = idToken;
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
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}


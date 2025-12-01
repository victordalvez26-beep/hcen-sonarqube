package uy.edu.tse.hcen.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la asociación entre un Usuario de Salud (paciente)
 * y las clínicas en las que está registrado.
 * Un mismo paciente puede estar asociado a múltiples clínicas.
 */
@Entity
@Table(name = "user_clinic_association",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_user_clinic",
           columnNames = {"user_id", "clinic_tenant_id"}
       ))
public class UserClinicAssociation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "clinic_tenant_id", nullable = false)
    private Long clinicTenantId;
    
    @Column(name = "fecha_alta")
    private LocalDateTime fechaAlta;
    
    // Constructors
    public UserClinicAssociation() {
    }
    
    public UserClinicAssociation(Long userId, Long clinicTenantId) {
        this.userId = userId;
        this.clinicTenantId = clinicTenantId;
        this.fechaAlta = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getClinicTenantId() {
        return clinicTenantId;
    }
    
    public void setClinicTenantId(Long clinicTenantId) {
        this.clinicTenantId = clinicTenantId;
    }
    
    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }
    
    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
    
    @Override
    public String toString() {
        return "UserClinicAssociation{" +
                "id=" + id +
                ", userId=" + userId +
                ", clinicTenantId=" + clinicTenantId +
                ", fechaAlta=" + fechaAlta +
                '}';
    }
}


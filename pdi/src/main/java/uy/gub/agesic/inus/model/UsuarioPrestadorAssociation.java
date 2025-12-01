package uy.gub.agesic.inus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la asociación entre un Usuario de Salud (paciente)
 * y un Prestador de Salud en el INUS.
 * 
 * Similar a UserClinicAssociation en HCEN, pero para prestadores externos.
 * Un mismo paciente puede estar asociado a múltiples prestadores.
 * 
 * Referencia: https://arquitecturadegobierno.agesic.gub.uy/docs/salud/modelos-referencia/arquitectura-negocio/servicios-hcen
 */
@Entity
@Table(name = "usuario_prestador_associations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_uid", "prestador_id"}))
public class UsuarioPrestadorAssociation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "usuario_uid", nullable = false)
    private String usuarioUid;
    
    @Column(name = "prestador_id", nullable = false)
    private Long prestadorId;
    
    @Column(name = "prestador_rut")
    private String prestadorRut;
    
    @Column(name = "fecha_alta")
    private LocalDateTime fechaAlta;
    
    // Constructors
    public UsuarioPrestadorAssociation() {
    }
    
    public UsuarioPrestadorAssociation(String usuarioUid, Long prestadorId, String prestadorRut) {
        this.usuarioUid = usuarioUid;
        this.prestadorId = prestadorId;
        this.prestadorRut = prestadorRut;
        this.fechaAlta = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsuarioUid() {
        return usuarioUid;
    }
    
    public void setUsuarioUid(String usuarioUid) {
        this.usuarioUid = usuarioUid;
    }
    
    public Long getPrestadorId() {
        return prestadorId;
    }
    
    public void setPrestadorId(Long prestadorId) {
        this.prestadorId = prestadorId;
    }
    
    public String getPrestadorRut() {
        return prestadorRut;
    }
    
    public void setPrestadorRut(String prestadorRut) {
        this.prestadorRut = prestadorRut;
    }
    
    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }
    
    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
    
    @Override
    public String toString() {
        return "UsuarioPrestadorAssociation{" +
                "id=" + id +
                ", usuarioUid='" + usuarioUid + '\'' +
                ", prestadorId=" + prestadorId +
                ", prestadorRut='" + prestadorRut + '\'' +
                ", fechaAlta=" + fechaAlta +
                '}';
    }
}
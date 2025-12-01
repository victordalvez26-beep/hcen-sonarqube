package uy.edu.tse.hcen.politicas.model;

import jakarta.persistence.*;

import java.util.Date;

/**
 * Entidad que registra cada acceso a documentos clínicos.
 * Se crea un registro cada vez que un profesional accede a un documento.
 */
@Entity
@Table(name = "registro_acceso")
public class RegistroAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Column(length = 1000)
    private String referencia;

    @Column(name = "profesional_id", nullable = false, length = 100)
    private String profesionalId;

    @Column(name = "cod_docum_paciente", nullable = false, length = 50)
    private String codDocumPaciente;

    @Column(name = "documento_id", length = 100)
    private String documentoId;

    @Column(name = "tipo_documento", length = 100)
    private String tipoDocumento;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "exito", nullable = false)
    private Boolean exito = true;

    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    @Column(name = "clinica_id", length = 100)
    private String clinicaId; // ID del tenant/clínica del profesional

    @Column(name = "nombre_profesional", length = 255)
    private String nombreProfesional; // Nombre completo del profesional

    @Column(name = "especialidad", length = 100)
    private String especialidad; // Especialidad del profesional

    public RegistroAcceso() {
        this.fecha = new Date();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public String getProfesionalId() { return profesionalId; }
    public void setProfesionalId(String profesionalId) { this.profesionalId = profesionalId; }

    public String getCodDocumPaciente() { return codDocumPaciente; }
    public void setCodDocumPaciente(String codDocumPaciente) { this.codDocumPaciente = codDocumPaciente; }

    public String getDocumentoId() { return documentoId; }
    public void setDocumentoId(String documentoId) { this.documentoId = documentoId; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Boolean getExito() { return exito; }
    public void setExito(Boolean exito) { this.exito = exito; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public String getClinicaId() { return clinicaId; }
    public void setClinicaId(String clinicaId) { this.clinicaId = clinicaId; }

    public String getNombreProfesional() { return nombreProfesional; }
    public void setNombreProfesional(String nombreProfesional) { this.nombreProfesional = nombreProfesional; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}











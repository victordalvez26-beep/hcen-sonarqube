package uy.edu.tse.hcen.rndc.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.LocalDateTime;
import uy.edu.tse.hcen.common.enumerations.FormatoDocumento;
import uy.edu.tse.hcen.common.enumerations.TipoDocumentoClinico;

/**
 * Entidad JPA que representa los metadatos de un documento clínico.
 */
@Entity
@Table(
    name = "metadata_documento",
    indexes = {
        @Index(name = "idx_cod_docum", columnList = "cod_docum"),
        @Index(name = "idx_nombre_paciente", columnList = "nombre_paciente"),
        @Index(name = "idx_fecha_creacion", columnList = "fecha_creacion"),
        @Index(name = "idx_clinica_origen", columnList = "clinica_origen"),
        @Index(name = "idx_restringido", columnList = "restringido"),
        @Index(name = "idx_cod_docum_fecha", columnList = "cod_docum, fecha_creacion DESC")
    }
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataDocumento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "cod_docum", nullable = false, length = 20)
    private String codDocum;

    @Column(name = "nombre_paciente", nullable = false, length = 200)
    private String nombrePaciente;
    
    @Column(name = "apellido_paciente", nullable = true, length = 200)
    private String apellidoPaciente;

    @Enumerated(EnumType.STRING)
    @Column(name = "formato_documento", nullable = false, length = 20)
    private FormatoDocumento formatoDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 50)
    private TipoDocumentoClinico tipoDocumento;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "uri_documento", nullable = false, length = 500)
    private String uriDocumento;

    @Column(name = "clinica_origen", nullable = false, length = 200)
    private String clinicaOrigen;

    @Column(name = "tenant_id", nullable = true)
    private Long tenantId;

    @Column(name = "profesional_salud", nullable = true, length = 200)
    private String profesionalSalud;

    @Column(name = "descripcion", nullable = true, length = 1000)
    private String descripcion;

    @Column(name = "restringido", nullable = false)
    private boolean restringido;

    public MetadataDocumento() {
    }

    public MetadataDocumento(String codDocum, String nombrePaciente, FormatoDocumento formatoDocumento,
                             TipoDocumentoClinico tipoDocumento, LocalDateTime fechaCreacion,
                             String uriDocumento, String clinicaOrigen, boolean restringido) {
        this.codDocum = codDocum;
        this.nombrePaciente = nombrePaciente;
        this.formatoDocumento = formatoDocumento;
        this.tipoDocumento = tipoDocumento;
        this.fechaCreacion = fechaCreacion;
        this.uriDocumento = uriDocumento;
        this.clinicaOrigen = clinicaOrigen;
        this.restringido = restringido;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodDocum() { return codDocum; }
    public void setCodDocum(String codDocum) { this.codDocum = codDocum; }
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    public String getApellidoPaciente() { return apellidoPaciente; }
    public void setApellidoPaciente(String apellidoPaciente) { this.apellidoPaciente = apellidoPaciente; }
    public FormatoDocumento getFormatoDocumento() { return formatoDocumento; }
    public void setFormatoDocumento(FormatoDocumento formatoDocumento) { this.formatoDocumento = formatoDocumento; }
    public TipoDocumentoClinico getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumentoClinico tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getUriDocumento() { return uriDocumento; }
    public void setUriDocumento(String uriDocumento) { this.uriDocumento = uriDocumento; }
    public String getClinicaOrigen() { return clinicaOrigen; }
    public void setClinicaOrigen(String clinicaOrigen) { this.clinicaOrigen = clinicaOrigen; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getProfesionalSalud() { return profesionalSalud; }
    public void setProfesionalSalud(String profesionalSalud) { this.profesionalSalud = profesionalSalud; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isRestringido() { return restringido; }
    public void setRestringido(boolean restringido) { this.restringido = restringido; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetadataDocumento)) return false;
        MetadataDocumento that = (MetadataDocumento) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "MetadataDocumento{" +
                "id=" + id +
                ", codDocum='" + codDocum + '\'' +
                ", nombrePaciente='" + nombrePaciente + '\'' +
                ", formatoDocumento=" + formatoDocumento +
                ", tipoDocumento=" + tipoDocumento +
                ", fechaCreacion=" + fechaCreacion +
                ", clinicaOrigen='" + clinicaOrigen + '\'' +
                ", restringido=" + restringido +
                '}';
    }
}


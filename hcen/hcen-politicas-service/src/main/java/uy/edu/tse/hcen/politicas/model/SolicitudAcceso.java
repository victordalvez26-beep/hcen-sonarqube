package uy.edu.tse.hcen.politicas.model;

import jakarta.persistence.*;
import uy.edu.tse.hcen.common.enumerations.EstadoSolicitudAcceso;

import java.util.Date;

/**
 * Entidad que representa una solicitud de acceso a documentos clínicos.
 * Los profesionales solicitan acceso a documentos de pacientes, y los pacientes pueden aprobar o rechazar.
 */
@Entity
@Table(name = "solicitud_acceso")
public class SolicitudAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_solicitud", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitudAcceso estado;

    @Column(name = "solicitante_id", nullable = false, length = 100)
    private String solicitanteId;

    @Column(length = 100)
    private String especialidad;

    @Column(name = "cod_docum_paciente", nullable = false, length = 50)
    private String codDocumPaciente;

    @Column(name = "tipo_documento", length = 100)
    private String tipoDocumento;

    @Column(name = "documento_id", length = 100)
    private String documentoId;

    @Column(name = "razon_solicitud", length = 1000)
    private String razonSolicitud;

    @Column(name = "fecha_resolucion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaResolucion;

    @Column(name = "resolucion_comentario", length = 1000)
    private String resolucionComentario;

    @Column(name = "resuelto_por", length = 100)
    private String resueltoPor;

    @Column(name = "clinica_autorizada", length = 100)
    private String clinicaAutorizada; // ID de la clínica (tenantId) del profesional que solicitó acceso - mismo campo que PoliticaAcceso

    public SolicitudAcceso() {
        this.fechaSolicitud = new Date();
        this.estado = EstadoSolicitudAcceso.PENDIENTE;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Date fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public EstadoSolicitudAcceso getEstado() { return estado; }
    public void setEstado(EstadoSolicitudAcceso estado) { this.estado = estado; }

    public String getSolicitanteId() { return solicitanteId; }
    public void setSolicitanteId(String solicitanteId) { this.solicitanteId = solicitanteId; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getCodDocumPaciente() { return codDocumPaciente; }
    public void setCodDocumPaciente(String codDocumPaciente) { this.codDocumPaciente = codDocumPaciente; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getDocumentoId() { return documentoId; }
    public void setDocumentoId(String documentoId) { this.documentoId = documentoId; }

    public String getRazonSolicitud() { return razonSolicitud; }
    public void setRazonSolicitud(String razonSolicitud) { this.razonSolicitud = razonSolicitud; }

    public Date getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Date fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public String getResolucionComentario() { return resolucionComentario; }
    public void setResolucionComentario(String resolucionComentario) { this.resolucionComentario = resolucionComentario; }

    public String getResueltoPor() { return resueltoPor; }
    public void setResueltoPor(String resueltoPor) { this.resueltoPor = resueltoPor; }

    public String getClinicaAutorizada() { return clinicaAutorizada; }
    public void setClinicaAutorizada(String clinicaAutorizada) { this.clinicaAutorizada = clinicaAutorizada; }
}











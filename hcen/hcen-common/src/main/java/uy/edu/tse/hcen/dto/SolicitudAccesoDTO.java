package uy.edu.tse.hcen.dto;

import uy.edu.tse.hcen.common.enumerations.EstadoSolicitudAcceso;
import java.io.Serializable;
import java.util.Date;

/**
 * DTO para transferir datos de solicitudes de acceso a documentos clínicos.
 */
public class SolicitudAccesoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Date fechaSolicitud;
    private EstadoSolicitudAcceso estado;
    private String solicitanteId;
    private String especialidad;
    private String codDocumPaciente;
    private String tipoDocumento;
    private Date fechaResolucion;

    public SolicitudAccesoDTO() {}

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
    
    public Date getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Date fechaResolucion) { this.fechaResolucion = fechaResolucion; }
}

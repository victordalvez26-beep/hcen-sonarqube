package uy.edu.tse.hcen.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO para transferir datos de registros de acceso a historias clínicas.
 */
public class RegistroAccesoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Date fecha;
    private String referencia;
    private String profesionalId;
    private String codDocumPaciente;
    private String documentoId;
    private String tipoDocumento;
    private String ipAddress;
    private String userAgent;
    private Boolean exito;
    private String motivoRechazo;
    private String clinicaId; // ID del tenant/clínica del profesional
    private String nombreProfesional; // Nombre completo del profesional
    private String especialidad; // Especialidad del profesional
    private String nombreClinica; // Nombre de la clínica (obtenido por consulta, no almacenado)

    public RegistroAccesoDTO() {}

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

    public String getNombreClinica() { return nombreClinica; }
    public void setNombreClinica(String nombreClinica) { this.nombreClinica = nombreClinica; }
}

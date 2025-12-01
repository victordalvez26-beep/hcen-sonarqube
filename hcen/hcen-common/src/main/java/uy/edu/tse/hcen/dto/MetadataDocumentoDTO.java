package uy.edu.tse.hcen.dto;

import java.io.Serializable;

/**
 * DTO (Data Transfer Object) para transferir datos de metadatos de documentos clínicos
 * entre las capas de la aplicación.
 * 
 */
public class MetadataDocumentoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id; // ID del documento en la base de datos
    private String codDocum; // Documento de identidad (CI, Paciente, etc.)
    private String nombrePaciente; 
    private String apellidoPaciente;
    private String formatoDocumento; 
    private String tipoDocumento; 
    private String fechaCreacion;
    private String uriDocumento; // URL para obtener el PDF/documento desde el Nodo Periférico
    private String clinicaOrigen; // Nombre de la clínica que generó el documento
    private Long tenantId; // ID del tenant (clínica) que generó el documento
    private String profesionalSalud; // Profesional que creó el documento
    private String descripcion; // Descripción del documento
    private boolean accesoPermitido; // Indica si el profesional tiene permiso de acceso

    public MetadataDocumentoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodDocum() { return codDocum; }
    public void setCodDocum(String codDocum) { this.codDocum = codDocum; }
    
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public String getUriDocumento() { return uriDocumento; }
    public void setUriDocumento(String uriDocumento) { this.uriDocumento = uriDocumento; }
    
    public String getClinicaOrigen() { return clinicaOrigen; }
    public void setClinicaOrigen(String clinicaOrigen) { this.clinicaOrigen = clinicaOrigen; }
    
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    
    public String getFormatoDocumento() { return formatoDocumento; }
    public void setFormatoDocumento(String formatoDocumento) { this.formatoDocumento = formatoDocumento; }
    
    public String getProfesionalSalud() { return profesionalSalud; }
    public void setProfesionalSalud(String profesionalSalud) { this.profesionalSalud = profesionalSalud; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getApellidoPaciente() { return apellidoPaciente; }
    public void setApellidoPaciente(String apellidoPaciente) { this.apellidoPaciente = apellidoPaciente; }
    
    public boolean isAccesoPermitido() { return accesoPermitido; }
    public void setAccesoPermitido(boolean accesoPermitido) { this.accesoPermitido = accesoPermitido; }
    
    // Alias para compatibilidad
    public String getCodDocumPaciente() { return codDocum; }
    public boolean isRestringido() { return !accesoPermitido; }
    public void setRestringido(boolean restringido) { this.accesoPermitido = !restringido; }
}

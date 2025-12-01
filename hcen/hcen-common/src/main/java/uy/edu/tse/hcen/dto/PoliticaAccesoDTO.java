package uy.edu.tse.hcen.dto;

import uy.edu.tse.hcen.common.enumerations.AlcancePoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.DuracionPoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.TipoGestionAcceso;
import java.io.Serializable;
import java.util.Date;

/**
 * DTO para transferir datos de políticas de acceso a documentos clínicos.
 */
public class PoliticaAccesoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private AlcancePoliticaAcceso alcance;
    private DuracionPoliticaAcceso duracion;
    private TipoGestionAcceso gestion;
    private Date fechaCreacion;
    private String referencia;
    private String codDocumPaciente;
    private String profesionalAutorizado;
    private String clinicaAutorizada;
    private String especialidadesAutorizadas; // JSON array o lista de especialidades
    private String tipoDocumento;
    private Date fechaVencimiento;
    private Boolean activa;

    public PoliticaAccesoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public AlcancePoliticaAcceso getAlcance() { return alcance; }
    public void setAlcance(AlcancePoliticaAcceso alcance) { this.alcance = alcance; }
    
    public DuracionPoliticaAcceso getDuracion() { return duracion; }
    public void setDuracion(DuracionPoliticaAcceso duracion) { this.duracion = duracion; }
    
    public TipoGestionAcceso getGestion() { return gestion; }
    public void setGestion(TipoGestionAcceso gestion) { this.gestion = gestion; }
    
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    
    public String getCodDocumPaciente() { return codDocumPaciente; }
    public void setCodDocumPaciente(String codDocumPaciente) { this.codDocumPaciente = codDocumPaciente; }
    
    public String getProfesionalAutorizado() { return profesionalAutorizado; }
    public void setProfesionalAutorizado(String profesionalAutorizado) { this.profesionalAutorizado = profesionalAutorizado; }
    
    public String getClinicaAutorizada() { return clinicaAutorizada; }
    public void setClinicaAutorizada(String clinicaAutorizada) { this.clinicaAutorizada = clinicaAutorizada; }
    
    public String getEspecialidadesAutorizadas() { return especialidadesAutorizadas; }
    public void setEspecialidadesAutorizadas(String especialidadesAutorizadas) { this.especialidadesAutorizadas = especialidadesAutorizadas; }
    
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    
    public Date getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}

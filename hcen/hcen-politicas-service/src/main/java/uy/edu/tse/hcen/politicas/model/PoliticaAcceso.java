package uy.edu.tse.hcen.politicas.model;

import jakarta.persistence.*;
import uy.edu.tse.hcen.common.enumerations.AlcancePoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.DuracionPoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.TipoGestionAcceso;

import java.util.Date;

/**
 * Entidad que representa una política de acceso a documentos clínicos.
 * Define qué profesionales pueden acceder a qué documentos de qué pacientes.
 */
@Entity
@Table(name = "politica_acceso")
public class PoliticaAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlcancePoliticaAcceso alcance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DuracionPoliticaAcceso duracion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoGestionAcceso gestion;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(length = 500)
    private String referencia;

    @Column(name = "cod_docum_paciente", nullable = false, length = 50)
    private String codDocumPaciente;

    @Column(name = "profesional_autorizado", nullable = false, length = 100)
    private String profesionalAutorizado;

    @Column(name = "tipo_documento", length = 100)
    private String tipoDocumento;

    @Column(name = "clinica_autorizada", length = 100)
    private String clinicaAutorizada;

    @Column(name = "especialidades_autorizadas", length = 1000)
    private String especialidadesAutorizadas; // JSON array o comma-separated: ["CARDIOLOGIA","PEDIATRIA"] o "CARDIOLOGIA,PEDIATRIA"

    @Column(name = "fecha_vencimiento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVencimiento;

    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    public PoliticaAcceso() {
        this.fechaCreacion = new Date();
    }

    // Getters y Setters
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

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getClinicaAutorizada() { return clinicaAutorizada; }
    public void setClinicaAutorizada(String clinicaAutorizada) { this.clinicaAutorizada = clinicaAutorizada; }

    public String getEspecialidadesAutorizadas() { return especialidadesAutorizadas; }
    public void setEspecialidadesAutorizadas(String especialidadesAutorizadas) { this.especialidadesAutorizadas = especialidadesAutorizadas; }

    public Date getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    /**
     * Verifica si la política está vigente (activa y no expirada).
     */
    public boolean estaVigente() {
        if (!activa) {
            return false;
        }
        if (fechaVencimiento != null && fechaVencimiento.before(new Date())) {
            return false;
        }
        return true;
    }
}







package uy.edu.tse.hcen.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Prestador de Salud (sociedad médica, laboratorio, etc.)
 * registrado en la plataforma HCEN.
 * 
 * A diferencia de las clínicas multi-tenant, los prestadores:
 * - Hospedan su propia infraestructura (URL propia)
 * - Brindan interfaz de servicios para obtener documentos clínicos
 * - Se registran mediante invitación + auto-registro
 */
@Entity
@Table(name = "prestador_salud")
public class PrestadorSalud {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 255, nullable = false)
    private String nombre;
    
    @Column(length = 20, unique = true)
    private String rut;
    
    @Column(length = 255)
    private String contacto;  // Email de contacto
    
    @Column(length = 512)
    private String url;  // URL del servidor del prestador (provista por ellos)
    
    @Convert(converter = uy.edu.tse.hcen.converter.DepartamentoConverter.class)
    @Column(length = 50)
    private Departamento departamento;
    
    @Column(length = 100)
    private String localidad;
    
    @Column(length = 255)
    private String direccion;
    
    @Column(length = 50)
    private String telefono;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private EstadoPrestador estado;
    
    @Column(name = "fecha_alta")
    private LocalDateTime fechaAlta;
    
    // Campos para flujo de activación
    @Column(name = "invitation_token", length = 255)
    private String invitationToken;
    
    @Column(name = "invitation_url", length = 512)
    private String invitationUrl;
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    
    // API Key para autenticación de servicios
    @Column(name = "api_key", length = 255, unique = true)
    private String apiKey;
    
    // Constructors
    public PrestadorSalud() {
    }
    
    public PrestadorSalud(String nombre, String contacto) {
        this.nombre = nombre;
        this.contacto = contacto;
        this.estado = EstadoPrestador.PENDIENTE_REGISTRO;
        this.fechaAlta = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getRut() {
        return rut;
    }
    
    public void setRut(String rut) {
        this.rut = rut;
    }
    
    public String getContacto() {
        return contacto;
    }
    
    public void setContacto(String contacto) {
        this.contacto = contacto;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Departamento getDepartamento() {
        return departamento;
    }
    
    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }
    
    public String getLocalidad() {
        return localidad;
    }
    
    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public EstadoPrestador getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoPrestador estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }
    
    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
    
    public String getInvitationToken() {
        return invitationToken;
    }
    
    public void setInvitationToken(String invitationToken) {
        this.invitationToken = invitationToken;
    }
    
    public String getInvitationUrl() {
        return invitationUrl;
    }
    
    public void setInvitationUrl(String invitationUrl) {
        this.invitationUrl = invitationUrl;
    }
    
    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }
    
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    @Override
    public String toString() {
        return "PrestadorSalud{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", rut='" + rut + '\'' +
                ", url='" + url + '\'' +
                ", estado=" + estado +
                '}';
    }
}


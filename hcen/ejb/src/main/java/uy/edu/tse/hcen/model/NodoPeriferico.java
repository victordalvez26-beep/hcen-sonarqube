package uy.edu.tse.hcen.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
public class NodoPeriferico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true)
    private String RUT;

    @Enumerated(EnumType.STRING)
    private Departamento departamento;

    private String localidad;

    private String direccion;

    private String contacto;

    private String url;

    // --- Configuración Técnica del Nodo Periférico ---
    private String nodoPerifericoUrlBase; 
    private String nodoPerifericoUsuario; // Credenciales para el Componente Central

    @Transient
    private String nodoPerifericoPassword;

    // Datos persistidos de la contraseña segura
    private String passwordHash;
    private String passwordSalt;

    @Enumerated(EnumType.STRING)
    private EstadoNodoPeriferico estado;

    // Fecha de alta cuando el nodo es registrado en HCEN
    private OffsetDateTime fechaAlta;

    // --- Datos de Activación del Administrador de la Clínica ---
    @Column(name = "admin_nickname")
    private String adminNickname;  // ej: "admin_c123"
    
    @Column(name = "activation_url", length = 512)
    private String activationUrl;  // URL completa para activar cuenta
    
    @Column(name = "activation_token")
    private String activationToken;  // Token de activación (para referencia, no crítico guardarlo)
    
    @Column(name = "admin_email")
    private String adminEmail;  // Email donde se envió el link de activación

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

    public String getRUT() {
        return RUT;
    }

    public void setRUT(String rUT) {
        RUT = rUT;
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

    public String getNodoPerifericoUrlBase() {
        return nodoPerifericoUrlBase;
    }

    public void setNodoPerifericoUrlBase(String nodoPerifericoUrlBase) {
        this.nodoPerifericoUrlBase = nodoPerifericoUrlBase;
    }

    public String getNodoPerifericoUsuario() {
        return nodoPerifericoUsuario;
    }

    public void setNodoPerifericoUsuario(String nodoPerifericoUsuario) {
        this.nodoPerifericoUsuario = nodoPerifericoUsuario;
    }

    public String getNodoPerifericoPassword() {
        return nodoPerifericoPassword;
    }

    public void setNodoPerifericoPassword(String nodoPerifericoPassword) {
        this.nodoPerifericoPassword = nodoPerifericoPassword;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public EstadoNodoPeriferico getEstado() {
        return estado;
    }

    public void setEstado(EstadoNodoPeriferico estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(OffsetDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public String getAdminNickname() {
        return adminNickname;
    }

    public void setAdminNickname(String adminNickname) {
        this.adminNickname = adminNickname;
    }

    public String getActivationUrl() {
        return activationUrl;
    }

    public void setActivationUrl(String activationUrl) {
        this.activationUrl = activationUrl;
    }

    public String getActivationToken() {
        return activationToken;
    }

    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }
}

package uy.edu.tse.hcen.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NodoPerifericoDTO {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    // RUT es opcional al crear (se completa en el formulario de activación)
    @Size(max = 50)
    private String RUT;

    // Departamento es opcional al crear (se completa en el formulario de activación)
    private String departamento; 

    @Size(max = 100)
    private String localidad;

    @Size(max = 255)
    private String direccion;

    @Size(max = 100)
    private String contacto;

    @Size(max = 255)
    private String url;

    // --- Configuración Técnica del Nodo Periférico ---
    @Size(max = 255)
    private String nodoPerifericoUrlBase;

    @Size(max = 100)
    private String nodoPerifericoUsuario;

    @Size(max = 100)
    private String nodoPerifericoPassword;

    private String estado;

    // Fecha de alta en ISO-8601 (ej: 2023-08-14T12:34:56+00:00)
    private String fechaAlta;

    // --- Datos de Activación del Administrador ---
    private String adminNickname;
    private String activationUrl;
    private String activationToken;
    private String adminEmail;

    public NodoPerifericoDTO() {
    }

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

    public void setRUT(String RUT) {
        this.RUT = RUT;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(String fechaAlta) {
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

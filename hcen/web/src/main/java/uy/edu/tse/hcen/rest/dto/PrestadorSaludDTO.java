package uy.edu.tse.hcen.rest.dto;

/**
 * DTO para transferir datos de Prestadores de Salud.
 */
public class PrestadorSaludDTO {
    
    private Long id;
    private String nombre;
    private String rut;
    private String contacto;
    private String url;
    private String departamento;
    private String localidad;
    private String direccion;
    private String telefono;
    private String estado;
    private String invitationUrl;
    private String apiKey;  // Solo se incluye al completar el registro
    
    // Constructors
    public PrestadorSaludDTO() {
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
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getInvitationUrl() {
        return invitationUrl;
    }
    
    public void setInvitationUrl(String invitationUrl) {
        this.invitationUrl = invitationUrl;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}


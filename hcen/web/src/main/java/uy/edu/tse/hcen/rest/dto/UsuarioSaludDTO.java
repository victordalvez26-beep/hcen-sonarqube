package uy.edu.tse.hcen.rest.dto;

import java.time.LocalDate;
import uy.edu.tse.hcen.model.Departamento;

/**
 * DTO para transferir datos de Usuarios de Salud (pacientes)
 * entre el nodo periférico y el componente central.
 */
public class UsuarioSaludDTO {
    
    private Long tenantId;        // ID de la clínica que registra al paciente
    private String ci;            // Documento de identidad (cédula)
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
    private String email;
    private Departamento departamento;
    private String localidad;
    
    // Constructors
    public UsuarioSaludDTO() {
    }
    
    // Getters and Setters
    public Long getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getCi() {
        return ci;
    }
    
    public void setCi(String ci) {
        this.ci = ci;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
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
    
    @Override
    public String toString() {
        return "UsuarioSaludDTO{" +
                "tenantId=" + tenantId +
                ", ci='" + ci + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", departamento=" + departamento +
                '}';
    }
}


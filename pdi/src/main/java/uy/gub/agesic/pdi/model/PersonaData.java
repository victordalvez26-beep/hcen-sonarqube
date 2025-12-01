package uy.gub.agesic.pdi.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Modelo de datos de una persona del Registro Civil
 * Representa la información que DNIC tiene de cada ciudadano
 */
public class PersonaData {
    
    private String tipoDocumento;      // CI, PASAPORTE, etc.
    private String numeroDocumento;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private LocalDate fechaNacimiento;
    private String sexo;               // M o F
    private String nacionalidad;       // Código: UY, AR, BR, CL, OT
    private String departamento;       // Solo para uruguayos
    private String localidad;          // Solo para uruguayos
    
    // Constructor vacío
    public PersonaData() {
    }
    
    // Constructor con todos los argumentos
    public PersonaData(String tipoDocumento, String numeroDocumento, String primerNombre, 
                      String segundoNombre, String primerApellido, String segundoApellido,
                      LocalDate fechaNacimiento, String sexo, String nacionalidad, 
                      String departamento, String localidad) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.nacionalidad = nacionalidad;
        this.departamento = departamento;
        this.localidad = localidad;
    }
    
    // Getters y Setters
    public String getTipoDocumento() {
        return tipoDocumento;
    }
    
    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    
    public String getNumeroDocumento() {
        return numeroDocumento;
    }
    
    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
    
    public String getPrimerNombre() {
        return primerNombre;
    }
    
    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }
    
    public String getSegundoNombre() {
        return segundoNombre;
    }
    
    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }
    
    public String getPrimerApellido() {
        return primerApellido;
    }
    
    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }
    
    public String getSegundoApellido() {
        return segundoApellido;
    }
    
    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }
    
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    
    public String getSexo() {
        return sexo;
    }
    
    public void setSexo(String sexo) {
        this.sexo = sexo;
    }
    
    public String getNacionalidad() {
        return nacionalidad;
    }
    
    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
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
    
    /**
     * Calcula la edad actual de la persona
     */
    public int getEdad() {
        if (fechaNacimiento == null) {
            return 0;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
    
    /**
     * Verifica si la persona es mayor de edad (>= 18 años)
     */
    public boolean isMayorDeEdad() {
        return getEdad() >= 18;
    }
}


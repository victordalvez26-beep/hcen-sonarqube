package uy.gub.agesic.inus.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Modelo de datos patronímicos de usuarios en INUS
 * Replica la estructura de User de HCEN
 */
@Entity
@Table(name = "inus_usuarios")
public class InusUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uid;

    private String email;

    @Column(name = "primer_nombre")
    private String primerNombre;

    @Column(name = "segundo_nombre")
    private String segundoNombre;

    @Column(name = "primer_apellido")
    private String primerApellido;

    @Column(name = "segundo_apellido")
    private String segundoApellido;

    @Column(name = "tip_docum")
    private String tipDocum;

    @Column(name = "cod_docum")
    private String codDocum;

    @Enumerated(EnumType.STRING)
    private Nacionalidad nacionalidad;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    private Departamento departamento;

    private String localidad;
    private String direccion;
    private String telefono;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    @Column(name = "profile_completed")
    private boolean profileCompleted = false;

    @Enumerated(EnumType.STRING)
    private Rol rol = Rol.getDefault();

    // Constructor vacío
    public InusUsuario() {
    }

    // Constructor con parámetros básicos
    public InusUsuario(String uid, String email, String primerNombre, String segundoNombre, 
                      String primerApellido, String segundoApellido, String tipDocum, 
                      String codDocum, Nacionalidad nacionalidad) {
        this.uid = uid;
        this.email = email;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.tipDocum = tipDocum;
        this.codDocum = codDocum;
        this.nacionalidad = nacionalidad;
        this.rol = Rol.getDefault();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getTipDocum() {
        return tipDocum;
    }

    public void setTipDocum(String tipDocum) {
        this.tipDocum = tipDocum;
    }

    public String getCodDocum() {
        return codDocum;
    }

    public void setCodDocum(String codDocum) {
        this.codDocum = codDocum;
    }

    public Nacionalidad getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(Nacionalidad nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
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

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
package uy.edu.tse.hcen.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uid", length = 255, unique = true, nullable = false)
	private String uid;
	
	@Column(name = "email", length = 255)
	private String email;
	
	@Column(name = "primer_nombre", length = 100)
	private String primerNombre;
	
	@Column(name = "segundo_nombre", length = 100)
	private String segundoNombre;
	
	@Column(name = "primer_apellido", length = 100)
	private String primerApellido;
	
	@Column(name = "segundo_apellido", length = 100)
	private String segundoApellido;
	
	@Column(name = "tipo_documento", length = 5)
	private String tipDocum;
	
	@Column(name = "codigo_documento", length = 20)
	private String codDocum;
	
	@Convert(converter = uy.edu.tse.hcen.converter.NacionalidadConverter.class)
	@Column(name = "nacionalidad", length = 2)
	private Nacionalidad nacionalidad;
	
	@Column(name = "fecha_nacimiento")
	@Temporal(TemporalType.DATE)
	private Date fechaNacimiento;
	
	@Convert(converter = uy.edu.tse.hcen.converter.DepartamentoConverter.class)
	@Column(name = "departamento", length = 50)
	private Departamento departamento;
	
	@Column(name = "localidad", length = 100)
	private String localidad;
	
	@Column(name = "direccion", length = 255)
	private String direccion;
	
	@Column(name = "telefono", length = 20)
	private String telefono;
	
	@Column(name = "codigo_postal", length = 10)
	private String codigoPostal;
	
	@Column(name = "profile_completed", nullable = false)
	private boolean profileCompleted = false;
	
	@Convert(converter = uy.edu.tse.hcen.converter.RolConverter.class)
	@Column(name = "rol", length = 2, nullable = false, updatable = false)
	private Rol rol = Rol.getDefault();
	
	public User() {
	}
	
	public User(String uid, String email, String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String tipDocum, String codDocum, Nacionalidad nacionalidad) {
		this.uid = uid;
		this.email = email;
		this.primerNombre = primerNombre;
		this.segundoNombre = segundoNombre;
		this.primerApellido = primerApellido;
		this.segundoApellido = segundoApellido;
		this.tipDocum = tipDocum;
		this.codDocum = codDocum;
		this.nacionalidad = nacionalidad;
		this.rol = Rol.getDefault(); // Todos los usuarios nuevos son usuarios de la salud
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
	
	public Date getFechaNacimiento() {
		return fechaNacimiento;
	}
	
	public void setFechaNacimiento(Date fechaNacimiento) {
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

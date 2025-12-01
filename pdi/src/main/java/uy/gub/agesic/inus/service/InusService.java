package uy.gub.agesic.inus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.gub.agesic.inus.model.*;
import uy.gub.agesic.inus.repository.InusUsuarioRepository;
import uy.gub.agesic.inus.repository.UsuarioPrestadorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para INUS.
 * Reemplaza al antiguo InusRepository en memoria.
 */
@Service
@Transactional
public class InusService {

    private static final Logger logger = LoggerFactory.getLogger(InusService.class);

    @Autowired
    private InusUsuarioRepository usuarioRepo;

    @Autowired
    private UsuarioPrestadorRepository asociacionRepo;

    @PostConstruct
    public void init() {
        if (usuarioRepo.count() == 0) {
            logger.info("Inicializando base de datos INUS con datos de prueba...");
            cargarDatosPrueba();
        }
    }

    private void cargarDatosPrueba() {
        // Usuario 1: Usuario completo uruguayo
        InusUsuario user1 = new InusUsuario(
            "uid-victor-50830691",
            "victor.alvez@example.com",
            "Victor",
            "David",
            "Alvez",
            "González",
            "CI",
            "50830691",
            Nacionalidad.UY
        );
        user1.setFechaNacimiento(LocalDate.of(2000, 12, 26));
        user1.setDepartamento(Departamento.MONTEVIDEO);
        user1.setLocalidad("Montevideo");
        user1.setDireccion("Av. 18 de Julio 1234");
        user1.setTelefono("099123456");
        user1.setCodigoPostal("11200");
        user1.setProfileCompleted(true);
        user1.setRol(Rol.USUARIO_SALUD);
        usuarioRepo.save(user1);

        // Usuario 2: Usuario extranjero
        InusUsuario user2 = new InusUsuario(
            "uid-roberto-26347848",
            "roberto.silva@example.com",
            "Roberto",
            null,
            "Silva",
            "Santos",
            "PASAPORTE",
            "26347848",
            Nacionalidad.BR
        );
        user2.setFechaNacimiento(LocalDate.of(1982, 4, 18));
        user2.setTelefono("+55 11 98765-4321");
        user2.setProfileCompleted(true);
        user2.setRol(Rol.USUARIO_SALUD);
        usuarioRepo.save(user2);

        // Usuario 3: Administrador
        InusUsuario user3 = new InusUsuario(
            "uid-admin-hcen",
            "admin@hcen.gub.uy",
            "María",
            "Laura",
            "González",
            "Pérez",
            "CI",
            "41500075",
            Nacionalidad.UY
        );
        user3.setFechaNacimiento(LocalDate.of(1985, 3, 15));
        user3.setDepartamento(Departamento.MONTEVIDEO);
        user3.setLocalidad("Montevideo");
        user3.setProfileCompleted(true);
        user3.setRol(Rol.ADMIN_HCEN);
        usuarioRepo.save(user3);

        // Asociaciones
        crearAsociacion("uid-victor-50830691", 1L, "12345678");
        crearAsociacion("uid-roberto-26347848", 2L, "87654321");
        
        logger.info("Datos de prueba INUS cargados.");
    }

    // Métodos de Usuario

    public Optional<InusUsuario> findByUid(String uid) {
        return usuarioRepo.findByUid(uid);
    }

    public Optional<InusUsuario> findByDocumento(String tipDocum, String codDocum) {
        return usuarioRepo.findByTipDocumAndCodDocum(tipDocum, codDocum);
    }
    
    public Optional<InusUsuario> findByTipDocumAndCodDocum(String tipDocum, String codDocum) {
        return findByDocumento(tipDocum, codDocum);
    }

    public Optional<InusUsuario> findByNombreApellidoAndFechaNacimiento(String primerNombre, String primerApellido, LocalDate fechaNacimiento) {
        return usuarioRepo.findByNombreApellidoFecha(primerNombre, primerApellido, fechaNacimiento);
    }

    public Optional<InusUsuario> findByNombreApellidoAndTelefono(String primerNombre, String primerApellido, String telefono) {
        return usuarioRepo.findByNombreApellidoTelefono(primerNombre, primerApellido, telefono);
    }

    public Optional<InusUsuario> findByNombreApellidoAndEmail(String primerNombre, String primerApellido, String email) {
        return usuarioRepo.findByNombreApellidoEmail(primerNombre, primerApellido, email);
    }

    public List<InusUsuario> findAll() {
        return usuarioRepo.findAll();
    }

    public InusUsuario save(InusUsuario usuario) {
        // Si tiene ID, es update, si no insert. 
        // Pero si viene con UID y no ID, buscamos primero.
        if (usuario.getId() == null && usuario.getUid() != null) {
            Optional<InusUsuario> existing = usuarioRepo.findByUid(usuario.getUid());
            if (existing.isPresent()) {
                usuario.setId(existing.get().getId());
            }
        }
        return usuarioRepo.save(usuario);
    }

    public Optional<InusUsuario> update(String uid, InusUsuario usuarioActualizado) {
        Optional<InusUsuario> existing = usuarioRepo.findByUid(uid);
        if (existing.isPresent()) {
            InusUsuario usuario = existing.get();
            
            updatePersonalData(usuario, usuarioActualizado);
            updateContactData(usuario, usuarioActualizado);
            updateSystemData(usuario, usuarioActualizado);
            
            return Optional.of(usuarioRepo.save(usuario));
        }
        return Optional.empty();
    }

    private void updatePersonalData(InusUsuario usuario, InusUsuario usuarioActualizado) {
        if (usuarioActualizado.getPrimerNombre() != null) usuario.setPrimerNombre(usuarioActualizado.getPrimerNombre());
        if (usuarioActualizado.getSegundoNombre() != null) usuario.setSegundoNombre(usuarioActualizado.getSegundoNombre());
        if (usuarioActualizado.getPrimerApellido() != null) usuario.setPrimerApellido(usuarioActualizado.getPrimerApellido());
        if (usuarioActualizado.getSegundoApellido() != null) usuario.setSegundoApellido(usuarioActualizado.getSegundoApellido());
        if (usuarioActualizado.getTipDocum() != null) usuario.setTipDocum(usuarioActualizado.getTipDocum());
        if (usuarioActualizado.getCodDocum() != null) usuario.setCodDocum(usuarioActualizado.getCodDocum());
        if (usuarioActualizado.getNacionalidad() != null) usuario.setNacionalidad(usuarioActualizado.getNacionalidad());
        if (usuarioActualizado.getFechaNacimiento() != null) usuario.setFechaNacimiento(usuarioActualizado.getFechaNacimiento());
    }

    private void updateContactData(InusUsuario usuario, InusUsuario usuarioActualizado) {
        if (usuarioActualizado.getEmail() != null) usuario.setEmail(usuarioActualizado.getEmail());
        if (usuarioActualizado.getDepartamento() != null) usuario.setDepartamento(usuarioActualizado.getDepartamento());
        if (usuarioActualizado.getLocalidad() != null) usuario.setLocalidad(usuarioActualizado.getLocalidad());
        if (usuarioActualizado.getDireccion() != null) usuario.setDireccion(usuarioActualizado.getDireccion());
        if (usuarioActualizado.getTelefono() != null) usuario.setTelefono(usuarioActualizado.getTelefono());
        if (usuarioActualizado.getCodigoPostal() != null) usuario.setCodigoPostal(usuarioActualizado.getCodigoPostal());
    }

    private void updateSystemData(InusUsuario usuario, InusUsuario usuarioActualizado) {
        if (usuarioActualizado.getRol() != null) usuario.setRol(usuarioActualizado.getRol());
        usuario.setProfileCompleted(usuarioActualizado.isProfileCompleted());
    }

    public boolean existsByUid(String uid) {
        return usuarioRepo.existsByUid(uid);
    }

    public boolean deleteByUid(String uid) {
        Optional<InusUsuario> user = usuarioRepo.findByUid(uid);
        if (user.isPresent()) {
            usuarioRepo.delete(user.get());
            return true;
        }
        return false;
    }

    // Métodos de Asociaciones

    public Optional<UsuarioPrestadorAssociation> findAsociacionByUsuarioYPrestador(String usuarioUid, Long prestadorId) {
        return asociacionRepo.findByUsuarioUidAndPrestadorId(usuarioUid, prestadorId);
    }

    public List<Long> findPrestadoresByUsuario(String usuarioUid) {
        return asociacionRepo.findByUsuarioUid(usuarioUid).stream()
                .map(UsuarioPrestadorAssociation::getPrestadorId)
                .collect(Collectors.toList());
    }

    public List<String> findUsuariosByPrestadorId(Long prestadorId) {
        return asociacionRepo.findByPrestadorId(prestadorId).stream()
                .map(UsuarioPrestadorAssociation::getUsuarioUid)
                .collect(Collectors.toList());
    }

    public List<String> findUsuariosByPrestadorRut(String prestadorRut) {
        return asociacionRepo.findByPrestadorRut(prestadorRut).stream()
                .map(UsuarioPrestadorAssociation::getUsuarioUid)
                .collect(Collectors.toList());
    }

    public List<InusUsuario> findUsuariosCompletosByPrestadorId(Long prestadorId) {
        List<String> uids = findUsuariosByPrestadorId(prestadorId);
        List<InusUsuario> resultado = new ArrayList<>();
        for (String uid : uids) {
            usuarioRepo.findByUid(uid).ifPresent(resultado::add);
        }
        return resultado;
    }

    public List<InusUsuario> findUsuariosCompletosByPrestadorRut(String prestadorRut) {
        List<String> uids = findUsuariosByPrestadorRut(prestadorRut);
        List<InusUsuario> resultado = new ArrayList<>();
        for (String uid : uids) {
            usuarioRepo.findByUid(uid).ifPresent(resultado::add);
        }
        return resultado;
    }

    public UsuarioPrestadorAssociation crearAsociacion(String usuarioUid, Long prestadorId, String prestadorRut) {
        Optional<UsuarioPrestadorAssociation> existente = asociacionRepo.findByUsuarioUidAndPrestadorId(usuarioUid, prestadorId);
        if (existente.isPresent()) {
            return existente.get();
        }
        
        UsuarioPrestadorAssociation asociacion = new UsuarioPrestadorAssociation(usuarioUid, prestadorId, prestadorRut);
        return asociacionRepo.save(asociacion);
    }

    public boolean eliminarAsociacion(String usuarioUid, Long prestadorId) {
        Optional<UsuarioPrestadorAssociation> asoc = asociacionRepo.findByUsuarioUidAndPrestadorId(usuarioUid, prestadorId);
        if (asoc.isPresent()) {
            asociacionRepo.delete(asoc.get());
            return true;
        }
        return false;
    }

    public List<UsuarioPrestadorAssociation> findAllAsociaciones() {
        return asociacionRepo.findAll();
    }
}

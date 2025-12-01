package uy.gub.agesic.inus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uy.gub.agesic.inus.model.InusUsuario;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface InusUsuarioRepository extends JpaRepository<InusUsuario, Long> {
    Optional<InusUsuario> findByUid(String uid);
    
    Optional<InusUsuario> findByTipDocumAndCodDocum(String tipDocum, String codDocum);
    
    @Query("SELECT u FROM InusUsuario u WHERE u.primerNombre = :nombre AND u.primerApellido = :apellido AND u.fechaNacimiento = :fecha")
    Optional<InusUsuario> findByNombreApellidoFecha(@Param("nombre") String nombre, @Param("apellido") String apellido, @Param("fecha") LocalDate fecha);

    @Query("SELECT u FROM InusUsuario u WHERE u.primerNombre = :nombre AND u.primerApellido = :apellido AND u.telefono = :telefono")
    Optional<InusUsuario> findByNombreApellidoTelefono(@Param("nombre") String nombre, @Param("apellido") String apellido, @Param("telefono") String telefono);

    @Query("SELECT u FROM InusUsuario u WHERE u.primerNombre = :nombre AND u.primerApellido = :apellido AND u.email = :email")
    Optional<InusUsuario> findByNombreApellidoEmail(@Param("nombre") String nombre, @Param("apellido") String apellido, @Param("email") String email);
    
    boolean existsByUid(String uid);
    
    void deleteByUid(String uid);
}
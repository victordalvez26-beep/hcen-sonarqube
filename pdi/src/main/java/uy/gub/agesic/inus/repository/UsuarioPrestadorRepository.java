package uy.gub.agesic.inus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uy.gub.agesic.inus.model.UsuarioPrestadorAssociation;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioPrestadorRepository extends JpaRepository<UsuarioPrestadorAssociation, Long> {
    Optional<UsuarioPrestadorAssociation> findByUsuarioUidAndPrestadorId(String usuarioUid, Long prestadorId);
    
    List<UsuarioPrestadorAssociation> findByUsuarioUid(String usuarioUid);
    
    List<UsuarioPrestadorAssociation> findByPrestadorId(Long prestadorId);
    
    List<UsuarioPrestadorAssociation> findByPrestadorRut(String prestadorRut);
    
    void deleteByUsuarioUidAndPrestadorId(String usuarioUid, Long prestadorId);
}
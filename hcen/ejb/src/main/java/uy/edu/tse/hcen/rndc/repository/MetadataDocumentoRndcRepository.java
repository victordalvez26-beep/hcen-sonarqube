package uy.edu.tse.hcen.rndc.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import uy.edu.tse.hcen.rndc.model.MetadataDocumento;

import java.util.List;
import java.util.logging.Logger;

/**
 * Repositorio JPA para gestionar MetadataDocumento.
 */
@ApplicationScoped
public class MetadataDocumentoRndcRepository {
    
    private static final Logger LOGGER = Logger.getLogger(MetadataDocumentoRndcRepository.class.getName());
    
    @PersistenceContext(unitName = "hcenPersistenceUnit")
    private EntityManager em;
    
    public List<MetadataDocumento> buscarPorCodDocum(String codDocum) {
        LOGGER.info("Buscando documentos por CI: " + codDocum);
        
        TypedQuery<MetadataDocumento> query = em.createQuery(
            "SELECT m FROM MetadataDocumento m WHERE m.codDocum = :codDocum ORDER BY m.fechaCreacion DESC",
            MetadataDocumento.class);
        query.setParameter("codDocum", codDocum);
        
        return query.getResultList();
    }
    
    public List<MetadataDocumento> buscarPorNombre(String nombrePaciente) {
        LOGGER.info("Buscando documentos por nombre: " + nombrePaciente);
        
        TypedQuery<MetadataDocumento> query = em.createQuery(
            "SELECT m FROM MetadataDocumento m WHERE LOWER(m.nombrePaciente) LIKE LOWER(:nombre) ORDER BY m.fechaCreacion DESC",
            MetadataDocumento.class);
        query.setParameter("nombre", "%" + nombrePaciente + "%");
        
        return query.getResultList();
    }
    
    public MetadataDocumento findById(Long id) {
        LOGGER.info("Buscando documento por ID: " + id);
        return em.find(MetadataDocumento.class, id);
    }
    
    @Transactional
    public MetadataDocumento save(MetadataDocumento metadata) {
        if (metadata.getId() == null) {
            LOGGER.info("Persistiendo nuevo documento");
            em.persist(metadata);
            return metadata;
        } else {
            LOGGER.info("Actualizando documento ID: " + metadata.getId());
            return em.merge(metadata);
        }
    }
    
    @Transactional
    public void deleteById(Long id) {
        LOGGER.info("Eliminando documento ID: " + id);
        MetadataDocumento metadata = findById(id);
        if (metadata != null) {
            em.remove(metadata);
        }
    }
    
    public List<MetadataDocumento> findAll() {
        LOGGER.info("Obteniendo todos los documentos");
        return em.createQuery("SELECT m FROM MetadataDocumento m ORDER BY m.fechaCreacion DESC", 
            MetadataDocumento.class)
            .getResultList();
    }
    
    public long count() {
        return em.createQuery("SELECT COUNT(m) FROM MetadataDocumento m", Long.class)
            .getSingleResult();
    }
}


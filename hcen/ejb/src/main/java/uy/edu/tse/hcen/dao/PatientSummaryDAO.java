package uy.edu.tse.hcen.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uy.edu.tse.hcen.model.PatientSummary;

import java.util.logging.Logger;

@Stateless
public class PatientSummaryDAO {
    
    private static final Logger LOGGER = Logger.getLogger(PatientSummaryDAO.class.getName());
    
    @PersistenceContext(unitName = "hcenPersistenceUnit")
    private EntityManager em;
    
    /**
     * Busca un resumen de paciente por UID.
     * @param uid El UID del paciente
     * @return PatientSummary o null si no existe
     */
    public PatientSummary findByUid(String uid) {
        try {
            return em.find(PatientSummary.class, uid);
        } catch (Exception e) {
            LOGGER.warning("Error buscando resumen de paciente por UID: " + uid + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Guarda o actualiza un resumen de paciente.
     * @param patientSummary El resumen a guardar o actualizar
     * @return PatientSummary guardado
     */
    public PatientSummary saveOrUpdate(PatientSummary patientSummary) {
        try {
            PatientSummary existing = findByUid(patientSummary.getUid());
            if (existing != null) {
                // Actualizar campos existentes
                existing.setAllergies(patientSummary.getAllergies());
                existing.setConditions(patientSummary.getConditions());
                existing.setMedications(patientSummary.getMedications());
                existing.setImmunizations(patientSummary.getImmunizations());
                existing.setObservations(patientSummary.getObservations());
                existing.setProcedures(patientSummary.getProcedures());
                PatientSummary updated = em.merge(existing);
                LOGGER.info("Resumen de paciente actualizado: " + patientSummary.getUid());
                return updated;
            } else {
                // Crear nuevo
                em.persist(patientSummary);
                LOGGER.info("Resumen de paciente creado: " + patientSummary.getUid());
                return patientSummary;
            }
        } catch (Exception e) {
            LOGGER.severe("Error guardando resumen de paciente: " + e.getMessage());
            throw new RuntimeException("Error guardando resumen de paciente", e);
        }
    }
    
    /**
     * Elimina un resumen de paciente por UID.
     * @param uid El UID del paciente
     * @return true si se eliminó, false si no existía
     */
    public boolean deleteByUid(String uid) {
        try {
            PatientSummary summary = findByUid(uid);
            if (summary != null) {
                em.remove(summary);
                LOGGER.info("Resumen de paciente eliminado: " + uid);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.severe("Error eliminando resumen de paciente: " + e.getMessage());
            return false;
        }
    }
}


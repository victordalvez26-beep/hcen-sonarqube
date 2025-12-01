package uy.edu.tse.hcen.politicas.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uy.edu.tse.hcen.politicas.model.PoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.AlcancePoliticaAcceso;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@Stateless
public class PoliticaAccesoRepository {

    private static final Logger LOGGER = Logger.getLogger(PoliticaAccesoRepository.class.getName());

    @PersistenceContext(unitName = "politicasPersistenceUnit")
    private EntityManager em;

    public PoliticaAcceso crear(PoliticaAcceso politica) {
        em.persist(politica);
        return politica;
    }

    public PoliticaAcceso buscarPorId(Long id) {
        return em.find(PoliticaAcceso.class, id);
    }

    public List<PoliticaAcceso> buscarPorPaciente(String codDocumPaciente) {
        LOGGER.info("🔍 [POLITICAS-REPO] Buscando políticas para paciente CI: '" + codDocumPaciente + "'");
        
        if (codDocumPaciente == null || codDocumPaciente.trim().isEmpty()) {
            LOGGER.warning("⚠️ [POLITICAS-REPO] CI es null o vacío");
            return new java.util.ArrayList<>();
        }
        
        String ciNormalizado = codDocumPaciente.trim();
        LOGGER.info("🔍 [POLITICAS-REPO] CI normalizado: '" + ciNormalizado + "'");
        
        List<PoliticaAcceso> politicas = em.createQuery(
            "SELECT p FROM PoliticaAcceso p WHERE p.codDocumPaciente = :codDocum AND p.activa = true",
            PoliticaAcceso.class
        )
        .setParameter("codDocum", ciNormalizado)
        .getResultList();
        
        LOGGER.info("🔍 [POLITICAS-REPO] Políticas encontradas para CI '" + ciNormalizado + "': " + politicas.size());
        
        if (!politicas.isEmpty()) {
            for (PoliticaAcceso p : politicas) {
                LOGGER.info("  ✅ Política encontrada - ID: " + p.getId() + 
                    ", Paciente CI: " + p.getCodDocumPaciente() + 
                    ", Clínica: " + p.getClinicaAutorizada() +
                    ", Activa: " + p.getActiva());
            }
        } else {
            // Si no se encontraron políticas, buscar todas las políticas activas para ver qué hay
            LOGGER.warning("⚠️ [POLITICAS-REPO] No se encontraron políticas para CI '" + ciNormalizado + "'. Buscando todas las políticas activas...");
            List<PoliticaAcceso> todasActivas = em.createQuery(
                "SELECT p FROM PoliticaAcceso p WHERE p.activa = true",
                PoliticaAcceso.class
            ).getResultList();
            LOGGER.warning("⚠️ [POLITICAS-REPO] Total de políticas activas en el sistema: " + todasActivas.size());
            for (PoliticaAcceso p : todasActivas) {
                String pacienteCI = p.getCodDocumPaciente() != null ? p.getCodDocumPaciente() : "NULL";
                boolean coincide = ciNormalizado.equals(pacienteCI);
                LOGGER.warning("  - Política activa - ID: " + p.getId() + 
                    ", Paciente CI: '" + pacienteCI + "'" +
                    ", Clínica: " + p.getClinicaAutorizada() +
                    ", Activa: " + p.getActiva() +
                    " (¿Coincide con '" + ciNormalizado + "'? " + coincide + ")");
            }
        }
        
        return politicas;
    }

    public List<PoliticaAcceso> buscarPorProfesional(String profesionalId) {
        LOGGER.info("🔍 [REPOSITORY] Buscando políticas para profesional: '" + profesionalId + "'");
        
        if (profesionalId == null || profesionalId.trim().isEmpty()) {
            LOGGER.warning("⚠️ [REPOSITORY] ProfesionalId es null o vacío");
            return new java.util.ArrayList<>();
        }
        
        // Normalizar el profesionalId (trim para eliminar espacios)
        String profIdNormalizado = profesionalId.trim();
        LOGGER.info("🔍 [REPOSITORY] ProfesionalId normalizado: '" + profIdNormalizado + "'");
        
        // Buscar políticas donde profesionalAutorizado coincida exactamente
        // Usar la misma lógica que verificarPermiso: p.activa = true (sin parámetro para evitar problemas con Boolean)
        Date ahora = new Date();
        LOGGER.info("🔍 [REPOSITORY] Ejecutando consulta para profesional: '" + profIdNormalizado + "', fecha actual: " + ahora);
        
        List<PoliticaAcceso> politicas = em.createQuery(
            "SELECT p FROM PoliticaAcceso p WHERE p.profesionalAutorizado = :profId AND p.activa = true AND (p.fechaVencimiento IS NULL OR p.fechaVencimiento >= :ahora)",
            PoliticaAcceso.class
        )
        .setParameter("profId", profIdNormalizado)
        .setParameter("ahora", ahora)
        .getResultList();
        
        LOGGER.info("🔍 [REPOSITORY] Consulta ejecutada. Resultados: " + politicas.size());
        
        LOGGER.info("🔍 [REPOSITORY] Políticas encontradas para profesional '" + profIdNormalizado + "': " + politicas.size());
        
        if (!politicas.isEmpty()) {
            for (PoliticaAcceso p : politicas) {
                LOGGER.info("  ✅ Política encontrada - ID: " + p.getId() + 
                    ", Paciente: " + p.getCodDocumPaciente() + 
                    ", ProfesionalAutorizado: '" + p.getProfesionalAutorizado() + "'" +
                    ", Activa: " + p.getActiva());
            }
        } else {
            // Si no se encontraron políticas, buscar todas las políticas activas para ver qué hay
            LOGGER.warning("⚠️ [REPOSITORY] No se encontraron políticas para profesional '" + profIdNormalizado + "'. Buscando todas las políticas activas...");
            List<PoliticaAcceso> todasActivas = em.createQuery(
                "SELECT p FROM PoliticaAcceso p WHERE p.activa = true OR p.activa IS NULL",
                PoliticaAcceso.class
            ).getResultList();
            LOGGER.warning("⚠️ [REPOSITORY] Total de políticas activas en el sistema: " + todasActivas.size());
            for (PoliticaAcceso p : todasActivas) {
                String profAuth = p.getProfesionalAutorizado() != null ? p.getProfesionalAutorizado() : "NULL";
                String profAuthTrimmed = profAuth != null && !profAuth.equals("NULL") ? profAuth.trim() : profAuth;
                boolean coincide = profIdNormalizado.equals(profAuthTrimmed);
                LOGGER.warning("  - Política activa - ID: " + p.getId() + 
                    ", Paciente: " + p.getCodDocumPaciente() + 
                    ", ProfesionalAutorizado: '" + profAuth + "'" +
                    ", Activa: " + p.getActiva() +
                    " (¿Coincide con '" + profIdNormalizado + "'? " + coincide + ")");
            }
        }
        
        return politicas;
    }

    public List<PoliticaAcceso> verificarPermiso(String profesionalId, String codDocumPaciente, String tipoDocumento) {
        return verificarPermiso(profesionalId, codDocumPaciente, tipoDocumento, null, null);
    }

    public List<PoliticaAcceso> verificarPermiso(String profesionalId, String codDocumPaciente, String tipoDocumento, String tenantIdProfesional) {
        return verificarPermiso(profesionalId, codDocumPaciente, tipoDocumento, tenantIdProfesional, null);
    }

    /**
     * Verifica permisos considerando clínica y especialidad del profesional.
     * @param profesionalId ID del profesional
     * @param codDocumPaciente CI del paciente
     * @param tipoDocumento Tipo de documento (opcional)
     * @param tenantIdProfesional ID de la clínica del profesional
     * @param especialidadProfesional Especialidad del profesional (opcional)
     * @return Lista de políticas aplicables
     */
    public List<PoliticaAcceso> verificarPermiso(String profesionalId, String codDocumPaciente, String tipoDocumento, String tenantIdProfesional, String especialidadProfesional) {
        Date ahora = new Date();
        
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Verificando permiso - Profesional: {0}, Paciente: {1}, TipoDocumento: {2}, TenantId: {3}, Especialidad: {4}",
                    new Object[]{profesionalId, codDocumPaciente, tipoDocumento, tenantIdProfesional, especialidadProfesional});
        }
        
        // Buscar políticas por clínica (clinicaAutorizada coincide con tenantIdProfesional)
        // Ya no buscamos por profesional específico, solo por clínica
        String queryStr = "SELECT p FROM PoliticaAcceso p WHERE " +
            "p.codDocumPaciente = :pacienteId AND " +
            "p.activa = true AND " +
            "(p.fechaVencimiento IS NULL OR p.fechaVencimiento >= :ahora)";
        
        // Solo buscar políticas por clínica si tenantIdProfesional no es null
        if (tenantIdProfesional != null && !tenantIdProfesional.isBlank()) {
            queryStr += " AND p.clinicaAutorizada = :tenantId";
        }
        // Si no hay tenantId, no filtramos por clínica (puede retornar lista vacía si no hay políticas sin clínica)
        
        // Condición para tipo de documento: 
        // - Si tipoDocumento es NULL, aceptar políticas con tipoDocumento NULL o alcance TODOS_LOS_DOCUMENTOS
        // - Si tipoDocumento tiene valor, aceptar políticas con:
        //   * tipoDocumento IS NULL (políticas globales)
        //   * tipoDocumento = :tipoDoc (coincidencia exacta, incluye UN_DOCUMENTO_ESPECIFICO con tipoDocumento coincidente)
        //   * alcance = TODOS_LOS_DOCUMENTOS (permite todos los tipos)
        // NOTA: UN_DOCUMENTO_ESPECIFICO solo se acepta si tipoDocumento coincide (ya cubierto por tipoDocumento = :tipoDoc)
        if (tipoDocumento == null || tipoDocumento.isBlank()) {
            queryStr += " AND " +
                "(p.tipoDocumento IS NULL OR p.alcance = :todos)";
        } else {
            queryStr += " AND " +
                "(p.tipoDocumento IS NULL OR p.tipoDocumento = :tipoDoc OR p.alcance = :todos)";
        }
        
        // Loggear query siempre para diagnóstico
        LOGGER.log(Level.INFO, "Query de verificación: {0} [Params: Paciente={1}, Tenant={2}, Tipo={3}]", 
            new Object[]{queryStr, codDocumPaciente, tenantIdProfesional, tipoDocumento});
        
        jakarta.persistence.TypedQuery<PoliticaAcceso> query = em.createQuery(queryStr, PoliticaAcceso.class)
            .setParameter("pacienteId", codDocumPaciente)
            .setParameter("todos", AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS)
            .setParameter("ahora", ahora);
        
        if (tipoDocumento != null && !tipoDocumento.isBlank()) {
            query.setParameter("tipoDoc", tipoDocumento);
        }
        
        if (tenantIdProfesional != null && !tenantIdProfesional.isBlank()) {
            query.setParameter("tenantId", tenantIdProfesional.trim());
        }
        
        List<PoliticaAcceso> politicas = query.getResultList();
        
        // Filtrar por especialidades si la política tiene especialidades especificadas
        if (especialidadProfesional != null && !especialidadProfesional.isBlank()) {
            politicas = politicas.stream()
                .filter(p -> {
                    String especialidades = p.getEspecialidadesAutorizadas();
                    // Si no hay especialidades especificadas, permitir a todos
                    if (especialidades == null || especialidades.isBlank() || especialidades.trim().isEmpty()) {
                        return true;
                    }
                    // Parsear especialidades (puede ser JSON array o comma-separated)
                    java.util.List<String> especialidadesList = parseEspecialidades(especialidades);
                    return especialidadesList.contains(especialidadProfesional.toUpperCase());
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Verificación de permiso - Profesional: {0}, Paciente: {1}, TipoDocumento: {2}, Políticas encontradas: {3}",
                    new Object[]{profesionalId, codDocumPaciente, tipoDocumento, politicas.size()});
            if (!politicas.isEmpty()) {
                for (PoliticaAcceso p : politicas) {
                    LOGGER.log(Level.INFO, "Política encontrada - ID: {0}, Alcance: {1}, TipoDocumento: {2}, ProfesionalAutorizado: {3}, Paciente: {4}",
                            new Object[]{p.getId(), p.getAlcance(), p.getTipoDocumento(), p.getProfesionalAutorizado(), p.getCodDocumPaciente()});
                }
            } else {
                // Si no se encontraron políticas, buscar todas las políticas para este profesional para ayudar en el diagnóstico
                List<PoliticaAcceso> politicasProfesional = buscarPorProfesional(profesionalId);
                if (!politicasProfesional.isEmpty()) {
                    LOGGER.log(Level.WARNING, "No se encontraron políticas para paciente {0}, pero el profesional {1} tiene {2} políticas para otros pacientes",
                            new Object[]{codDocumPaciente, profesionalId, politicasProfesional.size()});
                    for (PoliticaAcceso p : politicasProfesional) {
                        LOGGER.log(Level.WARNING, "Política del profesional - ID: {0}, Paciente: {1}, Alcance: {2}, TipoDocumento: {3}",
                                new Object[]{p.getId(), p.getCodDocumPaciente(), p.getAlcance(), p.getTipoDocumento()});
                    }
                } else {
                    LOGGER.log(Level.WARNING, "No se encontraron políticas para paciente {0} ni para el profesional {1}. Se requiere crear y aprobar una solicitud de acceso.",
                            new Object[]{codDocumPaciente, profesionalId});
                }
            }
        }
        
        return politicas;
    }
    
    /**
     * Parsea las especialidades desde un string (puede ser JSON array o comma-separated).
     * @param especialidadesStr String con especialidades
     * @return Lista de especialidades en mayúsculas
     */
    private java.util.List<String> parseEspecialidades(String especialidadesStr) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (especialidadesStr == null || especialidadesStr.isBlank()) {
            return result;
        }
        
        String trimmed = especialidadesStr.trim();
        
        // Intentar parsear como JSON array
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                // Simple JSON array parsing (sin dependencias externas)
                String content = trimmed.substring(1, trimmed.length() - 1).trim();
                if (!content.isEmpty()) {
                    String[] parts = content.split(",");
                    for (String part : parts) {
                        String cleaned = part.trim().replace("\"", "").replace("'", "");
                        if (!cleaned.isEmpty()) {
                            result.add(cleaned.toUpperCase());
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error parseando especialidades como JSON: {0}", especialidadesStr);
            }
        } else {
            // Parsear como comma-separated
            String[] parts = trimmed.split(",");
            for (String part : parts) {
                String cleaned = part.trim();
                if (!cleaned.isEmpty()) {
                    result.add(cleaned.toUpperCase());
                }
            }
        }
        
        return result;
    }

    public void eliminar(Long id) {
        PoliticaAcceso politica = buscarPorId(id);
        if (politica != null) {
            em.remove(politica);
        }
    }

    public PoliticaAcceso actualizar(PoliticaAcceso politica) {
        return em.merge(politica);
    }

    public List<PoliticaAcceso> listarTodas() {
        return em.createQuery("SELECT p FROM PoliticaAcceso p ORDER BY p.fechaCreacion DESC", PoliticaAcceso.class)
                .getResultList();
    }

    /**
     * Obtiene estadísticas de políticas creadas agrupadas por día en un rango de fechas.
     */
    public List<Object[]> obtenerPoliticasCreadasPorDia(Date fechaInicio, Date fechaFin) {
        return em.createQuery(
            "SELECT CAST(p.fechaCreacion AS DATE), COUNT(p) " +
            "FROM PoliticaAcceso p " +
            "WHERE p.fechaCreacion BETWEEN :inicio AND :fin " +
            "GROUP BY CAST(p.fechaCreacion AS DATE) " +
            "ORDER BY CAST(p.fechaCreacion AS DATE) ASC",
            Object[].class
        )
        .setParameter("inicio", fechaInicio)
        .setParameter("fin", fechaFin)
        .getResultList();
    }

    /**
     * Obtiene estadísticas de políticas agrupadas por alcance.
     */
    public List<Object[]> obtenerPoliticasPorAlcance() {
        return em.createQuery(
            "SELECT p.alcance, COUNT(p), " +
            "SUM(CASE WHEN p.activa = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.activa = false THEN 1 ELSE 0 END) " +
            "FROM PoliticaAcceso p " +
            "GROUP BY p.alcance",
            Object[].class
        )
        .getResultList();
    }

    /**
     * Obtiene estadísticas de políticas agrupadas por duración.
     */
    public List<Object[]> obtenerPoliticasPorDuracion() {
        return em.createQuery(
            "SELECT p.duracion, COUNT(p), " +
            "SUM(CASE WHEN p.activa = true THEN 1 ELSE 0 END) " +
            "FROM PoliticaAcceso p " +
            "GROUP BY p.duracion",
            Object[].class
        )
        .getResultList();
    }

    /**
     * Obtiene el total de políticas activas e inactivas.
     */
    public List<Object[]> obtenerResumenPoliticas() {
        return em.createQuery(
            "SELECT COUNT(p), " +
            "SUM(CASE WHEN p.activa = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.activa = false THEN 1 ELSE 0 END) " +
            "FROM PoliticaAcceso p",
            Object[].class
        )
        .getResultList();
    }
}


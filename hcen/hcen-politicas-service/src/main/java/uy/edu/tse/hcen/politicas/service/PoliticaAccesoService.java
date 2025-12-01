package uy.edu.tse.hcen.politicas.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.edu.tse.hcen.politicas.model.PoliticaAcceso;
import uy.edu.tse.hcen.politicas.repository.PoliticaAccesoRepository;
import uy.edu.tse.hcen.common.enumerations.AlcancePoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.DuracionPoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.TipoGestionAcceso;

import java.util.Date;
import java.util.List;

@Stateless
public class PoliticaAccesoService {

    @Inject
    private PoliticaAccesoRepository repository;

    public PoliticaAcceso crearPolitica(AlcancePoliticaAcceso alcance, DuracionPoliticaAcceso duracion,
                                       TipoGestionAcceso gestion, String codDocumPaciente,
                                       String profesionalAutorizado, String tipoDocumento,
                                       Date fechaVencimiento, String referencia) {
        return crearPolitica(alcance, duracion, gestion, codDocumPaciente, profesionalAutorizado,
                tipoDocumento, null, fechaVencimiento, referencia);
    }

    public PoliticaAcceso crearPolitica(AlcancePoliticaAcceso alcance, DuracionPoliticaAcceso duracion,
                                       TipoGestionAcceso gestion, String codDocumPaciente,
                                       String profesionalAutorizado, String tipoDocumento,
                                       String clinicaAutorizada, Date fechaVencimiento, String referencia) {
        return crearPolitica(alcance, duracion, gestion, codDocumPaciente, profesionalAutorizado,
                tipoDocumento, clinicaAutorizada, null, fechaVencimiento, referencia);
    }

    public PoliticaAcceso crearPolitica(AlcancePoliticaAcceso alcance, DuracionPoliticaAcceso duracion,
                                       TipoGestionAcceso gestion, String codDocumPaciente,
                                       String profesionalAutorizado, String tipoDocumento,
                                       String clinicaAutorizada, String especialidadesAutorizadas, 
                                       Date fechaVencimiento, String referencia) {
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setAlcance(alcance);
        politica.setDuracion(duracion);
        politica.setGestion(gestion);
        politica.setCodDocumPaciente(codDocumPaciente);
        politica.setProfesionalAutorizado(profesionalAutorizado != null ? profesionalAutorizado : "*");
        politica.setTipoDocumento(tipoDocumento);
        politica.setClinicaAutorizada(clinicaAutorizada);
        politica.setEspecialidadesAutorizadas(especialidadesAutorizadas);
        politica.setFechaVencimiento(fechaVencimiento);
        politica.setReferencia(referencia);
        politica.setActiva(true);
        
        return repository.crear(politica);
    }

    public PoliticaAcceso crearPolitica(PoliticaAcceso politica) {
        if (politica.getFechaCreacion() == null) {
            politica.setFechaCreacion(new Date());
        }
        // Asegurar que profesionalAutorizado nunca sea null (requerido por la base de datos)
        if (politica.getProfesionalAutorizado() == null || politica.getProfesionalAutorizado().trim().isEmpty()) {
            politica.setProfesionalAutorizado("*");
        }
        politica.setActiva(true);
        return repository.crear(politica);
    }

    public boolean verificarPermiso(String profesionalId, String codDocumPaciente, String tipoDocumento) {
        return verificarPermiso(profesionalId, codDocumPaciente, tipoDocumento, null);
    }

    public boolean verificarPermiso(String profesionalId, String codDocumPaciente, String tipoDocumento, String tenantIdProfesional) {
        return verificarPermiso(profesionalId, codDocumPaciente, tipoDocumento, tenantIdProfesional, null);
    }

    public boolean verificarPermiso(String profesionalId, String codDocumPaciente, String tipoDocumento, String tenantIdProfesional, String especialidadProfesional) {
        List<PoliticaAcceso> politicas = repository.verificarPermiso(profesionalId, codDocumPaciente, tipoDocumento, tenantIdProfesional, especialidadProfesional);
        return !politicas.isEmpty();
    }

    public List<PoliticaAcceso> listarPorPaciente(String codDocumPaciente) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PoliticaAccesoService.class.getName());
        logger.info("🔵 [POLITICAS-SERVICE] listarPorPaciente llamado con CI: '" + codDocumPaciente + "'");
        List<PoliticaAcceso> politicas = repository.buscarPorPaciente(codDocumPaciente);
        logger.info("🔵 [POLITICAS-SERVICE] Políticas retornadas por repository: " + politicas.size());
        if (!politicas.isEmpty()) {
            for (PoliticaAcceso p : politicas) {
                logger.info("  ✅ Política - ID: " + p.getId() + ", Paciente CI: " + p.getCodDocumPaciente());
            }
        }
        return politicas;
    }

    public List<PoliticaAcceso> listarPorProfesional(String profesionalId) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PoliticaAccesoService.class.getName());
        logger.info("🔵 [SERVICE] listarPorProfesional llamado con profesionalId: '" + profesionalId + "'");
        List<PoliticaAcceso> politicas = repository.buscarPorProfesional(profesionalId);
        logger.info("🔵 [SERVICE] Políticas retornadas por repository: " + politicas.size());
        return politicas;
    }

    public void eliminarPolitica(Long id) {
        repository.eliminar(id);
    }

    public PoliticaAcceso obtenerPorId(Long id) {
        return repository.buscarPorId(id);
    }

    public List<PoliticaAcceso> listarTodas() {
        return repository.listarTodas();
    }
}


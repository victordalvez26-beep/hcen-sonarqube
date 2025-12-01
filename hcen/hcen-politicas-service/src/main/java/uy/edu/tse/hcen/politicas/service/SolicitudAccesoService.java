package uy.edu.tse.hcen.politicas.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.edu.tse.hcen.politicas.model.SolicitudAcceso;
import uy.edu.tse.hcen.politicas.repository.SolicitudAccesoRepository;
import uy.edu.tse.hcen.common.enumerations.EstadoSolicitudAcceso;
import uy.edu.tse.hcen.common.enumerations.AlcancePoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.DuracionPoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.TipoGestionAcceso;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.json.JSONObject;

@Stateless
public class SolicitudAccesoService {

    private static final Logger LOGGER = Logger.getLogger(SolicitudAccesoService.class.getName());

    @Inject
    private SolicitudAccesoRepository repository;

    @Inject
    private PoliticaAccesoService politicaAccesoService;

    public SolicitudAcceso crearSolicitud(String solicitanteId, String especialidad,
            String codDocumPaciente, String tipoDocumento,
            String documentoId, String razonSolicitud) {
        return crearSolicitud(solicitanteId, especialidad, codDocumPaciente, tipoDocumento, documentoId, razonSolicitud,
                null);
    }

    public SolicitudAcceso crearSolicitud(String solicitanteId, String especialidad,
            String codDocumPaciente, String tipoDocumento,
            String documentoId, String razonSolicitud, String tenantId) {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setSolicitanteId(solicitanteId);
        solicitud.setEspecialidad(especialidad);
        solicitud.setCodDocumPaciente(codDocumPaciente);
        solicitud.setTipoDocumento(tipoDocumento);
        solicitud.setDocumentoId(documentoId);
        solicitud.setRazonSolicitud(razonSolicitud);
        solicitud.setClinicaAutorizada(tenantId); // Guardar tenantId de la clínica del profesional
        solicitud.setEstado(EstadoSolicitudAcceso.PENDIENTE);

        // Enviar notificación
        sendNotificationToUser(tipoDocumento, codDocumPaciente, razonSolicitud);

        return repository.crear(solicitud);
    }

    private void sendNotificationToUser(String tipoDocumento, String codDocumPaciente, String razonSolicitud) {
        try {
            String userUid = "uy-ci-" + codDocumPaciente;
            JSONObject json = new JSONObject();
            json.put("userUid", userUid);
            json.put("notificationType", "new_access_request");
            json.put("title", "Nuevo pedido de acceso");
            json.put("body", "Te solicitaron acceder a tu información clínica con la razón: " + razonSolicitud);

            String baseUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8080/hcen");
            URL url = new URL(baseUrl + "/hcen/api/notifications/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            LOGGER.info(String.format("Envío de notificación con código de respuesta: %d", responseCode));
        } catch (Exception e) {
            LOGGER.warning(String.format("Error enviando notificación: " + e.getMessage()));
        }
    }

    public SolicitudAcceso aprobarSolicitud(Long solicitudId, String resueltoPor, String comentario) {
        SolicitudAcceso solicitud = repository.buscarPorId(solicitudId);
        if (solicitud == null) {
            throw new IllegalArgumentException("Solicitud no encontrada");
        }

        // Si ya está aprobada, devolverla sin error (idempotente)
        if (solicitud.getEstado() == EstadoSolicitudAcceso.APROBADA) {
            return solicitud;
        }

        // Si está rechazada, no se puede aprobar
        if (solicitud.getEstado() == EstadoSolicitudAcceso.RECHAZADA) {
            throw new IllegalStateException("La solicitud ya fue rechazada y no puede ser aprobada");
        }

        // Si está pendiente, aprobarla
        solicitud.setEstado(EstadoSolicitudAcceso.APROBADA);
        solicitud.setFechaResolucion(new Date());
        solicitud.setResueltoPor(resueltoPor);
        solicitud.setResolucionComentario(comentario);

        SolicitudAcceso actualizada = repository.actualizar(solicitud);

        // Crear automáticamente una política de acceso cuando se aprueba la solicitud
        try {
            // Determinar el alcance basado en el tipo de documento
            // Si hay tipoDocumento específico, usar UN_DOCUMENTO_ESPECIFICO
            // Si no hay tipoDocumento (solicitud de historia clínica completa), usar
            // TODOS_LOS_DOCUMENTOS
            AlcancePoliticaAcceso alcance = (solicitud.getTipoDocumento() != null
                    && !solicitud.getTipoDocumento().isBlank())
                            ? AlcancePoliticaAcceso.UN_DOCUMENTO_ESPECIFICO
                            : AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS;

            // Si el alcance es TODOS_LOS_DOCUMENTOS, el tipoDocumento debe ser NULL
            // para que la query de verificación funcione correctamente (p.tipoDocumento IS
            // NULL)
            String tipoDocumentoParaPolitica = (alcance == AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS)
                    ? null
                    : solicitud.getTipoDocumento();

            // Crear política de acceso con duración indefinida
            // Usar el tenantId guardado en la solicitud (clinicaAutorizada)
            String clinicaAutorizada = solicitud.getClinicaAutorizada();
            if (clinicaAutorizada == null || clinicaAutorizada.isBlank()) {
                LOGGER.warning(String.format(
                        "Solicitud ID: %d no tiene clinicaAutorizada (tenantId) - la política puede no funcionar correctamente",
                        solicitudId));
            }

            politicaAccesoService.crearPolitica(
                    alcance,
                    DuracionPoliticaAcceso.INDEFINIDA,
                    TipoGestionAcceso.MANUAL,
                    solicitud.getCodDocumPaciente(),
                    solicitud.getSolicitanteId(),
                    tipoDocumentoParaPolitica,
                    clinicaAutorizada, // Usar el tenantId guardado en la solicitud
                    null, // fechaVencimiento - null para duración indefinida
                    "Política creada automáticamente al aprobar solicitud ID: " + solicitudId);

            LOGGER.info(String.format(
                    "Política de acceso creada automáticamente para solicitud ID: %d, profesional: %s, paciente: %s",
                    solicitudId, solicitud.getSolicitanteId(), solicitud.getCodDocumPaciente()));
        } catch (Exception e) {
            // Log el error pero no fallar la aprobación de la solicitud
            LOGGER.warning(String.format("Error al crear política de acceso automática para solicitud ID: %d: %s",
                    solicitudId, e.getMessage()));
        }

        return actualizada;
    }

    public SolicitudAcceso rechazarSolicitud(Long solicitudId, String resueltoPor, String comentario) {
        SolicitudAcceso solicitud = repository.buscarPorId(solicitudId);
        if (solicitud == null) {
            throw new IllegalArgumentException("Solicitud no encontrada");
        }

        // Si ya está rechazada, devolverla sin error (idempotente)
        if (solicitud.getEstado() == EstadoSolicitudAcceso.RECHAZADA) {
            return solicitud;
        }

        // Si está aprobada, no se puede rechazar
        if (solicitud.getEstado() == EstadoSolicitudAcceso.APROBADA) {
            throw new IllegalStateException("La solicitud ya fue aprobada y no puede ser rechazada");
        }

        // Si está pendiente, rechazarla
        solicitud.setEstado(EstadoSolicitudAcceso.RECHAZADA);
        solicitud.setFechaResolucion(new Date());
        solicitud.setResueltoPor(resueltoPor);
        solicitud.setResolucionComentario(comentario);

        return repository.actualizar(solicitud);
    }

    public List<SolicitudAcceso> listarPendientes() {
        return repository.buscarPendientes();
    }

    public List<SolicitudAcceso> listarPorPaciente(String codDocumPaciente) {
        return repository.buscarPorPaciente(codDocumPaciente);
    }

    public List<SolicitudAcceso> listarPendientesPorPaciente(String codDocumPaciente) {
        return repository.buscarPendientesPorPaciente(codDocumPaciente);
    }

    public List<SolicitudAcceso> listarPorProfesional(String profesionalId) {
        return repository.buscarPorProfesional(profesionalId);
    }

    public SolicitudAcceso obtenerPorId(Long id) {
        return repository.buscarPorId(id);
    }
}

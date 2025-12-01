package uy.edu.tse.hcen.politicas.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.edu.tse.hcen.politicas.model.RegistroAcceso;
import uy.edu.tse.hcen.politicas.repository.RegistroAccesoRepository;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONObject;

@Stateless
public class RegistroAccesoService {

    private static final Logger LOGGER = Logger.getLogger(SolicitudAccesoService.class.getName());

    @Inject
    private RegistroAccesoRepository repository;

    public RegistroAcceso registrarAcceso(String profesionalId, String codDocumPaciente,
            String documentoId, String tipoDocumento,
            String ipAddress, String userAgent,
            boolean exito, String motivoRechazo, String referencia) {
        return registrarAcceso(profesionalId, codDocumPaciente, documentoId, tipoDocumento,
                ipAddress, userAgent, exito, motivoRechazo, referencia,
                null, null, null); // Mantener compatibilidad con método anterior
    }

    public RegistroAcceso registrarAcceso(String profesionalId, String codDocumPaciente,
            String documentoId, String tipoDocumento,
            String ipAddress, String userAgent,
            boolean exito, String motivoRechazo, String referencia,
            String clinicaId, String nombreProfesional, String especialidad) {
        RegistroAcceso registro = new RegistroAcceso();
        registro.setProfesionalId(profesionalId);
        registro.setCodDocumPaciente(codDocumPaciente);
        registro.setDocumentoId(documentoId);
        registro.setTipoDocumento(tipoDocumento);
        registro.setIpAddress(ipAddress);
        registro.setUserAgent(userAgent);
        registro.setExito(exito);
        registro.setMotivoRechazo(motivoRechazo);
        registro.setReferencia(referencia);
        registro.setClinicaId(clinicaId);
        registro.setNombreProfesional(nombreProfesional);
        registro.setEspecialidad(especialidad);
        RegistroAcceso result = repository.crear(registro);

        // Enviar notificación
        sendNotificationToUser(tipoDocumento, codDocumPaciente, nombreProfesional);
        return result;
    }

    private void sendNotificationToUser(String tipoDocumento, String codDocumPaciente, String nombreProfesional) {
        try {
            String userUid = "uy-ci-" + codDocumPaciente;
            JSONObject json = new JSONObject();
            json.put("userUid", userUid);
            json.put("notificationType", "new_access_history");
            json.put("title", "Nuevo acceso a Historia Clínica");
            json.put("body", "Tienes un nuevo acceso a tu historia clínica por parte de " + nombreProfesional);

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

    public List<RegistroAcceso> listarPorPaciente(String codDocumPaciente) {
        return repository.buscarPorPaciente(codDocumPaciente);
    }

    public List<RegistroAcceso> listarPorProfesional(String profesionalId) {
        return repository.buscarPorProfesional(profesionalId);
    }

    public List<RegistroAcceso> listarPorDocumento(String documentoId) {
        return repository.buscarPorDocumento(documentoId);
    }

    public List<RegistroAcceso> listarPorRangoFechas(Date fechaInicio, Date fechaFin) {
        return repository.buscarPorRangoFechas(fechaInicio, fechaFin);
    }

    public Long contarAccesosPorPaciente(String codDocumPaciente) {
        return repository.contarAccesosPorPaciente(codDocumPaciente);
    }

    public RegistroAcceso obtenerPorId(Long id) {
        return repository.buscarPorId(id);
    }
}

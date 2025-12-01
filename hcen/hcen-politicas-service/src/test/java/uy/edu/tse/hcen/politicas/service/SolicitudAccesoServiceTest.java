package uy.edu.tse.hcen.politicas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.politicas.model.SolicitudAcceso;
import uy.edu.tse.hcen.politicas.repository.SolicitudAccesoRepository;
import uy.edu.tse.hcen.common.enumerations.EstadoSolicitudAcceso;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para SolicitudAccesoService.
 */
class SolicitudAccesoServiceTest {

    @Mock
    private SolicitudAccesoRepository repository;

    @Mock
    private PoliticaAccesoService politicaAccesoService;

    private SolicitudAccesoService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new SolicitudAccesoService();
        
        Field repoField = SolicitudAccesoService.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(service, repository);
        
        Field politicaField = SolicitudAccesoService.class.getDeclaredField("politicaAccesoService");
        politicaField.setAccessible(true);
        politicaField.set(service, politicaAccesoService);
    }

    @Test
    void crearSolicitud_validData_shouldCreate() {
        when(repository.crear(any(SolicitudAcceso.class))).thenAnswer(invocation -> {
            SolicitudAcceso s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });
        
        SolicitudAcceso result = service.crearSolicitud(
            "PROF-001", "CARDIOLOGIA", "1.234.567-8", 
            "Resumen", "DOC-123", "Reason", "tenant-1");
        
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(EstadoSolicitudAcceso.PENDIENTE, result.getEstado());
        assertEquals("PROF-001", result.getSolicitanteId());
        assertEquals("1.234.567-8", result.getCodDocumPaciente());
        verify(repository, times(1)).crear(any(SolicitudAcceso.class));
    }

    @Test
    void aprobarSolicitud_validId_shouldApprove() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        solicitud.setEstado(EstadoSolicitudAcceso.PENDIENTE);
        solicitud.setCodDocumPaciente("1.234.567-8");
        solicitud.setSolicitanteId("PROF-001");
        solicitud.setTipoDocumento("Resumen");
        solicitud.setClinicaAutorizada("tenant-1");
        
        when(repository.buscarPorId(1L)).thenReturn(solicitud);
        when(repository.actualizar(any(SolicitudAcceso.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));
        when(politicaAccesoService.crearPolitica(any(), any(), any(), anyString(), 
            anyString(), anyString(), anyString(), any(), anyString()))
            .thenReturn(null);
        
        SolicitudAcceso result = service.aprobarSolicitud(1L, "admin", "Approved");
        
        assertNotNull(result);
        assertEquals(EstadoSolicitudAcceso.APROBADA, result.getEstado());
        verify(repository, times(1)).actualizar(any(SolicitudAcceso.class));
    }

    @Test
    void aprobarSolicitud_notFound_shouldThrowException() {
        when(repository.buscarPorId(999L)).thenReturn(null);
        
        assertThrows(IllegalArgumentException.class, () -> 
            service.aprobarSolicitud(999L, "admin", "notes"));
    }

    @Test
    void aprobarSolicitud_alreadyApproved_shouldReturnSame() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        solicitud.setEstado(EstadoSolicitudAcceso.APROBADA);
        
        when(repository.buscarPorId(1L)).thenReturn(solicitud);
        
        SolicitudAcceso result = service.aprobarSolicitud(1L, "admin", "notes");
        
        assertNotNull(result);
        assertEquals(EstadoSolicitudAcceso.APROBADA, result.getEstado());
        verify(repository, never()).actualizar(any(SolicitudAcceso.class));
    }

    @Test
    void rechazarSolicitud_validId_shouldReject() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        solicitud.setEstado(EstadoSolicitudAcceso.PENDIENTE);
        
        when(repository.buscarPorId(1L)).thenReturn(solicitud);
        when(repository.actualizar(any(SolicitudAcceso.class))).thenAnswer(invocation ->
            invocation.getArgument(0));
        
        SolicitudAcceso result = service.rechazarSolicitud(1L, "admin", "Rejected reason");
        
        assertNotNull(result);
        assertEquals(EstadoSolicitudAcceso.RECHAZADA, result.getEstado());
    }

    @Test
    void listarPendientes_shouldReturnList() {
        SolicitudAcceso s1 = new SolicitudAcceso();
        SolicitudAcceso s2 = new SolicitudAcceso();
        
        when(repository.buscarPendientes()).thenReturn(Arrays.asList(s1, s2));
        
        List<SolicitudAcceso> result = service.listarPendientes();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).buscarPendientes();
    }

    @Test
    void obtenerPorId_validId_shouldReturnSolicitud() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        
        when(repository.buscarPorId(1L)).thenReturn(solicitud);
        
        SolicitudAcceso result = service.obtenerPorId(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void obtenerPorId_invalidId_shouldReturnNull() {
        when(repository.buscarPorId(999L)).thenReturn(null);
        
        SolicitudAcceso result = service.obtenerPorId(999L);
        
        assertNull(result);
    }

    @Test
    void crearSolicitud_basicMethod_shouldDelegateToExtended() {
        when(repository.crear(any(SolicitudAcceso.class))).thenAnswer(invocation -> {
            SolicitudAcceso s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });
        
        SolicitudAcceso result = service.crearSolicitud(
            "PROF-001", "CARDIOLOGIA", "1.234.567-8", 
            "Resumen", "DOC-123", "Reason");
        
        assertNotNull(result);
        assertEquals(EstadoSolicitudAcceso.PENDIENTE, result.getEstado());
    }

    @Test
    void aprobarSolicitud_alreadyRejected_shouldThrowException() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        solicitud.setEstado(EstadoSolicitudAcceso.RECHAZADA);
        
        when(repository.buscarPorId(1L)).thenReturn(solicitud);
        
        assertThrows(IllegalStateException.class, () -> 
            service.aprobarSolicitud(1L, "admin", "notes"));
    }

    @Test
    void aprobarSolicitud_withTipoDocumento_shouldCreatePoliticaWithUnDocumentoEspecifico() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        solicitud.setEstado(EstadoSolicitudAcceso.PENDIENTE);
        solicitud.setCodDocumPaciente("1.234.567-8");
        solicitud.setSolicitanteId("PROF-001");
        solicitud.setTipoDocumento("CONSULTA_MEDICA");
        solicitud.setClinicaAutorizada("tenant-1");
        
        when(repository.buscarPorId(1L)).thenReturn(solicitud);
        when(repository.actualizar(any(SolicitudAcceso.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));
        when(politicaAccesoService.crearPolitica(any(), any(), any(), anyString(), 
            anyString(), anyString(), anyString(), any(), anyString()))
            .thenReturn(null);
        
        SolicitudAcceso result = service.aprobarSolicitud(1L, "admin", "Approved");
        
        assertNotNull(result);
        verify(politicaAccesoService).crearPolitica(any(), any(), any(), anyString(), 
            anyString(), eq("CONSULTA_MEDICA"), anyString(), any(), anyString());
    }

    @Test
    void aprobarSolicitud_withoutTipoDocumento_shouldCreatePoliticaWithTodosLosDocumentos() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        solicitud.setEstado(EstadoSolicitudAcceso.PENDIENTE);
        solicitud.setCodDocumPaciente("1.234.567-8");
        solicitud.setSolicitanteId("PROF-001");
        solicitud.setTipoDocumento(null);
        solicitud.setClinicaAutorizada("tenant-1");
        
        when(repository.buscarPorId(1L)).thenReturn(solicitud);
        when(repository.actualizar(any(SolicitudAcceso.class))).thenAnswer(invocation -> 
            invocation.getArgument(0));
        when(politicaAccesoService.crearPolitica(any(), any(), any(), anyString(), 
            anyString(), isNull(), anyString(), any(), anyString()))
            .thenReturn(null);
        
        SolicitudAcceso result = service.aprobarSolicitud(1L, "admin", "Approved");
        
        assertNotNull(result);
        verify(politicaAccesoService).crearPolitica(any(), any(), any(), anyString(), 
            anyString(), isNull(), anyString(), any(), anyString());
    }

    @Test
    void rechazarSolicitud_notFound_shouldThrowException() {
        when(repository.buscarPorId(999L)).thenReturn(null);
        
        assertThrows(IllegalArgumentException.class, () -> 
            service.rechazarSolicitud(999L, "admin", "Rejected"));
    }

    @Test
    void rechazarSolicitud_alreadyRejected_shouldReturnSame() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        solicitud.setEstado(EstadoSolicitudAcceso.RECHAZADA);
        
        when(repository.buscarPorId(1L)).thenReturn(solicitud);
        
        SolicitudAcceso result = service.rechazarSolicitud(1L, "admin", "notes");
        
        assertNotNull(result);
        assertEquals(EstadoSolicitudAcceso.RECHAZADA, result.getEstado());
    }

    @Test
    void rechazarSolicitud_alreadyApproved_shouldThrowException() {
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setId(1L);
        solicitud.setEstado(EstadoSolicitudAcceso.APROBADA);
        
        when(repository.buscarPorId(1L)).thenReturn(solicitud);
        
        assertThrows(IllegalStateException.class, () -> 
            service.rechazarSolicitud(1L, "admin", "Rejected"));
    }

    @Test
    void listarPorPaciente_shouldReturnList() {
        SolicitudAcceso s1 = new SolicitudAcceso();
        when(repository.buscarPorPaciente("1.234.567-8")).thenReturn(Arrays.asList(s1));
        
        List<SolicitudAcceso> result = service.listarPorPaciente("1.234.567-8");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).buscarPorPaciente("1.234.567-8");
    }

    @Test
    void listarPendientesPorPaciente_shouldReturnList() {
        SolicitudAcceso s1 = new SolicitudAcceso();
        when(repository.buscarPendientesPorPaciente("1.234.567-8")).thenReturn(Arrays.asList(s1));
        
        List<SolicitudAcceso> result = service.listarPendientesPorPaciente("1.234.567-8");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).buscarPendientesPorPaciente("1.234.567-8");
    }

    @Test
    void listarPorProfesional_shouldReturnList() {
        SolicitudAcceso s1 = new SolicitudAcceso();
        when(repository.buscarPorProfesional("PROF-001")).thenReturn(Arrays.asList(s1));
        
        List<SolicitudAcceso> result = service.listarPorProfesional("PROF-001");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).buscarPorProfesional("PROF-001");
    }
}


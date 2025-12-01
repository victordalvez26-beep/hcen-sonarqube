package uy.edu.tse.hcen.politicas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.politicas.model.RegistroAcceso;
import uy.edu.tse.hcen.politicas.repository.RegistroAccesoRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios exhaustivos para RegistroAccesoService.
 */
class RegistroAccesoServiceTest {

    @Mock
    private RegistroAccesoRepository repository;

    private RegistroAccesoService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new RegistroAccesoService();
        
        Field repoField = RegistroAccesoService.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(service, repository);
    }

    @Test
    void registrarAcceso_basicMethod_shouldCreateRegistro() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String documentoId = "DOC-123";
        String tipoDocumento = "RECETA";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        boolean exito = true;
        String motivoRechazo = null;
        String referencia = "Consulta de rutina";
        
        RegistroAcceso saved = new RegistroAcceso();
        saved.setId(1L);
        
        when(repository.crear(any(RegistroAcceso.class))).thenAnswer(invocation -> {
            RegistroAcceso r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        
        RegistroAcceso result = service.registrarAcceso(
            profesionalId, codDocumPaciente, documentoId, tipoDocumento,
            ipAddress, userAgent, exito, motivoRechazo, referencia
        );
        
        assertNotNull(result);
        assertEquals(profesionalId, result.getProfesionalId());
        assertEquals(codDocumPaciente, result.getCodDocumPaciente());
        assertEquals(documentoId, result.getDocumentoId());
        assertEquals(tipoDocumento, result.getTipoDocumento());
        assertEquals(ipAddress, result.getIpAddress());
        assertEquals(userAgent, result.getUserAgent());
        assertEquals(exito, result.getExito());
        assertNull(result.getMotivoRechazo());
        assertEquals(referencia, result.getReferencia());
        verify(repository).crear(any(RegistroAcceso.class));
    }

    @Test
    void registrarAcceso_extendedMethod_shouldCreateRegistroWithAllFields() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        String documentoId = "DOC-123";
        String tipoDocumento = "RECETA";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        boolean exito = true;
        String motivoRechazo = null;
        String referencia = "Consulta de rutina";
        String clinicaId = "CLINIC-100";
        String nombreProfesional = "Dr. Juan Pérez";
        String especialidad = "CARDIOLOGIA";
        
        when(repository.crear(any(RegistroAcceso.class))).thenAnswer(invocation -> {
            RegistroAcceso r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        
        RegistroAcceso result = service.registrarAcceso(
            profesionalId, codDocumPaciente, documentoId, tipoDocumento,
            ipAddress, userAgent, exito, motivoRechazo, referencia,
            clinicaId, nombreProfesional, especialidad
        );
        
        assertNotNull(result);
        assertEquals(profesionalId, result.getProfesionalId());
        assertEquals(codDocumPaciente, result.getCodDocumPaciente());
        assertEquals(clinicaId, result.getClinicaId());
        assertEquals(nombreProfesional, result.getNombreProfesional());
        assertEquals(especialidad, result.getEspecialidad());
        verify(repository).crear(any(RegistroAcceso.class));
    }

    @Test
    void registrarAcceso_failedAccess_shouldSetExitoFalse() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        boolean exito = false;
        String motivoRechazo = "Política de acceso no encontrada";
        
        when(repository.crear(any(RegistroAcceso.class))).thenAnswer(invocation -> {
            RegistroAcceso r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        
        RegistroAcceso result = service.registrarAcceso(
            profesionalId, codDocumPaciente, null, null,
            null, null, exito, motivoRechazo, null
        );
        
        assertNotNull(result);
        assertFalse(result.getExito());
        assertEquals(motivoRechazo, result.getMotivoRechazo());
    }

    @Test
    void registrarAcceso_nullFields_shouldHandleGracefully() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        
        when(repository.crear(any(RegistroAcceso.class))).thenAnswer(invocation -> {
            RegistroAcceso r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        
        RegistroAcceso result = service.registrarAcceso(
            profesionalId, codDocumPaciente, null, null,
            null, null, true, null, null,
            null, null, null
        );
        
        assertNotNull(result);
        assertEquals(profesionalId, result.getProfesionalId());
        assertEquals(codDocumPaciente, result.getCodDocumPaciente());
        assertNull(result.getDocumentoId());
        assertNull(result.getTipoDocumento());
        assertNull(result.getIpAddress());
        assertNull(result.getUserAgent());
        assertNull(result.getClinicaId());
        assertNull(result.getNombreProfesional());
        assertNull(result.getEspecialidad());
    }

    @Test
    void listarPorPaciente_validCI_shouldReturnList() {
        String ci = "1.234.567-8";
        List<RegistroAcceso> registros = new ArrayList<>();
        RegistroAcceso r1 = new RegistroAcceso();
        r1.setId(1L);
        RegistroAcceso r2 = new RegistroAcceso();
        r2.setId(2L);
        registros.add(r1);
        registros.add(r2);
        
        when(repository.buscarPorPaciente(ci)).thenReturn(registros);
        
        List<RegistroAcceso> result = service.listarPorPaciente(ci);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).buscarPorPaciente(ci);
    }

    @Test
    void listarPorPaciente_noResults_shouldReturnEmptyList() {
        String ci = "999.999.999-9";
        
        when(repository.buscarPorPaciente(ci)).thenReturn(new ArrayList<>());
        
        List<RegistroAcceso> result = service.listarPorPaciente(ci);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listarPorProfesional_validId_shouldReturnList() {
        String profesionalId = "PROF-001";
        List<RegistroAcceso> registros = Arrays.asList(new RegistroAcceso(), new RegistroAcceso());
        
        when(repository.buscarPorProfesional(profesionalId)).thenReturn(registros);
        
        List<RegistroAcceso> result = service.listarPorProfesional(profesionalId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).buscarPorProfesional(profesionalId);
    }

    @Test
    void listarPorDocumento_validId_shouldReturnList() {
        String documentoId = "DOC-123";
        List<RegistroAcceso> registros = Arrays.asList(new RegistroAcceso());
        
        when(repository.buscarPorDocumento(documentoId)).thenReturn(registros);
        
        List<RegistroAcceso> result = service.listarPorDocumento(documentoId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).buscarPorDocumento(documentoId);
    }

    @Test
    void listarPorRangoFechas_validRange_shouldReturnList() {
        Date inicio = new Date(System.currentTimeMillis() - 86400000);
        Date fin = new Date();
        List<RegistroAcceso> registros = Arrays.asList(new RegistroAcceso(), new RegistroAcceso());
        
        when(repository.buscarPorRangoFechas(inicio, fin)).thenReturn(registros);
        
        List<RegistroAcceso> result = service.listarPorRangoFechas(inicio, fin);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).buscarPorRangoFechas(inicio, fin);
    }

    @Test
    void listarPorRangoFechas_sameDate_shouldReturnList() {
        Date fecha = new Date();
        List<RegistroAcceso> registros = new ArrayList<>();
        
        when(repository.buscarPorRangoFechas(fecha, fecha)).thenReturn(registros);
        
        List<RegistroAcceso> result = service.listarPorRangoFechas(fecha, fecha);
        
        assertNotNull(result);
        verify(repository).buscarPorRangoFechas(fecha, fecha);
    }

    @Test
    void contarAccesosPorPaciente_validCI_shouldReturnCount() {
        String ci = "1.234.567-8";
        Long count = 15L;
        
        when(repository.contarAccesosPorPaciente(ci)).thenReturn(count);
        
        Long result = service.contarAccesosPorPaciente(ci);
        
        assertEquals(count, result);
        verify(repository).contarAccesosPorPaciente(ci);
    }

    @Test
    void contarAccesosPorPaciente_noAccesses_shouldReturnZero() {
        String ci = "999.999.999-9";
        
        when(repository.contarAccesosPorPaciente(ci)).thenReturn(0L);
        
        Long result = service.contarAccesosPorPaciente(ci);
        
        assertEquals(0L, result);
    }

    @Test
    void obtenerPorId_validId_shouldReturnRegistro() {
        Long id = 1L;
        RegistroAcceso registro = new RegistroAcceso();
        registro.setId(id);
        
        when(repository.buscarPorId(id)).thenReturn(registro);
        
        RegistroAcceso result = service.obtenerPorId(id);
        
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(repository).buscarPorId(id);
    }

    @Test
    void obtenerPorId_notFound_shouldReturnNull() {
        Long id = 999L;
        
        when(repository.buscarPorId(id)).thenReturn(null);
        
        RegistroAcceso result = service.obtenerPorId(id);
        
        assertNull(result);
    }

    @Test
    void registrarAcceso_basicMethod_delegatesToExtendedMethod() {
        String profesionalId = "PROF-001";
        String codDocumPaciente = "1.234.567-8";
        
        when(repository.crear(any(RegistroAcceso.class))).thenAnswer(invocation -> {
            RegistroAcceso r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        
        RegistroAcceso result = service.registrarAcceso(
            profesionalId, codDocumPaciente, null, null,
            null, null, true, null, null
        );
        
        assertNotNull(result);
        // Verificar que los campos adicionales son null (delegación correcta)
        assertNull(result.getClinicaId());
        assertNull(result.getNombreProfesional());
        assertNull(result.getEspecialidad());
    }
}



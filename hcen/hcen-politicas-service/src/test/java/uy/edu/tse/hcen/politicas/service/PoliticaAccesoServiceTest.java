package uy.edu.tse.hcen.politicas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.common.enumerations.AlcancePoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.DuracionPoliticaAcceso;
import uy.edu.tse.hcen.common.enumerations.TipoGestionAcceso;
import uy.edu.tse.hcen.politicas.model.PoliticaAcceso;
import uy.edu.tse.hcen.politicas.repository.PoliticaAccesoRepository;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para PoliticaAccesoService.
 */
class PoliticaAccesoServiceTest {

    @Mock
    private PoliticaAccesoRepository repository;

    @InjectMocks
    private PoliticaAccesoService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new PoliticaAccesoService();
        Field field = PoliticaAccesoService.class.getDeclaredField("repository");
        field.setAccessible(true);
        field.set(service, repository);
    }

    @Test
    void crearPolitica_validParameters_shouldCreateAndReturn() {
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setAlcance(AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS);
        politica.setDuracion(DuracionPoliticaAcceso.INDEFINIDA);
        politica.setGestion(TipoGestionAcceso.AUTOMATICA);
        politica.setCodDocumPaciente("1.234.567-8");
        politica.setProfesionalAutorizado("PROF-001");
        politica.setTipoDocumento("Resumen de Alta");
        
        when(repository.crear(any(PoliticaAcceso.class))).thenAnswer(invocation -> {
            PoliticaAcceso p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        
        PoliticaAcceso result = service.crearPolitica(politica);
        
        assertNotNull(result);
        assertTrue(result.getActiva());
        assertNotNull(result.getFechaCreacion());
        verify(repository, times(1)).crear(any(PoliticaAcceso.class));
    }

    @Test
    void crearPolitica_nullProfesionalAutorizado_shouldSetWildcard() {
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setProfesionalAutorizado(null);
        
        when(repository.crear(any(PoliticaAcceso.class))).thenAnswer(invocation -> {
            PoliticaAcceso p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        
        PoliticaAcceso result = service.crearPolitica(politica);
        
        assertEquals("*", result.getProfesionalAutorizado());
    }

    @Test
    void verificarPermiso_withMatchingPolitica_shouldReturnTrue() {
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        
        when(repository.verificarPermiso("PROF-001", "1.234.567-8", "Resumen", null, null))
            .thenReturn(Arrays.asList(politica));
        
        boolean result = service.verificarPermiso("PROF-001", "1.234.567-8", "Resumen", null, null);
        
        assertTrue(result);
        verify(repository, times(1)).verificarPermiso(anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    void verificarPermiso_noMatchingPolitica_shouldReturnFalse() {
        when(repository.verificarPermiso("PROF-001", "1.234.567-8", "Resumen", null, null))
            .thenReturn(Arrays.asList());
        
        boolean result = service.verificarPermiso("PROF-001", "1.234.567-8", "Resumen", null, null);
        
        assertFalse(result);
    }

    @Test
    void listarPorPaciente_shouldReturnList() {
        PoliticaAcceso p1 = new PoliticaAcceso();
        PoliticaAcceso p2 = new PoliticaAcceso();
        
        when(repository.buscarPorPaciente("1.234.567-8")).thenReturn(Arrays.asList(p1, p2));
        
        List<PoliticaAcceso> result = service.listarPorPaciente("1.234.567-8");
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).buscarPorPaciente("1.234.567-8");
    }

    @Test
    void obtenerPorId_validId_shouldReturnPolitica() {
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        
        when(repository.buscarPorId(1L)).thenReturn(politica);
        
        PoliticaAcceso result = service.obtenerPorId(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void obtenerPorId_invalidId_shouldReturnNull() {
        when(repository.buscarPorId(999L)).thenReturn(null);
        
        PoliticaAcceso result = service.obtenerPorId(999L);
        
        assertNull(result);
    }

    @Test
    void eliminarPolitica_shouldCallRepository() {
        doNothing().when(repository).eliminar(1L);
        
        service.eliminarPolitica(1L);
        
        verify(repository, times(1)).eliminar(1L);
    }

    @Test
    void listarTodas_shouldReturnAll() {
        PoliticaAcceso p1 = new PoliticaAcceso();
        PoliticaAcceso p2 = new PoliticaAcceso();
        
        when(repository.listarTodas()).thenReturn(Arrays.asList(p1, p2));
        
        List<PoliticaAcceso> result = service.listarTodas();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void crearPolitica_withOverloadedMethods_shouldWork() {
        when(repository.crear(any(PoliticaAcceso.class))).thenAnswer(invocation -> {
            PoliticaAcceso p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PoliticaAcceso result1 = service.crearPolitica(
            AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS,
            DuracionPoliticaAcceso.INDEFINIDA,
            TipoGestionAcceso.AUTOMATICA,
            "12345678",
            "PROF-001",
            "CONSULTA_MEDICA",
            new java.util.Date(),
            "Referencia"
        );

        assertNotNull(result1);
        verify(repository).crear(any(PoliticaAcceso.class));
    }

    @Test
    void crearPolitica_withClinicaAutorizada_shouldWork() {
        when(repository.crear(any(PoliticaAcceso.class))).thenAnswer(invocation -> {
            PoliticaAcceso p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PoliticaAcceso result = service.crearPolitica(
            AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS,
            DuracionPoliticaAcceso.INDEFINIDA,
            TipoGestionAcceso.AUTOMATICA,
            "12345678",
            "PROF-001",
            "CONSULTA_MEDICA",
            "Clínica 1",
            new java.util.Date(),
            "Referencia"
        );

        assertNotNull(result);
        verify(repository).crear(any(PoliticaAcceso.class));
    }

    @Test
    void crearPolitica_withEspecialidades_shouldWork() {
        when(repository.crear(any(PoliticaAcceso.class))).thenAnswer(invocation -> {
            PoliticaAcceso p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PoliticaAcceso result = service.crearPolitica(
            AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS,
            DuracionPoliticaAcceso.INDEFINIDA,
            TipoGestionAcceso.AUTOMATICA,
            "12345678",
            "PROF-001",
            "CONSULTA_MEDICA",
            "Clínica 1",
            "CARDIOLOGIA",
            new java.util.Date(),
            "Referencia"
        );

        assertNotNull(result);
        verify(repository).crear(any(PoliticaAcceso.class));
    }

    @Test
    void crearPolitica_nullProfesionalAutorizadoInOverload_shouldSetWildcard() {
        when(repository.crear(any(PoliticaAcceso.class))).thenAnswer(invocation -> {
            PoliticaAcceso p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PoliticaAcceso result = service.crearPolitica(
            AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS,
            DuracionPoliticaAcceso.INDEFINIDA,
            TipoGestionAcceso.AUTOMATICA,
            "12345678",
            null, // null profesional
            "CONSULTA_MEDICA",
            "Clínica 1",
            "CARDIOLOGIA",
            new java.util.Date(),
            "Referencia"
        );

        assertEquals("*", result.getProfesionalAutorizado());
    }

    @Test
    void verificarPermiso_withTenantId_shouldPassAllParams() {
        PoliticaAcceso politica = new PoliticaAcceso();
        when(repository.verificarPermiso("PROF-001", "12345678", "CONSULTA", "100", null))
            .thenReturn(Arrays.asList(politica));

        boolean result = service.verificarPermiso("PROF-001", "12345678", "CONSULTA", "100", null);

        assertTrue(result);
    }

    @Test
    void verificarPermiso_withEspecialidad_shouldPassAllParams() {
        PoliticaAcceso politica = new PoliticaAcceso();
        when(repository.verificarPermiso("PROF-001", "12345678", "CONSULTA", "100", "CARDIOLOGIA"))
            .thenReturn(Arrays.asList(politica));

        boolean result = service.verificarPermiso("PROF-001", "12345678", "CONSULTA", "100", "CARDIOLOGIA");

        assertTrue(result);
    }

    @Test
    void verificarPermiso_withoutTenantId_shouldUseOverload() {
        PoliticaAcceso politica = new PoliticaAcceso();
        when(repository.verificarPermiso("PROF-001", "12345678", "CONSULTA", null, null))
            .thenReturn(Arrays.asList(politica));

        boolean result = service.verificarPermiso("PROF-001", "12345678", "CONSULTA");

        assertTrue(result);
    }

    @Test
    void listarPorProfesional_shouldReturnList() {
        PoliticaAcceso p1 = new PoliticaAcceso();
        when(repository.buscarPorProfesional("PROF-001")).thenReturn(Arrays.asList(p1));

        List<PoliticaAcceso> result = service.listarPorProfesional("PROF-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).buscarPorProfesional("PROF-001");
    }

    @Test
    void crearPolitica_withExistingFechaCreacion_shouldNotOverride() {
        PoliticaAcceso politica = new PoliticaAcceso();
        java.util.Date existingDate = new java.util.Date(1000L);
        politica.setFechaCreacion(existingDate);

        when(repository.crear(any(PoliticaAcceso.class))).thenAnswer(invocation -> {
            PoliticaAcceso p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PoliticaAcceso result = service.crearPolitica(politica);

        assertEquals(existingDate, result.getFechaCreacion());
    }

    @Test
    void crearPolitica_emptyProfesionalAutorizado_shouldSetWildcard() {
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setProfesionalAutorizado("   ");

        when(repository.crear(any(PoliticaAcceso.class))).thenAnswer(invocation -> {
            PoliticaAcceso p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PoliticaAcceso result = service.crearPolitica(politica);

        assertEquals("*", result.getProfesionalAutorizado());
    }
}


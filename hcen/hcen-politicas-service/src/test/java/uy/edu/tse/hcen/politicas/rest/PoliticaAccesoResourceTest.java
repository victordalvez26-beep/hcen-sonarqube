package uy.edu.tse.hcen.politicas.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dto.PoliticaAccesoDTO;
import uy.edu.tse.hcen.politicas.model.PoliticaAcceso;
import uy.edu.tse.hcen.politicas.service.PoliticaAccesoService;
import uy.edu.tse.hcen.common.enumerations.AlcancePoliticaAcceso;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios defensivos para PoliticaAccesoResource.
 */
class PoliticaAccesoResourceTest {

    @Mock
    private PoliticaAccesoService service;

    @InjectMocks
    private PoliticaAccesoResource resource;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resource = new PoliticaAccesoResource();
        Field field = PoliticaAccesoResource.class.getDeclaredField("service");
        field.setAccessible(true);
        field.set(resource, service);
    }

    @Test
    void crearPolitica_validDTO_shouldReturnCreated() {
        PoliticaAccesoDTO dto = new PoliticaAccesoDTO();
        dto.setAlcance(AlcancePoliticaAcceso.TODOS_LOS_DOCUMENTOS);
        dto.setCodDocumPaciente("1.234.567-8");
        dto.setProfesionalAutorizado("PROF-001");
        
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        
        when(service.crearPolitica(any(PoliticaAcceso.class))).thenReturn(politica);
        
        Response response = resource.crearPolitica(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service, times(1)).crearPolitica(any(PoliticaAcceso.class));
    }

    @Test
    void crearPolitica_exception_shouldReturnBadRequest() {
        PoliticaAccesoDTO dto = new PoliticaAccesoDTO();
        
        when(service.crearPolitica(any(PoliticaAcceso.class)))
            .thenThrow(new RuntimeException("Error de validación"));
        
        Response response = resource.crearPolitica(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerPorId_validId_shouldReturnOk() {
        PoliticaAcceso politica = new PoliticaAcceso();
        politica.setId(1L);
        
        when(service.obtenerPorId(1L)).thenReturn(politica);
        
        Response response = resource.obtenerPorId(1L);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void obtenerPorId_notFound_shouldReturnNotFound() {
        when(service.obtenerPorId(999L)).thenReturn(null);
        
        Response response = resource.obtenerPorId(999L);
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void listarPorPaciente_shouldReturnList() {
        PoliticaAcceso p1 = new PoliticaAcceso();
        PoliticaAcceso p2 = new PoliticaAcceso();
        
        when(service.listarPorPaciente("1.234.567-8")).thenReturn(Arrays.asList(p1, p2));
        
        Response response = resource.listarPorPaciente("1.234.567-8", null);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void verificarPermiso_validParams_shouldReturnResult() {
        when(service.verificarPermiso("PROF-001", "1.234.567-8", "Resumen", "tenant-1", null))
            .thenReturn(true);
        
        Response response = resource.verificarPermiso("PROF-001", "1.234.567-8", "Resumen", "tenant-1", null);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void verificarPermiso_missingParams_shouldReturnBadRequest() {
        Response response = resource.verificarPermiso(null, null, null, null, null);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void eliminarPolitica_validId_shouldReturnNoContent() {
        doNothing().when(service).eliminarPolitica(1L);
        
        Response response = resource.eliminarPolitica(1L);
        
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(service, times(1)).eliminarPolitica(1L);
    }

    @Test
    void eliminarPolitica_notFound_shouldReturnNotFound() {
        doThrow(new RuntimeException("Política no encontrada"))
            .when(service).eliminarPolitica(999L);
        
        Response response = resource.eliminarPolitica(999L);
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void listarPorProfesional_shouldReturnList() {
        PoliticaAcceso p1 = new PoliticaAcceso();
        PoliticaAcceso p2 = new PoliticaAcceso();
        
        when(service.listarPorProfesional("PROF-001")).thenReturn(Arrays.asList(p1, p2));
        
        Response response = resource.listarPorProfesional("PROF-001");
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service).listarPorProfesional("PROF-001");
    }

    @Test
    void listarTodas_shouldReturnList() {
        PoliticaAcceso p1 = new PoliticaAcceso();
        PoliticaAcceso p2 = new PoliticaAcceso();
        
        when(service.listarTodas()).thenReturn(Arrays.asList(p1, p2));
        
        Response response = resource.listarTodas();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(service).listarTodas();
    }

    @Test
    void verificarPermisoOptions_shouldReturnOk() {
        Response response = resource.verificarPermisoOptions();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void verificarPermiso_withAllParams_shouldReturnResult() {
        when(service.verificarPermiso("PROF-001", "1.234.567-8", "Resumen", "tenant-1", "CARDIOLOGIA"))
            .thenReturn(true);
        
        Response response = resource.verificarPermiso("PROF-001", "1.234.567-8", "Resumen", "tenant-1", "CARDIOLOGIA");
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void verificarPermiso_missingProfesionalId_shouldReturnBadRequest() {
        Response response = resource.verificarPermiso(null, "1.234.567-8", null, null, null);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void verificarPermiso_missingPacienteCI_shouldReturnBadRequest() {
        Response response = resource.verificarPermiso("PROF-001", null, null, null, null);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void listarPorPaciente_emptyList_shouldReturnOk() {
        when(service.listarPorPaciente("1.234.567-8")).thenReturn(Arrays.asList());
        
        Response response = resource.listarPorPaciente("1.234.567-8", null);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }
}


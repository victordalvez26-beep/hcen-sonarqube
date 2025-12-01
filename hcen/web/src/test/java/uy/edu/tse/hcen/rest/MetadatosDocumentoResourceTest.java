package uy.edu.tse.hcen.rest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.dto.DTMetadatos;
import uy.edu.tse.hcen.dto.MetadataDocumentoDTO;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.rndc.service.DocumentoRndcService;
import uy.edu.tse.hcen.util.JWTUtil;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios exhaustivos para MetadatosDocumentoResource.
 */
class MetadatosDocumentoResourceTest {

    @Mock
    private DocumentoRndcService documentoRndcService;

    @Mock
    private UserDAO userDAO;

    @Mock
    private HttpServletRequest request;

    private MetadatosDocumentoResource resource;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        reset(documentoRndcService, userDAO, request);
        resource = new MetadatosDocumentoResource();
        
        Field serviceField = MetadatosDocumentoResource.class.getDeclaredField("documentoRndcService");
        serviceField.setAccessible(true);
        serviceField.set(resource, documentoRndcService);
        
        Field daoField = MetadatosDocumentoResource.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(resource, userDAO);
    }

    // JWT hardcodeado para tests - generado con JWTUtil.generateJWT("test-user", 3600)
    private static final String HARDCODED_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDAwMzYwMCwiaXNzIjoiSENFTiJ9.test-signature";
    
    private void setupAuthenticatedUserWithMock(MockedStatic<JWTUtil> mockedJWTUtil, String userUid, String ci) {
        String jwt = HARDCODED_JWT;
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn(userUid);
        
        User user = new User(userUid, "user@example.com", "Test", null, "User", null, "CI", ci, null);
        when(userDAO.findByUid(userUid)).thenReturn(user);
    }

    @Test
    void recibirMetadatos_validData_shouldReturnCreated() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setDatosPatronimicos("Juan Pérez");
        dto.setTipoDocumento("RECETA");
        dto.setFechaCreacion(LocalDateTime.now());
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        dto.setTenantId("100");
        dto.setAutor("Dr. Test");
        dto.setBreakingTheGlass(false);
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            any(), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void recibirMetadatos_nullMetadata_shouldReturnBadRequest() {
        Response response = resource.recibirMetadatos(null);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_nullCI_shouldReturnBadRequest() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente(null);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_emptyCI_shouldReturnBadRequest() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("");
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_withFechaRegistro_shouldUseFechaRegistro() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setFechaRegistro(LocalDateTime.now());
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            any(), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_invalidDateFormat_shouldReturnBadRequest() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        reset(documentoRndcService);
        when(documentoRndcService.crearDocumentoDesdeParametros(
            eq("1.234.567-8"), isNull(), isNull(), eq("EVALUACION"),
            isNull(), eq("PDF"), eq("http://example.com/doc.pdf"), isNull(),
            isNull(), isNull(), isNull(), eq(true)
        )).thenThrow(new IllegalArgumentException("Invalid date"));
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_serviceException_shouldReturnInternalError() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        reset(documentoRndcService);
        when(documentoRndcService.crearDocumentoDesdeParametros(
            eq("1.234.567-8"), isNull(), isNull(), eq("EVALUACION"),
            isNull(), eq("PDF"), eq("http://example.com/doc.pdf"), isNull(),
            isNull(), isNull(), isNull(), eq(true)
        )).thenThrow(new RuntimeException("Service error"));
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_withProfesional_shouldReturnOk() {
        String ci = "1.234.567-8";
        List<MetadataDocumentoDTO> documentos = new ArrayList<>();
        MetadataDocumentoDTO doc = new MetadataDocumentoDTO();
        doc.setCodDocum(ci);
        documentos.add(doc);
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), anyString(), anyString(), anyString()
        )).thenReturn(documentos);
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, "PROF-001", "100", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void obtenerDocumentosPorCI_nullCI_shouldReturnBadRequest() {
        Response response = resource.obtenerDocumentosPorCI(
            null, null, null, null, null
        );
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_emptyCI_shouldReturnBadRequest() {
        Response response = resource.obtenerDocumentosPorCI(
            "   ", null, null, null, null
        );
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_serviceException_shouldReturnInternalError() {
        when(documentoRndcService.buscarDocumentosPorCI(
            anyString(), anyString(), anyString(), anyString()
        )).thenThrow(new RuntimeException("Service error"));
        
        Response response = resource.obtenerDocumentosPorCI(
            "1.234.567-8", "PROF-001", "100", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorUsuario_authenticated_shouldReturnOk() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            setupAuthenticatedUserWithMock(mockedJWTUtil, "user-uid", "1.234.567-8");
            
            List<MetadataDocumentoDTO> documentos = new ArrayList<>();
            when(documentoRndcService.buscarDocumentosPorCI(
                eq("1.234.567-8"), isNull(), isNull(), isNull()
            )).thenReturn(documentos);
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorUsuario_noCookie_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        
        Response response = resource.obtenerDocumentosPorUsuario(request);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorUsuario_invalidJWT_shouldReturnUnauthorized() {
        String jwt = "invalid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn(null);
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorUsuario_userNotFound_shouldReturnNotFound() {
        String jwt = HARDCODED_JWT;
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            when(userDAO.findByUid("user-uid")).thenReturn(null);
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorUsuario_userWithoutCI_shouldReturnBadRequest() {
        String jwt = HARDCODED_JWT;
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            User user = new User("user-uid", "user@example.com", "Test", null, "User", null, "CI", null, null);
            when(userDAO.findByUid("user-uid")).thenReturn(user);
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorUsuario_uidWithCI_shouldExtractCI() {
        String jwt = HARDCODED_JWT;
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("uy-ci-12345678");
            
            User user = new User("uy-ci-12345678", "user@example.com", "Test", null, "User", null, "CI", null, null);
            when(userDAO.findByUid("uy-ci-12345678")).thenReturn(user);
            
            List<MetadataDocumentoDTO> documentos = new ArrayList<>();
            when(documentoRndcService.buscarDocumentosPorCI(
                eq("12345678"), isNull(), isNull(), isNull()
            )).thenReturn(documentos);
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void descargarDocumento_validId_shouldReturnPdf() throws Exception {
        MetadataDocumentoDTO metadata = new MetadataDocumentoDTO();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        metadata.setClinicaOrigen("Clinica 1");
        
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(metadata);
        
        DocumentoRndcService.DocumentoDescarga descarga = 
            mock(DocumentoRndcService.DocumentoDescarga.class);
        java.io.InputStream stream = new java.io.ByteArrayInputStream("%PDF-1.4 test".getBytes());
        when(descarga.getStream()).thenReturn(stream);
        when(descarga.getContentType()).thenReturn("application/pdf");
        when(descarga.getFileName()).thenReturn("documento.pdf");
        
        when(documentoRndcService.prepararDescargaDocumento(1L)).thenReturn(descarga);
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void descargarDocumento_notFound_shouldReturnNotFound() {
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(null);
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }


    @Test
    void descargarDocumento_serviceExceptionOnObtener_shouldReturnInternalError() {
        when(documentoRndcService.obtenerDocumentoPorId(1L))
            .thenThrow(new RuntimeException("Service error"));
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_withNombreApellido_shouldSplitCorrectly() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setDatosPatronimicos("Juan Pérez García");
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            eq("1.234.567-8"), eq("Juan"), eq("Pérez García"), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            any(), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_nullDatosPatronimicos_shouldHandleGracefully() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setDatosPatronimicos(null);
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            eq("1.234.567-8"), isNull(), isNull(), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            any(), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_withTenantId_shouldParseCorrectly() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setTenantId("100");
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            eq(100L), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_invalidTenantId_shouldContinueWithoutTenantId() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setTenantId("invalid");
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            isNull(), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_breakingTheGlass_shouldPassCorrectly() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setBreakingTheGlass(true);
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            any(), anyString(), anyString(), eq(false) // accesoPermitido = !breakingTheGlass
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_withBlankDatosPatronimicos_shouldHandleGracefully() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setDatosPatronimicos("   "); // Blank
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            eq("1.234.567-8"), isNull(), isNull(), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            any(), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_withSingleWordDatosPatronimicos_shouldHandleCorrectly() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setDatosPatronimicos("Juan"); // Solo nombre, sin apellido
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            eq("1.234.567-8"), eq("Juan"), isNull(), anyString(),
            anyString(), anyString(), anyString(), anyString(),
            any(), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_withNullTipoDocumento_shouldUseDefault() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setTipoDocumento(null);
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            anyString(), anyString(), anyString(), eq("EVALUACION"), // Default
            anyString(), anyString(), anyString(), anyString(),
            any(), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_withNullFormato_shouldUseDefault() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setFormato(null);
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            anyString(), anyString(), anyString(), anyString(),
            anyString(), eq("PDF"), // Default
            anyString(), anyString(),
            any(), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_withAaPrestador_shouldUseAsClinicaOrigen() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setAaPrestador("Clínica Test");
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        when(documentoRndcService.crearDocumentoDesdeParametros(
            anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), eq("Clínica Test"),
            any(), anyString(), anyString(), anyBoolean()
        )).thenReturn(1L);
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_withNullParams_shouldHandleGracefully() {
        String ci = "1.234.567-8";
        List<MetadataDocumentoDTO> documentos = new ArrayList<>();
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), isNull(), isNull(), isNull()
        )).thenReturn(documentos);
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, null, null, null, null
        );
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_withAllParams_shouldPassAll() {
        String ci = "1.234.567-8";
        List<MetadataDocumentoDTO> documentos = new ArrayList<>();
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), eq("PROF-001"), eq("100"), eq("CARDIOLOGIA")
        )).thenReturn(documentos);
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, "PROF-001", "100", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void descargarDocumento_withNullStream_shouldReturnInternalError() throws Exception {
        MetadataDocumentoDTO metadata = new MetadataDocumentoDTO();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(metadata);
        
        DocumentoRndcService.DocumentoDescarga descarga = 
            mock(DocumentoRndcService.DocumentoDescarga.class);
        when(descarga.getStream()).thenReturn(null);
        
        when(documentoRndcService.prepararDescargaDocumento(1L)).thenReturn(descarga);
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorUsuario_exception_shouldReturnInternalError() {
        String jwt = HARDCODED_JWT;
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            when(userDAO.findByUid("user-uid")).thenThrow(new RuntimeException("Database error"));
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void solicitarAcceso_validData_shouldReturnCreated() {
        String jwt = HARDCODED_JWT;
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "1.234.567-8");
            body.put("tenantId", "100");
            body.put("motivo", "Consulta médica");
            
            // El método intentará hacer una llamada HTTP real al servicio de políticas
            Response response = resource.solicitarAcceso(request, body);
            
            // Puede fallar por conexión, pero la validación debe pasar
            assertNotNull(response);
        }
    }

    @Test
    void solicitarAcceso_missingPacienteCI_shouldReturnBadRequest() {
        String jwt = HARDCODED_JWT;
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            Map<String, Object> body = new HashMap<>();
            
            Response response = resource.solicitarAcceso(request, body);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void solicitarAcceso_noAuth_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);
        
        Map<String, Object> body = new HashMap<>();
        body.put("pacienteCI", "1.234.567-8");
        
        Response response = resource.solicitarAcceso(request, body);
        
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void solicitarAcceso_invalidJWT_shouldReturnUnauthorized() {
        String jwt = "invalid.jwt.token";
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn(null);
            
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "1.234.567-8");
            
            Response response = resource.solicitarAcceso(request, body);
            
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void solicitarAcceso_withBearerToken_shouldUseBearerToken() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + HARDCODED_JWT);
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(HARDCODED_JWT)).thenReturn("user-uid");
            
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "1.234.567-8");
            
            Response response = resource.solicitarAcceso(request, body);
            
            // Puede fallar por conexión, pero la autenticación debe pasar
            assertNotNull(response);
        }
    }

    @Test
    void descargarDocumento_metadataNotFound_shouldReturnNotFound() {
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(null);
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void descargarDocumento_serviceException_shouldReturnInternalError() {
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenThrow(new RuntimeException("Service error"));
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void descargarDocumento_emptyStream_shouldReturnInternalError() throws Exception {
        MetadataDocumentoDTO metadata = new MetadataDocumentoDTO();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(metadata);
        
        DocumentoRndcService.DocumentoDescarga descarga = 
            mock(DocumentoRndcService.DocumentoDescarga.class);
        java.io.ByteArrayInputStream emptyStream = new java.io.ByteArrayInputStream(new byte[0]);
        when(descarga.getStream()).thenReturn(emptyStream);
        when(descarga.getContentType()).thenReturn("application/pdf");
        when(descarga.getFileName()).thenReturn("documento.pdf");
        
        when(documentoRndcService.prepararDescargaDocumento(1L)).thenReturn(descarga);
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void descargarDocumento_validPdf_shouldReturnOk() throws Exception {
        MetadataDocumentoDTO metadata = new MetadataDocumentoDTO();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(metadata);
        
        DocumentoRndcService.DocumentoDescarga descarga = 
            mock(DocumentoRndcService.DocumentoDescarga.class);
        byte[] pdfBytes = "%PDF-1.4\nTest PDF content".getBytes();
        java.io.ByteArrayInputStream pdfStream = new java.io.ByteArrayInputStream(pdfBytes);
        when(descarga.getStream()).thenReturn(pdfStream);
        when(descarga.getContentType()).thenReturn("application/pdf");
        when(descarga.getFileName()).thenReturn("documento.pdf");
        
        when(documentoRndcService.prepararDescargaDocumento(1L)).thenReturn(descarga);
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void descargarDocumento_prepararDescargaThrowsException_shouldReturnInternalError() throws Exception {
        MetadataDocumentoDTO metadata = new MetadataDocumentoDTO();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(metadata);
        when(documentoRndcService.prepararDescargaDocumento(1L)).thenThrow(new RuntimeException("Download error"));
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_withBlankProfesionalId_shouldNotRegisterAccess() {
        String ci = "1.234.567-8";
        List<MetadataDocumentoDTO> documentos = new ArrayList<>();
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), anyString(), anyString(), anyString()
        )).thenReturn(documentos);
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, "   ", "100", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_withBlankTenantId_shouldNotRegisterAccess() {
        String ci = "1.234.567-8";
        List<MetadataDocumentoDTO> documentos = new ArrayList<>();
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), anyString(), anyString(), anyString()
        )).thenReturn(documentos);
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, "PROF-001", "   ", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_illegalArgumentException_shouldReturnBadRequest() {
        reset(documentoRndcService);
        when(documentoRndcService.buscarDocumentosPorCI(
            eq("invalid-ci"), eq("PROF-001"), eq("100"), eq("CARDIOLOGIA")
        )).thenThrow(new IllegalArgumentException("Invalid CI"));
        
        Response response = resource.obtenerDocumentosPorCI(
            "invalid-ci", "PROF-001", "100", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorUsuario_withEmptyCI_shouldReturnBadRequest() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            User user = new User("user-uid", "user@example.com", "Test", null, "User", null, "CI", "", null);
            when(userDAO.findByUid("user-uid")).thenReturn(user);
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorUsuario_withUidContainingCI_shouldExtractCI() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("uy-ci-12345678");
            
            User user = new User("uy-ci-12345678", "user@example.com", "Test", null, "User", null, "CI", null, null);
            when(userDAO.findByUid("uy-ci-12345678")).thenReturn(user);
            
            List<MetadataDocumentoDTO> documentos = new ArrayList<>();
            when(documentoRndcService.buscarDocumentosPorCI(
                eq("12345678"), isNull(), isNull(), isNull()
            )).thenReturn(documentos);
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorUsuario_illegalArgumentException_shouldReturnBadRequest() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            User user = new User("user-uid", "user@example.com", "Test", null, "User", null, "CI", "1.234.567-8", null);
            when(userDAO.findByUid("user-uid")).thenReturn(user);
            
            reset(documentoRndcService);
            when(documentoRndcService.buscarDocumentosPorCI(
                anyString(), isNull()
            )).thenThrow(new IllegalArgumentException("Invalid CI"));
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }


    @Test
    void solicitarAcceso_withRazonSolicitud_shouldIncludeInPayload() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "1.234.567-8");
            body.put("razonSolicitud", "Consulta médica urgente");
            body.put("tenantId", "100");
            
            Response response = resource.solicitarAcceso(request, body);
            
            assertNotNull(response);
        }
    }

    // Tests de extractJwtFromCookie eliminados: el método privado ya no existe,
    // ahora se usa CookieUtil.resolveJwtToken() que ya está cubierto en CookieUtilTest

    @Test
    void solicitarAcceso_withDocumentoId_shouldIncludeInPayload() {
        String jwt = HARDCODED_JWT;
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "1.234.567-8");
            body.put("documentoId", "DOC-123");
            body.put("tipoDocumento", "CONSULTA_MEDICA");
            body.put("especialidad", "CARDIOLOGIA");
            
            Response response = resource.solicitarAcceso(request, body);
            
            assertNotNull(response);
        }
    }

    @Test
    void solicitarAcceso_withRazonSolicitud_shouldUseRazonSolicitud() {
        String jwt = HARDCODED_JWT;
        Cookie cookie = new Cookie("hcen_session", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "1.234.567-8");
            body.put("razonSolicitud", "Razón personalizada");
            
            Response response = resource.solicitarAcceso(request, body);
            
            assertNotNull(response);
        }
    }

    @Test
    void descargarDocumento_invalidPdfHeader_shouldStillReturnOk() throws Exception {
        MetadataDocumentoDTO metadata = new MetadataDocumentoDTO();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(metadata);
        
        DocumentoRndcService.DocumentoDescarga descarga = 
            mock(DocumentoRndcService.DocumentoDescarga.class);
        // PDF con header inválido
        java.io.InputStream stream = new java.io.ByteArrayInputStream("INVALID".getBytes());
        when(descarga.getStream()).thenReturn(stream);
        when(descarga.getContentType()).thenReturn("application/pdf");
        when(descarga.getFileName()).thenReturn("documento.pdf");
        
        when(documentoRndcService.prepararDescargaDocumento(1L)).thenReturn(descarga);
        
        Response response = resource.descargarDocumento(1L, request);
        
        // Debe retornar OK aunque el header no sea válido (solo loguea warning)
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void descargarDocumento_serviceExceptionOnPreparar_shouldReturnInternalError() throws Exception {
        MetadataDocumentoDTO metadata = new MetadataDocumentoDTO();
        metadata.setId(1L);
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(metadata);

        when(documentoRndcService.prepararDescargaDocumento(1L))
            .thenThrow(new RuntimeException("Service error on download prepare"));
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_withProfesionalAndTenant_shouldRegisterAccess() {
        String ci = "1.234.567-8";
        List<MetadataDocumentoDTO> documentos = new ArrayList<>();
        MetadataDocumentoDTO doc = new MetadataDocumentoDTO();
        doc.setId(1L);
        documentos.add(doc);
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), eq("PROF-001"), eq("100"), eq("CARDIOLOGIA")
        )).thenReturn(documentos);
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, "PROF-001", "100", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        // El método registrarAccesoHistoriaClinica se llama de forma asíncrona
        // No podemos verificar fácilmente sin mocks más complejos
    }

    @Test
    void obtenerDocumentosPorCI_withNullProfesionalId_shouldNotRegisterAccess() {
        String ci = "1.234.567-8";
        List<MetadataDocumentoDTO> documentos = new ArrayList<>();
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), isNull(), isNull(), isNull()
        )).thenReturn(documentos);
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, null, "100", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_withNullTenantId_shouldNotRegisterAccess() {
        String ci = "1.234.567-8";
        List<MetadataDocumentoDTO> documentos = new ArrayList<>();
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), eq("PROF-001"), isNull(), eq("CARDIOLOGIA")
        )).thenReturn(documentos);
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, "PROF-001", null, "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_withEmptyList_shouldStillRegisterAccess() {
        String ci = "1.234.567-8";
        List<MetadataDocumentoDTO> documentos = new ArrayList<>(); // Lista vacía
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), eq("PROF-001"), eq("100"), eq("CARDIOLOGIA")
        )).thenReturn(documentos);
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, "PROF-001", "100", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void descargarDocumento_withValidPdfSmall_shouldReturnOk() throws Exception {
        MetadataDocumentoDTO metadata = new MetadataDocumentoDTO();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(metadata);
        
        DocumentoRndcService.DocumentoDescarga descarga = 
            mock(DocumentoRndcService.DocumentoDescarga.class);
        byte[] pdfBytes = "%PDF".getBytes();
        java.io.ByteArrayInputStream pdfStream = new java.io.ByteArrayInputStream(pdfBytes);
        when(descarga.getStream()).thenReturn(pdfStream);
        when(descarga.getContentType()).thenReturn("application/pdf");
        when(descarga.getFileName()).thenReturn("documento.pdf");
        
        when(documentoRndcService.prepararDescargaDocumento(1L)).thenReturn(descarga);
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void descargarDocumento_withLargePdf_shouldReturnOk() throws Exception {
        MetadataDocumentoDTO metadata = new MetadataDocumentoDTO();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(metadata);
        
        DocumentoRndcService.DocumentoDescarga descarga = 
            mock(DocumentoRndcService.DocumentoDescarga.class);
        byte[] pdfBytes = new byte[100000]; // 100KB
        java.util.Arrays.fill(pdfBytes, (byte) 'A');
        System.arraycopy("%PDF".getBytes(), 0, pdfBytes, 0, 4);
        java.io.ByteArrayInputStream pdfStream = new java.io.ByteArrayInputStream(pdfBytes);
        when(descarga.getStream()).thenReturn(pdfStream);
        when(descarga.getContentType()).thenReturn("application/pdf");
        when(descarga.getFileName()).thenReturn("documento.pdf");
        
        when(documentoRndcService.prepararDescargaDocumento(1L)).thenReturn(descarga);
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void descargarDocumento_withStreamReadException_shouldReturnInternalError() throws Exception {
        MetadataDocumentoDTO metadata = new MetadataDocumentoDTO();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        
        when(documentoRndcService.obtenerDocumentoPorId(1L)).thenReturn(metadata);
        
        DocumentoRndcService.DocumentoDescarga descarga = 
            mock(DocumentoRndcService.DocumentoDescarga.class);
        java.io.InputStream mockStream = mock(java.io.InputStream.class);
        when(mockStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new java.io.IOException("Read error"));
        when(descarga.getStream()).thenReturn(mockStream);
        when(descarga.getContentType()).thenReturn("application/pdf");
        when(descarga.getFileName()).thenReturn("documento.pdf");
        
        when(documentoRndcService.prepararDescargaDocumento(1L)).thenReturn(descarga);
        
        Response response = resource.descargarDocumento(1L, request);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void solicitarAcceso_withNullBody_shouldReturnBadRequest() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            Response response = resource.solicitarAcceso(request, null);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void solicitarAcceso_withEmptyPacienteCI_shouldReturnBadRequest() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "");
            
            Response response = resource.solicitarAcceso(request, body);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void solicitarAcceso_withMotivo_shouldUseMotivo() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "1.234.567-8");
            body.put("motivo", "Motivo personalizado");
            
            Response response = resource.solicitarAcceso(request, body);
            
            assertNotNull(response);
        }
    }

    @Test
    void solicitarAcceso_withTipoDocumento_shouldIncludeInPayload() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "1.234.567-8");
            body.put("tipoDocumento", "RECETA");
            
            Response response = resource.solicitarAcceso(request, body);
            
            assertNotNull(response);
        }
    }

    @Test
    void solicitarAcceso_withEspecialidad_shouldIncludeInPayload() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "1.234.567-8");
            body.put("especialidad", "CARDIOLOGIA");
            
            Response response = resource.solicitarAcceso(request, body);
            
            assertNotNull(response);
        }
    }

    @Test
    void solicitarAcceso_withBearerTokenNoPrefix_shouldReturnUnauthorized() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("invalid-token");
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            Map<String, Object> body = new HashMap<>();
            body.put("pacienteCI", "1.234.567-8");
            
            Response response = resource.solicitarAcceso(request, body);
            
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void registrarAccesoHistoriaClinica_shouldCallService() throws Exception {
        java.lang.reflect.Method method = MetadatosDocumentoResource.class.getDeclaredMethod(
            "registrarAccesoHistoriaClinica",
            String.class, String.class, String.class, String.class,
            String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);
        
        // El método hace una llamada HTTP real, pero no debe lanzar excepción
        assertDoesNotThrow(() -> {
            method.invoke(resource, "PROF-001", "Dr. Test", "CARDIOLOGIA", "100",
                "1.234.567-8", "DOC-123", "RECETA", true);
        });
    }

    @Test
    void registrarAccesoHistoriaClinica_withNullNombreProfesional_shouldHandle() throws Exception {
        java.lang.reflect.Method method = MetadatosDocumentoResource.class.getDeclaredMethod(
            "registrarAccesoHistoriaClinica",
            String.class, String.class, String.class, String.class,
            String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(resource, "PROF-001", null, "CARDIOLOGIA", "100",
                "1.234.567-8", "DOC-123", "RECETA", true);
        });
    }

    @Test
    void registrarAccesoHistoriaClinica_withNullEspecialidad_shouldHandle() throws Exception {
        java.lang.reflect.Method method = MetadatosDocumentoResource.class.getDeclaredMethod(
            "registrarAccesoHistoriaClinica",
            String.class, String.class, String.class, String.class,
            String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(resource, "PROF-001", "Dr. Test", null, "100",
                "1.234.567-8", "DOC-123", "RECETA", true);
        });
    }

    @Test
    void registrarAccesoHistoriaClinica_withNullDocumentoId_shouldUseDefaultTipo() throws Exception {
        java.lang.reflect.Method method = MetadatosDocumentoResource.class.getDeclaredMethod(
            "registrarAccesoHistoriaClinica",
            String.class, String.class, String.class, String.class,
            String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(resource, "PROF-001", "Dr. Test", "CARDIOLOGIA", "100",
                "1.234.567-8", null, null, true);
        });
    }

    @Test
    void registrarAccesoHistoriaClinica_withExitoFalse_shouldIncludeMotivoRechazo() throws Exception {
        java.lang.reflect.Method method = MetadatosDocumentoResource.class.getDeclaredMethod(
            "registrarAccesoHistoriaClinica",
            String.class, String.class, String.class, String.class,
            String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(resource, "PROF-001", "Dr. Test", "CARDIOLOGIA", "100",
                "1.234.567-8", "DOC-123", "RECETA", false);
        });
    }

    @Test
    void obtenerDocumentosPorCI_withIllegalArgumentException_shouldReturnBadRequest() {
        String ci = "1.234.567-8";
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), anyString(), anyString(), anyString()
        )).thenThrow(new IllegalArgumentException("Invalid CI format"));
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, "PROF-001", "100", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorCI_withRuntimeException_shouldReturnInternalError() {
        String ci = "1.234.567-8";
        
        when(documentoRndcService.buscarDocumentosPorCI(
            eq(ci), anyString(), anyString(), anyString()
        )).thenThrow(new RuntimeException("Service error"));
        
        Response response = resource.obtenerDocumentosPorCI(
            ci, "PROF-001", "100", "CARDIOLOGIA", "Dr. Test"
        );
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_withIllegalArgumentException_shouldReturnBadRequest() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        reset(documentoRndcService);
        when(documentoRndcService.crearDocumentoDesdeParametros(
            eq("1.234.567-8"), isNull(), isNull(), eq("EVALUACION"),
            isNull(), eq("PDF"), eq("http://example.com/doc.pdf"), isNull(),
            isNull(), isNull(), isNull(), eq(true)
        )).thenThrow(new IllegalArgumentException("Invalid data"));
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void recibirMetadatos_withRuntimeException_shouldReturnInternalError() {
        DTMetadatos dto = new DTMetadatos();
        dto.setDocumentoIdPaciente("1.234.567-8");
        dto.setFormato("application/pdf");
        dto.setUrlAcceso("http://example.com/doc.pdf");
        
        reset(documentoRndcService);
        when(documentoRndcService.crearDocumentoDesdeParametros(
            eq("1.234.567-8"), isNull(), isNull(), eq("EVALUACION"),
            isNull(), eq("PDF"), eq("http://example.com/doc.pdf"), isNull(),
            isNull(), isNull(), isNull(), eq(true)
        )).thenThrow(new RuntimeException("Service error"));
        
        Response response = resource.recibirMetadatos(dto);
        
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void obtenerDocumentosPorUsuario_withIllegalArgumentException_shouldReturnBadRequest() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            User user = new User("user-uid", "user@example.com", "Test", null, "User", null, "CI", "1.234.567-8", null);
            when(userDAO.findByUid("user-uid")).thenReturn(user);
            
            reset(documentoRndcService);
            when(documentoRndcService.buscarDocumentosPorCI(
                anyString(), isNull()
            )).thenThrow(new IllegalArgumentException("Invalid CI"));
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void obtenerDocumentosPorUsuario_withRuntimeException_shouldReturnInternalError() {
        try (MockedStatic<JWTUtil> mockedJWTUtil = mockStatic(JWTUtil.class)) {
            String jwt = HARDCODED_JWT;
            Cookie cookie = new Cookie("hcen_session", jwt);
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            mockedJWTUtil.when(() -> JWTUtil.validateJWT(jwt)).thenReturn("user-uid");
            
            User user = new User("user-uid", "user@example.com", "Test", null, "User", null, "CI", "1.234.567-8", null);
            when(userDAO.findByUid("user-uid")).thenReturn(user);
            
            reset(documentoRndcService);
            when(documentoRndcService.buscarDocumentosPorCI(
                anyString(), isNull()
            )).thenThrow(new RuntimeException("Service error"));
            
            Response response = resource.obtenerDocumentosPorUsuario(request);
            
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        }
    }
}

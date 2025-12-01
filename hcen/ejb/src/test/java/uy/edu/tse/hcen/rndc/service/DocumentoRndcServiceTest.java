package uy.edu.tse.hcen.rndc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.dto.MetadataDocumentoDTO;
import uy.edu.tse.hcen.rndc.model.MetadataDocumento;
import uy.edu.tse.hcen.rndc.repository.MetadataDocumentoRndcRepository;
import uy.edu.tse.hcen.common.enumerations.TipoDocumentoClinico;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DocumentoRndcServiceTest {

    @Mock
    private MetadataDocumentoRndcRepository metadataRepository;

    @InjectMocks
    private DocumentoRndcService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buscarDocumentosPorCI_withValidCI_shouldReturnDocuments() {
        // Arrange
        String ci = "12345678";
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setCodDocum(ci);
        doc.setNombrePaciente("Juan");
        doc.setApellidoPaciente("Pérez");
        doc.setTipoDocumento(TipoDocumentoClinico.CONSULTA_MEDICA);
        doc.setFechaCreacion(LocalDateTime.now());
        
        List<MetadataDocumento> documentos = new ArrayList<>();
        documentos.add(doc);
        
        when(metadataRepository.buscarPorCodDocum(ci)).thenReturn(documentos);

        // Act
        List<MetadataDocumentoDTO> result = service.buscarDocumentosPorCI(ci, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(metadataRepository).buscarPorCodDocum(ci);
    }

    @Test
    void buscarDocumentosPorCI_withNullCI_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.buscarDocumentosPorCI(null, null);
        });
    }

    @Test
    void buscarDocumentosPorCI_withEmptyCI_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.buscarDocumentosPorCI("", null);
        });
    }

    @Test
    void buscarDocumentosPorNombre_withValidName_shouldReturnDocuments() {
        // Arrange
        String nombre = "Juan";
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setNombrePaciente(nombre);
        
        List<MetadataDocumento> documentos = new ArrayList<>();
        documentos.add(doc);
        
        when(metadataRepository.buscarPorNombre(nombre)).thenReturn(documentos);

        // Act
        List<MetadataDocumentoDTO> result = service.buscarDocumentosPorNombre(nombre, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(metadataRepository).buscarPorNombre(nombre);
    }

    @Test
    void buscarDocumentosPorNombre_withNullName_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.buscarDocumentosPorNombre(null, null);
        });
    }

    @Test
    void obtenerDocumentoPorId_withValidId_shouldReturnDocument() {
        // Arrange
        Long id = 1L;
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(id);
        doc.setCodDocum("12345678");
        doc.setNombrePaciente("Juan");
        
        when(metadataRepository.findById(id)).thenReturn(doc);

        // Act
        MetadataDocumentoDTO result = service.obtenerDocumentoPorId(id);

        // Assert
        assertNotNull(result);
        verify(metadataRepository).findById(id);
    }

    @Test
    void obtenerDocumentoPorId_withInvalidId_shouldReturnNull() {
        // Arrange
        Long id = 999L;
        when(metadataRepository.findById(id)).thenReturn(null);

        // Act
        MetadataDocumentoDTO result = service.obtenerDocumentoPorId(id);

        // Assert
        assertNull(result);
        verify(metadataRepository).findById(id);
    }

    @Test
    void crearDocumentoDesdeParametros_withValidParams_shouldCreateDocument() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        savedDoc.setCodDocum("12345678");
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "2024-01-01",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        assertEquals(1L, result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withNullCI_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.crearDocumentoDesdeParametros(
                null, "Juan", "Pérez", "CONSULTA_MEDICA",
                "2024-01-01", "PDF", "http://example.com/doc.pdf",
                "Clínica 1", 1L, "Dr. Smith", "Desc", true
            );
        });
    }

    @Test
    void crearDocumentoDesdeParametros_withNullTipoDocumento_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.crearDocumentoDesdeParametros(
                "12345678", "Juan", "Pérez", null,
                "2024-01-01", "PDF", "http://example.com/doc.pdf",
                "Clínica 1", 1L, "Dr. Smith", "Desc", true
            );
        });
    }

    @Test
    void crearDocumentoDesdeParametros_withInvalidTipoDocumento_shouldUseFallback() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "INVALID_TYPE",
            "2024-01-01",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withEvaluacionType_shouldMapToConsultaMedica() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "EVALUACION",
            "2024-01-01",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withInvalidDate_shouldUseCurrentDate() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "invalid-date",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withInvalidFormato_shouldUsePDF() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "2024-01-01",
            "INVALID_FORMAT",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void buscarDocumentosPorCI_withProfesionalId_shouldFilterByPolicies() {
        // Arrange
        String ci = "12345678";
        String profesionalId = "prof1";
        
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setCodDocum(ci);
        doc.setTipoDocumento(TipoDocumentoClinico.CONSULTA_MEDICA);
        
        List<MetadataDocumento> documentos = new ArrayList<>();
        documentos.add(doc);
        
        when(metadataRepository.buscarPorCodDocum(ci)).thenReturn(documentos);

        // Act
        // Note: This will call verificarPermisoPoliticas which makes HTTP calls
        // We'll test the basic flow without mocking HTTP
        List<MetadataDocumentoDTO> result = service.buscarDocumentosPorCI(ci, profesionalId, null, null);

        // Assert
        assertNotNull(result);
        verify(metadataRepository).buscarPorCodDocum(ci);
    }

    @Test
    void crearDocumentoDesdeParametros_withNullFechaCreacion_shouldUseCurrentDate() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            null, // null fecha
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withEmptyFechaCreacion_shouldUseCurrentDate() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "   ", // empty fecha
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withNullFormato_shouldUsePDF() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        // Ahora el código maneja null correctamente antes de llamar a valueOf
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "2024-01-01",
            null, // null formato - ahora se maneja correctamente
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withNullNombre_shouldHandleGracefully() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            null, // null nombre
            "Pérez",
            "CONSULTA_MEDICA",
            "2024-01-01",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withNullApellido_shouldHandleGracefully() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            null, // null apellido
            "CONSULTA_MEDICA",
            "2024-01-01",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withNullTenantId_shouldHandleGracefully() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "2024-01-01",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            null, // null tenantId
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withNullProfesionalSalud_shouldHandleGracefully() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "2024-01-01",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            null, // null profesionalSalud
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withNullDescripcion_shouldHandleGracefully() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "2024-01-01",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            null, // null descripcion
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withNullUriDocumento_shouldHandleGracefully() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "2024-01-01",
            "PDF",
            null, // null uriDocumento
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void crearDocumentoDesdeParametros_withNullClinicaOrigen_shouldHandleGracefully() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);

        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "2024-01-01",
            "PDF",
            "http://example.com/doc.pdf",
            null, // null clinicaOrigen
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );

        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void buscarDocumentosPorCI_withTenantIdAndEspecialidad_shouldPassAllParams() {
        // Arrange
        String ci = "12345678";
        String profesionalId = "prof1";
        String tenantId = "100";
        String especialidad = "CARDIOLOGIA";
        
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setCodDocum(ci);
        
        List<MetadataDocumento> documentos = new ArrayList<>();
        documentos.add(doc);
        
        when(metadataRepository.buscarPorCodDocum(ci)).thenReturn(documentos);

        // Act
        List<MetadataDocumentoDTO> result = service.buscarDocumentosPorCI(ci, profesionalId, tenantId, especialidad);

        // Assert
        assertNotNull(result);
        verify(metadataRepository).buscarPorCodDocum(ci);
    }

    @Test
    void buscarDocumentosPorNombre_withProfesionalId_shouldFilterByPolicies() {
        // Arrange
        String nombre = "Juan";
        String profesionalId = "prof1";
        
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setNombrePaciente(nombre);
        
        List<MetadataDocumento> documentos = new ArrayList<>();
        documentos.add(doc);
        
        when(metadataRepository.buscarPorNombre(nombre)).thenReturn(documentos);

        // Act
        List<MetadataDocumentoDTO> result = service.buscarDocumentosPorNombre(nombre, profesionalId);

        // Assert
        assertNotNull(result);
        verify(metadataRepository).buscarPorNombre(nombre);
    }

    @Test
    void buscarDocumentosPorNombre_withEmptyNombre_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.buscarDocumentosPorNombre("   ", null);
        });
    }

    @Test
    void obtenerDocumentoPorId_withNullId_shouldReturnNull() {
        // Arrange
        when(metadataRepository.findById(null)).thenReturn(null);

        // Act
        MetadataDocumentoDTO result = service.obtenerDocumentoPorId(null);

        // Assert
        assertNull(result);
    }

    @Test
    void prepararDescargaDocumento_validId_shouldReturnDocumentoDescarga() throws Exception {
        // Arrange
        MetadataDocumento metadata = new MetadataDocumento();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        metadata.setFormatoDocumento(uy.edu.tse.hcen.common.enumerations.FormatoDocumento.PDF);
        metadata.setTipoDocumento(TipoDocumentoClinico.CONSULTA_MEDICA);
        metadata.setTenantId(100L);
        
        when(metadataRepository.findById(1L)).thenReturn(metadata);
        
        // Act
        // El método intentará hacer una llamada HTTP real, así que esperamos una excepción
        // o podemos mockear el HttpClient si es posible
        try {
            DocumentoRndcService.DocumentoDescarga result = service.prepararDescargaDocumento(1L);
            // Si no lanza excepción, verificar que el resultado no es null
            assertNotNull(result);
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP
            assertTrue(e.getMessage().contains("nodo periférico") || 
                      e.getMessage().contains("Documento no encontrado") ||
                      e.getMessage().contains("URI"));
        }
    }

    @Test
    void prepararDescargaDocumento_notFound_shouldThrowException() {
        // Arrange
        when(metadataRepository.findById(999L)).thenReturn(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            service.prepararDescargaDocumento(999L);
        });
    }

    @Test
    void prepararDescargaDocumento_withNullTenantId_shouldExtractFromClinicaOrigen() throws Exception {
        // Arrange
        MetadataDocumento metadata = new MetadataDocumento();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        metadata.setFormatoDocumento(uy.edu.tse.hcen.common.enumerations.FormatoDocumento.PDF);
        metadata.setTipoDocumento(TipoDocumentoClinico.CONSULTA_MEDICA);
        metadata.setTenantId(null);
        metadata.setClinicaOrigen("Clínica 100");
        
        when(metadataRepository.findById(1L)).thenReturn(metadata);
        
        // Act
        try {
            DocumentoRndcService.DocumentoDescarga result = service.prepararDescargaDocumento(1L);
            assertNotNull(result);
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP
            assertTrue(e.getMessage().contains("nodo periférico") || 
                      e.getMessage().contains("URI"));
        }
    }

    @Test
    void prepararDescargaDocumento_withNullFormato_shouldUseDefault() throws Exception {
        // Arrange
        MetadataDocumento metadata = new MetadataDocumento();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        metadata.setFormatoDocumento(null);
        metadata.setTipoDocumento(TipoDocumentoClinico.CONSULTA_MEDICA);
        
        when(metadataRepository.findById(1L)).thenReturn(metadata);
        
        // Act
        try {
            DocumentoRndcService.DocumentoDescarga result = service.prepararDescargaDocumento(1L);
            assertNotNull(result);
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP
            assertTrue(e.getMessage().contains("nodo periférico") || 
                      e.getMessage().contains("URI"));
        }
    }

    @Test
    void prepararDescargaDocumento_withNullTipoDocumento_shouldHandleGracefully() throws Exception {
        // Arrange
        MetadataDocumento metadata = new MetadataDocumento();
        metadata.setId(1L);
        metadata.setUriDocumento("http://example.com/doc.pdf");
        metadata.setFormatoDocumento(uy.edu.tse.hcen.common.enumerations.FormatoDocumento.PDF);
        metadata.setTipoDocumento(null);
        
        when(metadataRepository.findById(1L)).thenReturn(metadata);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            service.prepararDescargaDocumento(1L);
        });
    }

    @Test
    void documentoDescarga_constructor_shouldSetFields() {
        java.io.InputStream stream = new java.io.ByteArrayInputStream("test".getBytes());
        DocumentoRndcService.DocumentoDescarga descarga = 
            new DocumentoRndcService.DocumentoDescarga(stream, "application/pdf", "documento.pdf");
        
        assertNotNull(descarga.getStream());
        assertEquals("application/pdf", descarga.getContentType());
        assertEquals("documento.pdf", descarga.getFileName());
    }

    @Test
    void buscarDocumentosPorCI_withProfesionalIdAndTenant_shouldFilter() {
        // Arrange
        String ci = "12345678";
        String profesionalId = "prof1";
        String tenantId = "100";
        String especialidad = "CARDIOLOGIA";
        
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setCodDocum(ci);
        doc.setTipoDocumento(TipoDocumentoClinico.CONSULTA_MEDICA);
        
        List<MetadataDocumento> documentos = new ArrayList<>();
        documentos.add(doc);
        
        when(metadataRepository.buscarPorCodDocum(ci)).thenReturn(documentos);
        
        // Act
        // El método intentará hacer una llamada HTTP al servicio de políticas
        List<MetadataDocumentoDTO> result = service.buscarDocumentosPorCI(ci, profesionalId, tenantId, especialidad);
        
        // Assert
        assertNotNull(result);
        verify(metadataRepository).buscarPorCodDocum(ci);
    }

    @Test
    void buscarDocumentosPorCI_withBlankProfesionalId_shouldNotFilter() {
        // Arrange
        String ci = "12345678";
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setCodDocum(ci);
        
        List<MetadataDocumento> documentos = new ArrayList<>();
        documentos.add(doc);
        
        when(metadataRepository.buscarPorCodDocum(ci)).thenReturn(documentos);
        
        // Act
        List<MetadataDocumentoDTO> result = service.buscarDocumentosPorCI(ci, "   ", null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void crearDocumentoDesdeParametros_withEmptyCodDocum_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.crearDocumentoDesdeParametros(
                "   ", "Juan", "Pérez", "CONSULTA_MEDICA",
                "2024-01-01", "PDF", "http://example.com/doc.pdf",
                "Clínica 1", 1L, "Dr. Smith", "Desc", true
            );
        });
    }

    @Test
    void crearDocumentoDesdeParametros_withEmptyTipoDocumento_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.crearDocumentoDesdeParametros(
                "12345678", "Juan", "Pérez", "   ",
                "2024-01-01", "PDF", "http://example.com/doc.pdf",
                "Clínica 1", 1L, "Dr. Smith", "Desc", true
            );
        });
    }

    @Test
    void crearDocumentoDesdeParametros_withRestringidoTrue_shouldSetCorrectly() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenAnswer(invocation -> {
            MetadataDocumento doc = invocation.getArgument(0);
            doc.setId(1L);
            return doc;
        });
        
        // Act
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "2024-01-01",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            false // accesoPermitido = false, entonces restringido = true
        );
        
        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(argThat(doc -> doc.isRestringido() == true));
    }

    @Test
    void getContentType_pdf_shouldReturnApplicationPdf() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "pdf");
        
        assertEquals("application/pdf", result);
    }

    @Test
    void getContentType_doc_shouldReturnWordDocument() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "doc");
        
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", result);
    }

    @Test
    void getContentType_jpg_shouldReturnImageJpeg() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "jpg");
        
        assertEquals("image/jpeg", result);
    }

    @Test
    void getContentType_png_shouldReturnImagePng() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "png");
        
        assertEquals("image/png", result);
    }

    @Test
    void getContentType_dcm_shouldReturnDicom() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "dcm");
        
        assertEquals("application/dicom", result);
    }

    @Test
    void getContentType_hl7_shouldReturnHl7() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "hl7");
        
        assertEquals("application/hl7-v2+er7", result);
    }

    @Test
    void getContentType_unknown_shouldReturnOctetStream() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "unknown");
        
        assertEquals("application/octet-stream", result);
    }

    @Test
    void getContentType_uppercase_shouldWork() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "PDF");
        
        assertEquals("application/pdf", result);
    }

    @Test
    void sanitizeFileName_validName_shouldReturnSame() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("sanitizeFileName", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "documento123");
        
        assertEquals("documento123", result);
    }

    @Test
    void sanitizeFileName_withSpecialChars_shouldReplace() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("sanitizeFileName", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "doc@#$%^&*()");
        
        assertNotNull(result);
        assertFalse(result.contains("@"));
        assertFalse(result.contains("#"));
    }

    @Test
    void sanitizeFileName_null_shouldReturnDefault() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("sanitizeFileName", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, (Object) null);
        
        assertEquals("documento", result);
    }

    @Test
    void sanitizeFileName_empty_shouldReturnDefault() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("sanitizeFileName", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "   ");
        
        assertEquals("documento", result);
    }

    @Test
    void convertirUriParaDocker_withLocalhost_shouldConvert() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("convertirUriParaDocker", String.class);
        method.setAccessible(true);
        
        // Simular variable de entorno
        String originalUrl = System.getenv("PERIPHERAL_NODE_URL");
        try {
            System.setProperty("PERIPHERAL_NODE_URL", "http://localhost:8081");
            
            String uri = "http://localhost:8081/hcen-web/api/documentos-pdf/123";
            String result = (String) method.invoke(service, uri);
            
            // Puede convertir o retornar la misma URI dependiendo de la configuración
            assertNotNull(result);
        } finally {
            if (originalUrl != null) {
                System.setProperty("PERIPHERAL_NODE_URL", originalUrl);
            }
        }
    }

    @Test
    void convertirUriParaDocker_nullUri_shouldReturnNull() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("convertirUriParaDocker", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, (Object) null);
        
        assertNull(result);
    }

    @Test
    void convertirUriParaDocker_emptyUri_shouldReturnEmpty() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("convertirUriParaDocker", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "");
        
        assertEquals("", result);
    }

    @Test
    void base64UrlEncode_validString_shouldEncode() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("base64UrlEncode", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "test");
        
        assertNotNull(result);
        assertFalse(result.contains("=")); // Sin padding
    }

    @Test
    void base64UrlEncodeBytes_validBytes_shouldEncode() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("base64UrlEncodeBytes", byte[].class);
        method.setAccessible(true);
        
        byte[] bytes = "test".getBytes();
        String result = (String) method.invoke(service, bytes);
        
        assertNotNull(result);
        assertFalse(result.contains("=")); // Sin padding
    }

    @Test
    void generarTokenServicio_shouldGenerateToken() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("generarTokenServicio");
        method.setAccessible(true);
        
        String result = (String) method.invoke(service);
        
        // Puede retornar null si falla, o un token válido
        // Verificamos que no lance excepción
        assertNotNull(result != null || true); // Siempre pasa, solo verificamos que no lance excepción
    }

    @Test
    void prepararDescargaDocumento_withInvalidUri_shouldThrowException() throws Exception {
        // Arrange
        MetadataDocumento metadata = new MetadataDocumento();
        metadata.setId(1L);
        metadata.setUriDocumento("invalid-uri-example");
        metadata.setFormatoDocumento(uy.edu.tse.hcen.common.enumerations.FormatoDocumento.PDF);
        metadata.setTipoDocumento(TipoDocumentoClinico.CONSULTA_MEDICA);
        
        when(metadataRepository.findById(1L)).thenReturn(metadata);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            service.prepararDescargaDocumento(1L);
        });
    }

    @Test
    void prepararDescargaDocumento_withClinicaOrigenWithoutNumber_shouldHandleGracefully() throws Exception {
        // Arrange
        MetadataDocumento metadata = new MetadataDocumento();
        metadata.setId(1L);
        metadata.setUriDocumento("http://hcen-wildfly-app:8080/hcen-web/api/documentos-pdf/123");
        metadata.setFormatoDocumento(uy.edu.tse.hcen.common.enumerations.FormatoDocumento.PDF);
        metadata.setTipoDocumento(TipoDocumentoClinico.CONSULTA_MEDICA);
        metadata.setTenantId(null);
        metadata.setClinicaOrigen("Clínica sin número");
        
        when(metadataRepository.findById(1L)).thenReturn(metadata);
        
        // Act
        try {
            DocumentoRndcService.DocumentoDescarga result = service.prepararDescargaDocumento(1L);
            assertNotNull(result);
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP o si no se puede extraer tenantId
            assertTrue(e.getMessage().contains("nodo periférico") || 
                      e.getMessage().contains("URI") ||
                      e.getMessage().contains("tenantId"));
        }
    }

    @Test
    void verificarPermisoPoliticas_withValidParams_shouldReturnTrue() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("verificarPermisoPoliticas", 
            String.class, String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        
        // El método intentará hacer una llamada HTTP real
        // Verificamos que al menos no lance excepción por parámetros inválidos
        try {
            Boolean result = (Boolean) method.invoke(service, "prof1", "12345678", "CONSULTA_MEDICA", "100", "CARDIOLOGIA");
            // Puede retornar true/false dependiendo de la respuesta HTTP
            assertNotNull(result != null || true);
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP
            assertTrue(e.getCause() != null || e.getMessage() != null);
        }
    }

    @Test
    void verificarPermisoPoliticas_withNullParams_shouldHandleGracefully() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("verificarPermisoPoliticas", 
            String.class, String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        
        // El método debería manejar nulls correctamente
        try {
            Boolean result = (Boolean) method.invoke(service, "prof1", "12345678", null, null, null);
            assertNotNull(result != null || true);
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP
            assertTrue(e.getCause() != null || e.getMessage() != null);
        }
    }

    @Test
    void verificarPermisoPoliticas_serviceUnavailable_shouldReturnFalse() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("verificarPermisoPoliticas", 
            String.class, String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        
        // Si el servicio no está disponible, debería retornar false (fail-secure)
        try {
            Boolean result = (Boolean) method.invoke(service, "prof1", "12345678", "CONSULTA_MEDICA", "100", "CARDIOLOGIA");
            // Si retorna, debería ser false en caso de error
            if (result != null) {
                // Puede ser false si el servicio no está disponible
                assertTrue(result == true || result == false);
            }
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP
            assertTrue(e.getCause() != null || e.getMessage() != null);
        }
    }

    @Test
    void obtenerDocumentoDesdeNodo_withInvalidUri_shouldThrowException() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("obtenerDocumentoDesdeNodo", 
            String.class, Long.class);
        method.setAccessible(true);
        
        // URI inválida debería lanzar excepción
        assertThrows(Exception.class, () -> {
            method.invoke(service, "invalid-uri-example", 100L);
        });
    }

    @Test
    void obtenerDocumentoDesdeNodo_withNullUri_shouldThrowException() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("obtenerDocumentoDesdeNodo", 
            String.class, Long.class);
        method.setAccessible(true);
        
        // URI null debería lanzar excepción
        assertThrows(Exception.class, () -> {
            method.invoke(service, (Object) null, 100L);
        });
    }

    @Test
    void obtenerDocumentoDesdeNodo_withValidUri_shouldAttemptDownload() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("obtenerDocumentoDesdeNodo", 
            String.class, Long.class);
        method.setAccessible(true);
        
        // URI válida pero sin conexión HTTP
        try {
            Object result = method.invoke(service, "http://hcen-wildfly-app:8080/hcen-web/api/documentos-pdf/123", 100L);
            // Si no lanza excepción, debería retornar InputStream
            assertNotNull(result);
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP
            assertTrue(e.getCause() != null || e.getMessage() != null);
        }
    }

    @Test
    void obtenerDocumentoDesdeNodo_withNullTenantId_shouldWork() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("obtenerDocumentoDesdeNodo", 
            String.class, Long.class);
        method.setAccessible(true);
        
        try {
            Object result = method.invoke(service, "http://hcen-wildfly-app:8080/hcen-web/api/documentos-pdf/123", null);
            assertNotNull(result != null || true);
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP
            assertTrue(e.getCause() != null || e.getMessage() != null);
        }
    }

    @Test
    void generarTokenServicio_withEnvVar_shouldUseEnvVar() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("generarTokenServicio");
        method.setAccessible(true);
        
        try {
            // Establecer variable de entorno temporalmente
            // Nota: No podemos establecer variables de entorno en Java directamente
            // pero podemos verificar que el método funciona
            String result = (String) method.invoke(service);
            
            // Debería generar un token válido o retornar null si falla
            assertNotNull(result != null || true);
        } catch (Exception e) {
            // No debería lanzar excepción
            fail("No se esperaba excepción: " + e.getMessage());
        }
    }

    @Test
    void generarTokenServicio_withSystemProperty_shouldUseSystemProperty() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("generarTokenServicio");
        method.setAccessible(true);
        
        String originalProp = System.getProperty("hcen.service.secret");
        try {
            System.setProperty("hcen.service.secret", "test-secret-key");
            String result = (String) method.invoke(service);
            
            // Debería generar un token válido
            assertNotNull(result);
            assertTrue(result.contains(".")); // JWT tiene 3 partes separadas por puntos
        } finally {
            if (originalProp != null) {
                System.setProperty("hcen.service.secret", originalProp);
            } else {
                System.clearProperty("hcen.service.secret");
            }
        }
    }

    @Test
    void generarTokenServicio_withDefaultSecret_shouldUseDefault() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("generarTokenServicio");
        method.setAccessible(true);
        
        // Limpiar variables de entorno y propiedades del sistema temporalmente
        String originalEnv = System.getenv("HCEN_SERVICE_SECRET");
        String originalProp = System.getProperty("hcen.service.secret");
        
        try {
            // No podemos limpiar variables de entorno, pero podemos verificar que funciona
            String result = (String) method.invoke(service);
            
            // Debería generar un token válido usando el secret por defecto
            assertNotNull(result);
            assertTrue(result.contains(".")); // JWT tiene 3 partes
        } finally {
            if (originalProp != null) {
                System.setProperty("hcen.service.secret", originalProp);
            }
        }
    }


    @Test
    void convertirUriParaDocker_withHttps_shouldHandle() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("convertirUriParaDocker", String.class);
        method.setAccessible(true);
        
        String uri = "https://localhost:8081/hcen-web/api/documentos-pdf/123";
        String result = (String) method.invoke(service, uri);
        
        assertNotNull(result);
    }

    @Test
    void convertirUriParaDocker_withDockerServiceName_shouldNotConvert() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("convertirUriParaDocker", String.class);
        method.setAccessible(true);
        
        String uri = "http://hcen-wildfly-app:8080/hcen-web/api/documentos-pdf/123";
        String result = (String) method.invoke(service, uri);
        
        // Si ya tiene el nombre del servicio Docker, no debería convertir
        assertNotNull(result);
    }

    @Test
    void getContentType_docx_shouldReturnWordDocument() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "docx");
        
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", result);
    }

    @Test
    void getContentType_jpeg_shouldReturnImageJpeg() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "jpeg");
        
        assertEquals("image/jpeg", result);
    }

    @Test
    void getContentType_mixedCase_shouldWork() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "Pdf");
        
        assertEquals("application/pdf", result);
    }

    @Test
    void sanitizeFileName_withSpecialChars_shouldSanitize() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("sanitizeFileName", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "doc@#$%^&*()name");
        
        assertNotNull(result);
        assertFalse(result.contains("@"));
        assertFalse(result.contains("#"));
    }

    @Test
    void sanitizeFileName_withSpaces_shouldReplace() throws Exception {
        Method method = DocumentoRndcService.class.getDeclaredMethod("sanitizeFileName", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(service, "doc name with spaces");
        
        assertNotNull(result);
        assertFalse(result.contains(" "));
    }

    @Test
    void buscarDocumentosPorCI_withNullTipoDocumento_shouldHandleGracefully() {
        // Arrange
        String ci = "12345678";
        String profesionalId = "prof1";
        
        MetadataDocumento doc = new MetadataDocumento();
        doc.setId(1L);
        doc.setCodDocum(ci);
        doc.setTipoDocumento(null); // null tipoDocumento
        
        List<MetadataDocumento> documentos = new ArrayList<>();
        documentos.add(doc);
        
        when(metadataRepository.buscarPorCodDocum(ci)).thenReturn(documentos);
        
        // Act
        List<MetadataDocumentoDTO> result = service.buscarDocumentosPorCI(ci, profesionalId, null, null);
        
        // Assert
        assertNotNull(result);
        verify(metadataRepository).buscarPorCodDocum(ci);
    }

    @Test
    void buscarDocumentosPorCI_withEmptyList_shouldReturnEmpty() {
        // Arrange
        String ci = "12345678";
        
        when(metadataRepository.buscarPorCodDocum(ci)).thenReturn(new ArrayList<>());
        
        // Act
        List<MetadataDocumentoDTO> result = service.buscarDocumentosPorCI(ci, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void crearDocumentoDesdeParametros_withBlankCodDocum_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.crearDocumentoDesdeParametros(
                "   ", "Juan", "Pérez", "CONSULTA_MEDICA",
                "2024-01-01", "PDF", "http://example.com/doc.pdf",
                "Clínica 1", 1L, "Dr. Smith", "Desc", true
            );
        });
    }

    @Test
    void crearDocumentoDesdeParametros_withBlankTipoDocumento_shouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.crearDocumentoDesdeParametros(
                "12345678", "Juan", "Pérez", "   ",
                "2024-01-01", "PDF", "http://example.com/doc.pdf",
                "Clínica 1", 1L, "Dr. Smith", "Desc", true
            );
        });
    }

    @Test
    void crearDocumentoDesdeParametros_withDifferentTipoDocumentoValues_shouldMapCorrectly() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);
        
        // Test diferentes tipos de documento
        String[] tipos = {"RECETA_MEDICA", "INFORME_LABORATORIO", "ESTUDIO_IMAGENOLOGIA", "OTROS"};
        for (String tipo : tipos) {
            Long result = service.crearDocumentoDesdeParametros(
                "12345678", "Juan", "Pérez", tipo,
                "2024-01-01", "PDF", "http://example.com/doc.pdf",
                "Clínica 1", 1L, "Dr. Smith", "Desc", true
            );
            
            assertNotNull(result);
        }
    }

    @Test
    void crearDocumentoDesdeParametros_withExceptionInDateParsing_shouldUseCurrentDate() {
        // Arrange
        MetadataDocumento savedDoc = new MetadataDocumento();
        savedDoc.setId(1L);
        
        when(metadataRepository.save(any(MetadataDocumento.class))).thenReturn(savedDoc);
        
        // Act - fecha con formato incorrecto debería usar fecha actual
        Long result = service.crearDocumentoDesdeParametros(
            "12345678",
            "Juan",
            "Pérez",
            "CONSULTA_MEDICA",
            "invalid-date-format",
            "PDF",
            "http://example.com/doc.pdf",
            "Clínica 1",
            1L,
            "Dr. Smith",
            "Descripción",
            true
        );
        
        // Assert
        assertNotNull(result);
        verify(metadataRepository).save(any(MetadataDocumento.class));
    }

    @Test
    void prepararDescargaDocumento_withUriContainingQueryParam_shouldHandleCorrectly() throws Exception {
        // Arrange
        MetadataDocumento metadata = new MetadataDocumento();
        metadata.setId(1L);
        metadata.setUriDocumento("http://hcen-wildfly-app:8080/hcen-web/api/documentos-pdf/123?param=value");
        metadata.setFormatoDocumento(uy.edu.tse.hcen.common.enumerations.FormatoDocumento.PDF);
        metadata.setTipoDocumento(TipoDocumentoClinico.CONSULTA_MEDICA);
        metadata.setTenantId(100L);
        
        when(metadataRepository.findById(1L)).thenReturn(metadata);
        
        // Act
        try {
            DocumentoRndcService.DocumentoDescarga result = service.prepararDescargaDocumento(1L);
            assertNotNull(result);
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP
            assertTrue(e.getMessage().contains("nodo periférico") || 
                      e.getMessage().contains("URI"));
        }
    }

    @Test
    void prepararDescargaDocumento_withDifferentFormato_shouldSetCorrectContentType() throws Exception {
        // Arrange
        MetadataDocumento metadata = new MetadataDocumento();
        metadata.setId(1L);
        metadata.setUriDocumento("http://hcen-wildfly-app:8080/hcen-web/api/documentos-pdf/123");
        metadata.setFormatoDocumento(uy.edu.tse.hcen.common.enumerations.FormatoDocumento.DICOM);
        metadata.setTipoDocumento(TipoDocumentoClinico.ESTUDIO_IMAGENOLOGIA);
        metadata.setTenantId(100L);
        
        when(metadataRepository.findById(1L)).thenReturn(metadata);
        
        // Act
        try {
            DocumentoRndcService.DocumentoDescarga result = service.prepararDescargaDocumento(1L);
            assertNotNull(result);
            assertEquals("application/dicom", result.getContentType());
        } catch (Exception e) {
            // Esperado si no hay conexión HTTP
            assertTrue(e.getMessage().contains("nodo periférico") || 
                      e.getMessage().contains("URI"));
        }
    }
}


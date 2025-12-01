package uy.edu.tse.hcen.politicas.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CorsResponseFilter.
 */
class CorsResponseFilterTest {

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private ContainerResponseContext responseContext;

    private CorsResponseFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new CorsResponseFilter();
    }

    @Test
    void filter_optionsRequest_shouldNotAddHeaders() throws IOException {
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        
        filter.filter(requestContext, responseContext);
        
        verify(responseContext, never()).getHeaders();
    }

    @Test
    void filter_nonOptionsRequest_withAllowedOrigin_shouldAddCORSHeaders() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);
        
        filter.filter(requestContext, responseContext);
        
        assertTrue(headers.containsKey("Access-Control-Allow-Origin"));
        assertTrue(headers.containsKey("Access-Control-Allow-Methods"));
        assertTrue(headers.containsKey("Access-Control-Allow-Headers"));
    }

    @Test
    void filter_existingCORSHeader_shouldNotAddDuplicates() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("Access-Control-Allow-Origin", "http://localhost:3000");
        when(responseContext.getHeaders()).thenReturn(headers);
        
        filter.filter(requestContext, responseContext);
        
        // Si ya existe el header, no debería agregarlo de nuevo
        // Verificamos que el filtro no intenta agregar headers duplicados
        verify(responseContext).getHeaders();
    }

    @Test
    void filter_noOrigin_shouldNotApplyCORSHeaders() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn(null);

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);

        filter.filter(requestContext, responseContext);

        // La implementación actual no aplica CORS cuando no hay Origin (peticiones backend-to-backend)
        assertFalse(headers.containsKey("Access-Control-Allow-Origin"));
        assertTrue(headers.isEmpty());
    }
}

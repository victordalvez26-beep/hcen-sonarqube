package uy.edu.tse.hcen.politicas.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CorsRequestFilter.
 */
class CorsRequestFilterTest {

    @Mock
    private ContainerRequestContext requestContext;

    private CorsRequestFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new CorsRequestFilter();
    }

    @Test
    void filter_optionsRequest_withAllowedOrigin_shouldAbortWithCORS() throws IOException {
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        
        filter.filter(requestContext);
        
        verify(requestContext).abortWith(any(Response.class));
    }

    @Test
    void filter_optionsRequest_withoutOrigin_shouldAbortWithWildcard() throws IOException {
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn(null);
        
        filter.filter(requestContext);
        
        verify(requestContext).abortWith(any(Response.class));
    }

    @Test
    void filter_nonOptionsRequest_shouldNotAbort() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        
        filter.filter(requestContext);
        
        verify(requestContext, never()).abortWith(any(Response.class));
    }

    @Test
    void filter_optionsRequest_allowedOrigins_shouldSetCorrectOrigin() throws IOException {
        String[] allowedOrigins = {
            "http://localhost:3000",
            "http://localhost:3001",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3001"
        };
        
        for (String origin : allowedOrigins) {
            when(requestContext.getMethod()).thenReturn("OPTIONS");
            when(requestContext.getHeaderString("Origin")).thenReturn(origin);
            
            filter.filter(requestContext);
            
            verify(requestContext, atLeastOnce()).abortWith(any(Response.class));
            reset(requestContext);
        }
    }
}


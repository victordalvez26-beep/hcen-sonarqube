package uy.edu.tse.hcen.rest.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests para CorsResponseFilter.
 */
class CorsResponseFilterTest {

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private ContainerResponseContext responseContext;

    @Mock
    private MultivaluedMap<String, Object> responseHeaders;

    private CorsResponseFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new CorsResponseFilter();
        when(responseContext.getHeaders()).thenReturn(responseHeaders);
    }

    @Test
    void filter_withOrigin_shouldAddCorsHeaders() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, atLeastOnce()).putSingle(eq("Access-Control-Allow-Origin"), anyString());
        verify(responseHeaders, atLeastOnce()).putSingle(eq("Access-Control-Allow-Methods"), anyString());
        verify(responseHeaders, atLeastOnce()).putSingle(eq("Access-Control-Allow-Headers"), anyString());
    }

    @Test
    void filter_withNullOrigin_shouldStillAddHeaders() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn(null);
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, atLeastOnce()).putSingle(anyString(), anyString());
    }

    @Test
    void filter_withEmptyOrigin_shouldStillAddHeaders() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, atLeastOnce()).putSingle(anyString(), anyString());
    }

    @Test
    void filter_withDifferentOrigin_shouldAddHeaders() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://example.com");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, atLeastOnce()).putSingle(anyString(), anyString());
    }

    @Test
    void filter_shouldAddAllowCredentials() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, atLeastOnce()).putSingle(eq("Access-Control-Allow-Credentials"), eq("true"));
    }

    @Test
    void filter_shouldAddMaxAge() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, atLeastOnce()).putSingle(eq("Access-Control-Max-Age"), anyString());
    }

    @Test
    void filter_withHttpsOrigin_shouldHandle() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("https://example.com");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, atLeastOnce()).putSingle(anyString(), anyString());
    }

    @Test
    void filter_withPortInOrigin_shouldHandle() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:8080");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, atLeastOnce()).putSingle(anyString(), anyString());
    }

    @Test
    void filter_withSubdomainOrigin_shouldHandle() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://app.example.com");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, atLeastOnce()).putSingle(anyString(), anyString());
    }

    @Test
    void filter_withOptionsMethod_shouldSkip() throws IOException {
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, never()).putSingle(anyString(), anyString());
    }

    @Test
    void filter_withExistingHeaders_shouldSkip() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey("Access-Control-Allow-Origin")).thenReturn(true);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders, never()).putSingle(anyString(), anyString());
    }

    @Test
    void filter_shouldNotThrowException() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        assertDoesNotThrow(() -> {
            filter.filter(requestContext, responseContext);
        });
    }

    @Test
    void filter_withLocalhostOrigin_shouldSetCorrectOrigin() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders).putSingle(eq("Access-Control-Allow-Origin"), eq("http://localhost:3000"));
    }

    @Test
    void filter_with127Origin_shouldSetCorrectOrigin() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://127.0.0.1:8080");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders).putSingle(eq("Access-Control-Allow-Origin"), eq("http://127.0.0.1:8080"));
    }

    @Test
    void filter_withElasticCloudOrigin_shouldSetCorrectOrigin() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("https://app.web.elasticloud.uy");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders).putSingle(eq("Access-Control-Allow-Origin"), eq("https://app.web.elasticloud.uy"));
    }

    @Test
    void filter_withVercelOrigin_shouldSetCorrectOrigin() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("https://app.vercel.app");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders).putSingle(eq("Access-Control-Allow-Origin"), eq("https://app.vercel.app"));
    }

    @Test
    void filter_withNullOrigin_shouldSetDefaultOrigin() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn(null);
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders).putSingle(eq("Access-Control-Allow-Origin"), eq("http://localhost:3000"));
    }

    @Test
    void filter_withUnknownOrigin_shouldSetDefaultOrigin() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("https://unknown.com");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders).putSingle(eq("Access-Control-Allow-Origin"), eq("http://localhost:3000"));
    }

    @Test
    void filter_shouldSetAllRequiredHeaders() throws IOException {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        when(responseHeaders.containsKey(anyString())).thenReturn(false);
        
        filter.filter(requestContext, responseContext);
        
        verify(responseHeaders).putSingle(eq("Access-Control-Allow-Origin"), anyString());
        verify(responseHeaders).putSingle(eq("Access-Control-Allow-Methods"), anyString());
        verify(responseHeaders).putSingle(eq("Access-Control-Allow-Headers"), anyString());
        verify(responseHeaders).putSingle(eq("Access-Control-Allow-Credentials"), eq("true"));
        verify(responseHeaders).putSingle(eq("Access-Control-Max-Age"), anyString());
    }
}

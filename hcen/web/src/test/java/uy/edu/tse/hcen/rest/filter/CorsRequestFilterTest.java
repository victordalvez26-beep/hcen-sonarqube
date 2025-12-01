package uy.edu.tse.hcen.rest.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests para CorsRequestFilter.
 */
class CorsRequestFilterTest {

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private MultivaluedMap<String, String> headers;

    private CorsRequestFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new CorsRequestFilter();
        when(requestContext.getHeaders()).thenReturn(headers);
    }

    @Test
    void filter_withOptionsMethod_shouldAbort() throws IOException {
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        
        filter.filter(requestContext);
        
        verify(requestContext).abortWith(any());
    }

    @Test
    void filter_withGetMethod_shouldNotAbort() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        
        filter.filter(requestContext);
        
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void filter_withPostMethod_shouldNotAbort() throws IOException {
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        
        filter.filter(requestContext);
        
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void filter_withNullOrigin_shouldNotAbort() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn(null);
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        
        filter.filter(requestContext);
        
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void filter_withEmptyOrigin_shouldNotAbort() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        
        filter.filter(requestContext);
        
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void filter_withDifferentOrigins_shouldHandle() throws IOException {
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://example.com");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        
        filter.filter(requestContext);
        
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void filter_withPutMethod_shouldNotAbort() throws IOException {
        when(requestContext.getMethod()).thenReturn("PUT");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        
        filter.filter(requestContext);
        
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void filter_withDeleteMethod_shouldNotAbort() throws IOException {
        when(requestContext.getMethod()).thenReturn("DELETE");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(mock(jakarta.ws.rs.core.UriInfo.class));
        when(requestContext.getUriInfo().getPath()).thenReturn("/api/test");
        
        filter.filter(requestContext);
        
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void filter_withOptionsAndLocalhostOrigin_shouldAbortWithCorrectOrigin() throws IOException {
        jakarta.ws.rs.core.UriInfo uriInfo = mock(jakarta.ws.rs.core.UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/api/test");
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        
        filter.filter(requestContext);
        
        verify(requestContext).abortWith(argThat(response -> {
            jakarta.ws.rs.core.Response r = (jakarta.ws.rs.core.Response) response;
            return r.getHeaderString("Access-Control-Allow-Origin").equals("http://localhost:3000");
        }));
    }

    @Test
    void filter_withOptionsAnd127Origin_shouldAbortWithCorrectOrigin() throws IOException {
        jakarta.ws.rs.core.UriInfo uriInfo = mock(jakarta.ws.rs.core.UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/api/test");
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://127.0.0.1:8080");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        
        filter.filter(requestContext);
        
        verify(requestContext).abortWith(argThat(response -> {
            jakarta.ws.rs.core.Response r = (jakarta.ws.rs.core.Response) response;
            return r.getHeaderString("Access-Control-Allow-Origin").equals("http://127.0.0.1:8080");
        }));
    }

    @Test
    void filter_withOptionsAndElasticCloudOrigin_shouldAbortWithCorrectOrigin() throws IOException {
        jakarta.ws.rs.core.UriInfo uriInfo = mock(jakarta.ws.rs.core.UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/api/test");
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn("https://app.web.elasticloud.uy");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        
        filter.filter(requestContext);
        
        verify(requestContext).abortWith(argThat(response -> {
            jakarta.ws.rs.core.Response r = (jakarta.ws.rs.core.Response) response;
            return r.getHeaderString("Access-Control-Allow-Origin").equals("https://app.web.elasticloud.uy");
        }));
    }

    @Test
    void filter_withOptionsAndVercelOrigin_shouldAbortWithCorrectOrigin() throws IOException {
        jakarta.ws.rs.core.UriInfo uriInfo = mock(jakarta.ws.rs.core.UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/api/test");
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn("https://app.vercel.app");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        
        filter.filter(requestContext);
        
        verify(requestContext).abortWith(argThat(response -> {
            jakarta.ws.rs.core.Response r = (jakarta.ws.rs.core.Response) response;
            return r.getHeaderString("Access-Control-Allow-Origin").equals("https://app.vercel.app");
        }));
    }

    @Test
    void filter_withOptionsAndNullOrigin_shouldAbortWithDefaultOrigin() throws IOException {
        jakarta.ws.rs.core.UriInfo uriInfo = mock(jakarta.ws.rs.core.UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/api/test");
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn(null);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        
        filter.filter(requestContext);
        
        verify(requestContext).abortWith(argThat(response -> {
            jakarta.ws.rs.core.Response r = (jakarta.ws.rs.core.Response) response;
            return r.getHeaderString("Access-Control-Allow-Origin").equals("http://localhost:3000");
        }));
    }

    @Test
    void filter_withOptionsAndUnknownOrigin_shouldAbortWithDefaultOrigin() throws IOException {
        jakarta.ws.rs.core.UriInfo uriInfo = mock(jakarta.ws.rs.core.UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/api/test");
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn("https://unknown.com");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        
        filter.filter(requestContext);
        
        verify(requestContext).abortWith(argThat(response -> {
            jakarta.ws.rs.core.Response r = (jakarta.ws.rs.core.Response) response;
            return r.getHeaderString("Access-Control-Allow-Origin").equals("http://localhost:3000");
        }));
    }

    @Test
    void filter_withOptions_shouldSetCorrectHeaders() throws IOException {
        jakarta.ws.rs.core.UriInfo uriInfo = mock(jakarta.ws.rs.core.UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/api/test");
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(requestContext.getHeaderString("Origin")).thenReturn("http://localhost:3000");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        
        filter.filter(requestContext);
        
        verify(requestContext).abortWith(argThat(response -> {
            jakarta.ws.rs.core.Response r = (jakarta.ws.rs.core.Response) response;
            return r.getHeaderString("Access-Control-Allow-Methods") != null &&
                   r.getHeaderString("Access-Control-Allow-Headers") != null &&
                   r.getHeaderString("Access-Control-Max-Age") != null &&
                   r.getHeaderString("Access-Control-Allow-Credentials") != null;
        }));
    }
}

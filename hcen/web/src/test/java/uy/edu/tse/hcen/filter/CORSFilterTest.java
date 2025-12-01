package uy.edu.tse.hcen.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CORSFilter.
 */
class CORSFilterTest {

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private CORSFilter corsFilter;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        corsFilter = new CORSFilter();
        corsFilter.init(filterConfig);
    }

    @Test
    void doFilter_localhostOrigin_shouldSetCORSHeaders() throws Exception {
        when(request.getHeader("Origin")).thenReturn("http://localhost:3000");
        when(request.getMethod()).thenReturn("GET");
        
        corsFilter.doFilter(request, response, filterChain);
        
        verify(response).setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        verify(response).setHeader("Access-Control-Allow-Credentials", "true");
        verify(response).setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_elasticCloudOrigin_shouldSetCORSHeaders() throws Exception {
        when(request.getHeader("Origin")).thenReturn("https://app.web.elasticloud.uy");
        when(request.getMethod()).thenReturn("GET");
        
        corsFilter.doFilter(request, response, filterChain);
        
        verify(response).setHeader("Access-Control-Allow-Origin", "https://app.web.elasticloud.uy");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_vercelOrigin_shouldSetCORSHeaders() throws Exception {
        when(request.getHeader("Origin")).thenReturn("https://app.vercel.app");
        when(request.getMethod()).thenReturn("GET");
        
        corsFilter.doFilter(request, response, filterChain);
        
        verify(response).setHeader("Access-Control-Allow-Origin", "https://app.vercel.app");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_optionsRequest_shouldReturnOkAndNotChain() throws Exception {
        when(request.getHeader("Origin")).thenReturn("http://localhost:3000");
        when(request.getMethod()).thenReturn("OPTIONS");
        
        corsFilter.doFilter(request, response, filterChain);
        
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_noOrigin_shouldUseDefaultLocalhost() throws Exception {
        when(request.getHeader("Origin")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        
        corsFilter.doFilter(request, response, filterChain);
        
        verify(response).setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_unknownOrigin_shouldUseDefaultLocalhost() throws Exception {
        when(request.getHeader("Origin")).thenReturn("http://unknown-origin.com");
        when(request.getMethod()).thenReturn("GET");
        
        corsFilter.doFilter(request, response, filterChain);
        
        verify(response).setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void init_shouldInitialize() throws Exception {
        CORSFilter filter = new CORSFilter();
        filter.init(filterConfig);
        
        // Si no lanza excepción, el init funcionó correctamente
        assertNotNull(filter);
    }

    @Test
    void destroy_shouldComplete() {
        corsFilter.destroy();
        
        // Si no lanza excepción, el destroy funcionó correctamente
        assertNotNull(corsFilter);
    }

    @Test
    void doFilter_127001Origin_shouldSetCORSHeaders() throws Exception {
        when(request.getHeader("Origin")).thenReturn("http://127.0.0.1:3000");
        when(request.getMethod()).thenReturn("POST");
        
        corsFilter.doFilter(request, response, filterChain);
        
        verify(response).setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:3000");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldSetAllCORSHeaders() throws Exception {
        when(request.getHeader("Origin")).thenReturn("http://localhost:3000");
        when(request.getMethod()).thenReturn("GET");
        
        corsFilter.doFilter(request, response, filterChain);
        
        verify(response).setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        verify(response).setHeader("Access-Control-Allow-Credentials", "true");
        verify(response).setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        verify(response).setHeader(eq("Access-Control-Allow-Headers"), anyString());
        verify(response).setHeader("Access-Control-Max-Age", "3600");
    }

    @Test
    void doFilter_vercelComOrigin_shouldSetCORSHeaders() throws Exception {
        when(request.getHeader("Origin")).thenReturn("https://app.vercel.com");
        when(request.getMethod()).thenReturn("PUT");
        
        corsFilter.doFilter(request, response, filterChain);
        
        verify(response).setHeader("Access-Control-Allow-Origin", "https://app.vercel.com");
        verify(filterChain).doFilter(request, response);
    }
}

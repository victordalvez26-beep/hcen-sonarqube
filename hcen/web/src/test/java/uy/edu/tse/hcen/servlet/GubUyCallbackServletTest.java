package uy.edu.tse.hcen.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.service.GubUyCallbackService;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests para GubUyCallbackServlet.
 */
class GubUyCallbackServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private GubUyCallbackService callbackService;

    @Mock
    private java.io.PrintWriter printWriter;

    private GubUyCallbackServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new GubUyCallbackServlet();
        
        Field serviceField = GubUyCallbackServlet.class.getDeclaredField("callbackService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, callbackService);
    }

    @Test
    void doGet_withApiPath_shouldReturnEarly() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        
        servlet.doGet(request, response);
        
        verify(response, never()).getWriter();
        verify(callbackService, never()).processCallback(anyString(), anyString(), anyString(), any());
    }

    @Test
    void doGet_withNoParams_shouldShowWelcomePage() throws Exception {
        when(request.getRequestURI()).thenReturn("/");
        when(request.getParameter("code")).thenReturn(null);
        when(request.getParameter("error")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        verify(printWriter, atLeastOnce()).println(anyString());
    }

    @Test
    void doGet_withCode_shouldProcessCallback() throws Exception {
        when(request.getRequestURI()).thenReturn("/callback");
        when(request.getParameter("code")).thenReturn("test-code-123");
        when(request.getParameter("error")).thenReturn(null);
        when(request.getParameter("state")).thenReturn(null);
        when(callbackService.processCallback(anyString(), isNull(), isNull(), any()))
            .thenReturn("http://frontend.com?success=true");
        
        servlet.doGet(request, response);
        
        verify(callbackService).processCallback(eq("test-code-123"), isNull(), isNull(), any());
        verify(response).sendRedirect(anyString());
    }

    @Test
    void doGet_withError_shouldProcessCallback() throws Exception {
        when(request.getRequestURI()).thenReturn("/callback");
        when(request.getParameter("code")).thenReturn(null);
        when(request.getParameter("error")).thenReturn("access_denied");
        when(request.getParameter("state")).thenReturn(null);
        when(callbackService.processCallback(isNull(), eq("access_denied"), isNull(), any()))
            .thenReturn("http://frontend.com?error=access_denied");
        
        servlet.doGet(request, response);
        
        verify(callbackService).processCallback(isNull(), eq("access_denied"), isNull(), any());
        verify(response).sendRedirect(anyString());
    }

    @Test
    void doGet_withState_shouldProcessCallback() throws Exception {
        when(request.getRequestURI()).thenReturn("/callback");
        when(request.getParameter("code")).thenReturn("test-code");
        when(request.getParameter("error")).thenReturn(null);
        when(request.getParameter("state")).thenReturn("state-123");
        when(callbackService.processCallback(anyString(), isNull(), eq("state-123"), any()))
            .thenReturn("http://frontend.com?success=true");
        
        servlet.doGet(request, response);
        
        verify(callbackService).processCallback(eq("test-code"), isNull(), eq("state-123"), any());
        verify(response).sendRedirect(anyString());
    }

    @Test
    void doGet_withCallbackServiceException_shouldRedirectToError() throws Exception {
        when(request.getRequestURI()).thenReturn("/callback");
        when(request.getParameter("code")).thenReturn("test-code");
        when(request.getParameter("error")).thenReturn(null);
        when(request.getParameter("state")).thenReturn(null);
        when(callbackService.processCallback(anyString(), isNull(), isNull(), any()))
            .thenThrow(new RuntimeException("Service error"));
        
        servlet.doGet(request, response);
        
        verify(response).sendRedirect(argThat(url -> url.contains("error=internal_error")));
    }

    @Test
    void doGet_withEmptyCode_shouldShowWelcomePage() throws Exception {
        when(request.getRequestURI()).thenReturn("/");
        when(request.getParameter("code")).thenReturn("");
        when(request.getParameter("error")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        verify(printWriter, atLeastOnce()).println(anyString());
    }

    @Test
    void doGet_withQueryString_shouldLogIt() throws Exception {
        when(request.getRequestURI()).thenReturn("/callback");
        when(request.getQueryString()).thenReturn("code=test&state=123");
        when(request.getParameter("code")).thenReturn("test");
        when(request.getParameter("error")).thenReturn(null);
        when(request.getParameter("state")).thenReturn("123");
        when(callbackService.processCallback(anyString(), isNull(), anyString(), any()))
            .thenReturn("http://frontend.com?success=true");
        
        servlet.doGet(request, response);
        
        verify(callbackService).processCallback(eq("test"), isNull(), eq("123"), any());
    }

    @Test
    void doGet_withNullQueryString_shouldHandle() throws Exception {
        when(request.getRequestURI()).thenReturn("/");
        when(request.getQueryString()).thenReturn(null);
        when(request.getParameter("code")).thenReturn(null);
        when(request.getParameter("error")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
    }

    @Test
    void doGet_withNullPath_shouldHandle() throws Exception {
        when(request.getRequestURI()).thenReturn(null);
        when(request.getParameter("code")).thenReturn(null);
        when(request.getParameter("error")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
    }

    @Test
    void doGet_withApiPathStartingWithSlash_shouldReturnEarly() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        
        servlet.doGet(request, response);
        
        verify(response, never()).getWriter();
        verify(callbackService, never()).processCallback(anyString(), anyString(), anyString(), any());
    }

    @Test
    void doGet_withApiPathNoSlash_shouldReturnEarly() throws Exception {
        when(request.getRequestURI()).thenReturn("api/test");
        when(request.getQueryString()).thenReturn(null);
        
        servlet.doGet(request, response);
        
        verify(response, never()).getWriter();
        verify(callbackService, never()).processCallback(anyString(), anyString(), anyString(), any());
    }

    @Test
    void doGet_withIOException_shouldHandle() throws Exception {
        when(request.getRequestURI()).thenReturn("/callback");
        when(request.getParameter("code")).thenReturn("test-code");
        when(request.getParameter("error")).thenReturn(null);
        when(request.getParameter("state")).thenReturn(null);
        when(callbackService.processCallback(anyString(), isNull(), isNull(), any()))
            .thenThrow(new IOException("IO error"));
        
        servlet.doGet(request, response);
        
        verify(response).sendRedirect(argThat(url -> url.contains("error=internal_error")));
    }
}

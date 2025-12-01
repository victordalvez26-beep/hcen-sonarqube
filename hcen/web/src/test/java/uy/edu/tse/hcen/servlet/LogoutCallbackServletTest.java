package uy.edu.tse.hcen.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import uy.edu.tse.hcen.util.CookieUtil;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LogoutCallbackServlet.
 */
class LogoutCallbackServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private LogoutCallbackServlet servlet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new LogoutCallbackServlet();
    }

    @Test
    void doGet_validState_shouldDeleteCookieAndRedirect() throws Exception {
        String state = "test_state";
        String requestUrl = "http://localhost:8080/logout";
        String origin = "http://localhost:3000";
        String deleteCookieHeader = "session_token=; Path=/; HttpOnly; SameSite=None; Secure; Max-Age=0";
        
        when(request.getParameter("state")).thenReturn(state);
        when(request.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
        when(request.getHeader("Origin")).thenReturn(origin);
        
        try (MockedStatic<CookieUtil> cookieUtilMock = mockStatic(CookieUtil.class)) {
            
            cookieUtilMock.when(() -> CookieUtil.buildDeleteCookieHeader(requestUrl, origin))
                    .thenReturn(deleteCookieHeader);
            
            servlet.doGet(request, response);
            
            verify(response).setHeader("Set-Cookie", deleteCookieHeader);
            // Verificar que se hace redirect (puede ser a cualquier URL, no mockeamos GubUyConfig)
            verify(response).sendRedirect(anyString());
        }
    }

    @Test
    void doGet_noState_shouldStillProcessLogout() throws Exception {
        String requestUrl = "http://localhost:8080/logout";
        String origin = "http://localhost:3000";
        String deleteCookieHeader = "session_token=; Path=/; HttpOnly; SameSite=None; Secure; Max-Age=0";
        
        when(request.getParameter("state")).thenReturn(null);
        when(request.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
        when(request.getHeader("Origin")).thenReturn(origin);
        
        try (MockedStatic<CookieUtil> cookieUtilMock = mockStatic(CookieUtil.class)) {
            
            cookieUtilMock.when(() -> CookieUtil.buildDeleteCookieHeader(requestUrl, origin))
                    .thenReturn(deleteCookieHeader);
            
            servlet.doGet(request, response);
            
            verify(response).setHeader("Set-Cookie", deleteCookieHeader);
            // Verificar que se hace redirect (puede ser a cualquier URL, no mockeamos GubUyConfig)
            verify(response).sendRedirect(anyString());
        }
    }

    @Test
    void doGet_noOrigin_shouldStillProcessLogout() throws Exception {
        String state = "test_state";
        String requestUrl = "http://localhost:8080/logout";
        String deleteCookieHeader = "session_token=; Path=/; HttpOnly; SameSite=None; Secure; Max-Age=0";
        
        when(request.getParameter("state")).thenReturn(state);
        when(request.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
        when(request.getHeader("Origin")).thenReturn(null);
        
        try (MockedStatic<CookieUtil> cookieUtilMock = mockStatic(CookieUtil.class)) {
            
            cookieUtilMock.when(() -> CookieUtil.buildDeleteCookieHeader(requestUrl, null))
                    .thenReturn(deleteCookieHeader);
            
            servlet.doGet(request, response);
            
            verify(response).setHeader("Set-Cookie", deleteCookieHeader);
            // Verificar que se hace redirect (puede ser a cualquier URL, no mockeamos GubUyConfig)
            verify(response).sendRedirect(anyString());
        }
    }
}

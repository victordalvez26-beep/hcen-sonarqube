package uy.edu.tse.hcen.prestador.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher requestDispatcher;

    @InjectMocks
    private ConfigServlet servlet;

    @BeforeEach
    void setUp() {
        // Configuración básica - solo mockear lo necesario
    }

    @Test
    void testInit() throws ServletException {
        servlet.init();
        assertNotNull(servlet);
    }

    @Test
    void testDoGet_ForwardsToConfigHtml() throws Exception {
        when(request.getRequestDispatcher("/config.html")).thenReturn(requestDispatcher);

        servlet.doGet(request, response);

        verify(request, atLeastOnce()).getRequestDispatcher("/config.html");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void testDoPost_SavesApiKeyAndOrigin() throws Exception {
        String apiKey = "test-api-key-12345";
        String origin = "http://localhost:8080";
        String contextPath = "/prestador-salud";

        when(request.getParameter("apiKey")).thenReturn(apiKey);
        when(request.getParameter("origin")).thenReturn(origin);
        when(request.getContextPath()).thenReturn(contextPath);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("apiKey", apiKey);
        verify(session).setAttribute("origin", origin);
        verify(response).sendRedirect(contextPath + "/");
    }

    @Test
    void testDoPost_WithNullApiKey() throws Exception {
        String origin = "http://localhost:8080";
        String contextPath = "/prestador-salud";

        when(request.getParameter("apiKey")).thenReturn(null);
        when(request.getParameter("origin")).thenReturn(origin);
        when(request.getContextPath()).thenReturn(contextPath);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("apiKey", null);
        verify(session).setAttribute("origin", origin);
        verify(response).sendRedirect(contextPath + "/");
    }

    @Test
    void testDoPost_WithNullOrigin() throws Exception {
        String apiKey = "test-api-key-12345";
        String contextPath = "/prestador-salud";

        when(request.getParameter("apiKey")).thenReturn(apiKey);
        when(request.getParameter("origin")).thenReturn(null);
        when(request.getContextPath()).thenReturn(contextPath);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("apiKey", apiKey);
        verify(session).setAttribute("origin", null);
        verify(response).sendRedirect(contextPath + "/");
    }

    @Test
    void testDoPost_WithEmptyValues() throws Exception {
        String contextPath = "/prestador-salud";

        when(request.getParameter("apiKey")).thenReturn("");
        when(request.getParameter("origin")).thenReturn("");
        when(request.getContextPath()).thenReturn(contextPath);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("apiKey", "");
        verify(session).setAttribute("origin", "");
        verify(response).sendRedirect(contextPath + "/");
    }

    @Test
    void testDoPost_WithRootContextPath() throws Exception {
        String apiKey = "test-api-key";
        String origin = "http://localhost:8080";

        when(request.getParameter("apiKey")).thenReturn(apiKey);
        when(request.getParameter("origin")).thenReturn(origin);
        when(request.getContextPath()).thenReturn("");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/");
    }
}


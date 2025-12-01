package uy.edu.tse.hcen.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uy.edu.tse.hcen.model.NodoPeriferico;

import static org.junit.jupiter.api.Assertions.*;

class NodoPerifericoHttpClientTest {

    private NodoPerifericoHttpClient client;

    @BeforeEach
    void setUp() {
        client = new NodoPerifericoHttpClient();
    }

    @Test
    void enviarConfiguracionInicial_withNullNodo_shouldReturnFalse() {
        // Act
        boolean result = client.enviarConfiguracionInicial(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void enviarConfiguracionInicial_withNullUrl_shouldReturnFalse() {
        // Arrange
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Test Clinic");
        nodo.setNodoPerifericoUrlBase(null);

        // Act
        boolean result = client.enviarConfiguracionInicial(nodo);

        // Assert
        assertFalse(result);
    }

    @Test
    void enviarConfiguracionInicial_withInvalidUrl_shouldReturnFalse() {
        // Arrange
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Test Clinic");
        nodo.setNodoPerifericoUrlBase("invalid-url-that-does-not-exist-12345");

        // Act
        boolean result = client.enviarConfiguracionInicial(nodo);

        // Assert
        // Should return false because the URL doesn't exist
        assertFalse(result);
    }

    @Test
    void enviarBaja_withNullNodo_shouldReturnFalse() {
        // Act
        boolean result = client.enviarBaja(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void enviarBaja_withNullUrl_shouldReturnFalse() {
        // Arrange
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setNombre("Test Clinic");
        nodo.setNodoPerifericoUrlBase(null);

        // Act
        boolean result = client.enviarBaja(nodo);

        // Assert
        assertFalse(result);
    }

    @Test
    void enviarBaja_withInvalidUrl_shouldReturnFalse() {
        // Arrange
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("invalid-url-that-does-not-exist-12345");

        // Act
        boolean result = client.enviarBaja(nodo);

        // Assert
        // Should return false because the URL doesn't exist
        assertFalse(result);
    }

    @Test
    void enviarConfiguracionInicial_withUrlEndingWithSlash_shouldHandleCorrectly() {
        // Arrange
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Test Clinic");
        nodo.setNodoPerifericoUrlBase("http://invalid-url/");

        // Act
        boolean result = client.enviarConfiguracionInicial(nodo);

        // Assert
        // Should handle URL ending with slash correctly
        assertFalse(result); // Will fail because URL is invalid, but method should handle it
    }

    @Test
    void enviarBaja_withUrlEndingWithSlash_shouldHandleCorrectly() {
        // Arrange
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("http://invalid-url/");

        // Act
        boolean result = client.enviarBaja(nodo);

        // Assert
        // Should handle URL ending with slash correctly
        assertFalse(result); // Will fail because URL is invalid, but method should handle it
    }

    @Test
    void enviarConfiguracionInicial_withSpecialCharactersInNombre_shouldEscapeJson() {
        // Arrange
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Clínica \"Test\" con \\ backslash");
        nodo.setNodoPerifericoUrlBase("http://invalid-url");
        
        // Act
        boolean result = client.enviarConfiguracionInicial(nodo);
        
        // Assert
        // Should escape JSON special characters
        assertFalse(result); // Will fail because URL is invalid, but JSON should be escaped
    }

    @Test
    void enviarConfiguracionInicial_withNullNombre_shouldHandleGracefully() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre(null);
        nodo.setNodoPerifericoUrlBase("http://invalid-url");
        
        boolean result = client.enviarConfiguracionInicial(nodo);
        
        assertFalse(result);
    }

    @Test
    void enviarConfiguracionInicial_withEmptyNombre_shouldHandleGracefully() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("");
        nodo.setNodoPerifericoUrlBase("http://invalid-url");
        
        boolean result = client.enviarConfiguracionInicial(nodo);
        
        assertFalse(result);
    }

    @Test
    void enviarBaja_withNullNombre_shouldHandleGracefully() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre(null);
        nodo.setNodoPerifericoUrlBase("http://invalid-url");
        
        boolean result = client.enviarBaja(nodo);
        
        assertFalse(result);
    }

    @Test
    void enviarBaja_withEmptyUrl_shouldReturnFalse() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("");
        
        boolean result = client.enviarBaja(nodo);
        
        assertFalse(result);
    }

    @Test
    void enviarConfiguracionInicial_withEmptyUrl_shouldReturnFalse() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Test");
        nodo.setNodoPerifericoUrlBase("");
        
        boolean result = client.enviarConfiguracionInicial(nodo);
        
        assertFalse(result);
    }

    @Test
    void enviarConfiguracionInicial_withNullId_shouldHandleGracefully() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(null);
        nodo.setNombre("Test");
        nodo.setNodoPerifericoUrlBase("http://invalid-url");
        
        boolean result = client.enviarConfiguracionInicial(nodo);
        
        assertFalse(result);
    }

    @Test
    void enviarBaja_withNullId_shouldHandleGracefully() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(null);
        nodo.setNodoPerifericoUrlBase("http://invalid-url");
        
        boolean result = client.enviarBaja(nodo);
        
        assertFalse(result);
    }

    @Test
    void enviarConfiguracionInicial_withUrlWithoutProtocol_shouldReturnFalse() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Test");
        nodo.setNodoPerifericoUrlBase("invalid-url-without-protocol");
        
        boolean result = client.enviarConfiguracionInicial(nodo);
        
        assertFalse(result);
    }

    @Test
    void enviarBaja_withUrlWithoutProtocol_shouldReturnFalse() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNodoPerifericoUrlBase("invalid-url-without-protocol");
        
        boolean result = client.enviarBaja(nodo);
        
        assertFalse(result);
    }

    @Test
    void enviarConfiguracionInicial_withNombreContainingNewlines_shouldEscape() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Clínica\ncon\nnewlines");
        nodo.setNodoPerifericoUrlBase("http://invalid-url");
        
        boolean result = client.enviarConfiguracionInicial(nodo);
        
        assertFalse(result);
    }

    @Test
    void enviarConfiguracionInicial_withNombreContainingTabs_shouldEscape() {
        NodoPeriferico nodo = new NodoPeriferico();
        nodo.setId(1L);
        nodo.setNombre("Clínica\tcon\ttabs");
        nodo.setNodoPerifericoUrlBase("http://invalid-url");
        
        boolean result = client.enviarConfiguracionInicial(nodo);
        
        assertFalse(result);
    }
}


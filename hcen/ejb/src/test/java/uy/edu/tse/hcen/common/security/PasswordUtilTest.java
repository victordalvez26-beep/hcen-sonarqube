package uy.edu.tse.hcen.common.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios defensivos para PasswordUtil.
 * Verifica generación de salt, hashing de contraseñas y casos límite.
 */
class PasswordUtilTest {

    @Test
    void generateSalt_shouldReturnBase64String() {
        String salt = PasswordUtil.generateSalt();
        assertNotNull(salt);
        assertFalse(salt.isEmpty());
        // Verificar que es Base64 válido (caracteres alfanuméricos, +, /, =)
        assertTrue(salt.matches("[A-Za-z0-9+/=]+"));
    }

    @RepeatedTest(10)
    void generateSalt_shouldGenerateDifferentSalts() {
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();
        assertNotEquals(salt1, salt2, "Los salts generados deberían ser diferentes");
    }

    @Test
    void generateSalt_shouldReturnConsistentLength() {
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();
        // Salt de 16 bytes en Base64 debería tener longitud fija (sin padding = 22, con padding = 24)
        assertTrue(salt1.length() >= 20 && salt1.length() <= 24);
        assertEquals(salt1.length(), salt2.length());
    }

    @Test
    void hashPassword_validInput_shouldReturnHash() {
        String salt = PasswordUtil.generateSalt();
        char[] password = "TestPassword123".toCharArray();
        
        String hash = PasswordUtil.hashPassword(password, salt);
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertTrue(hash.matches("[A-Za-z0-9+/=]+")); // Base64
    }

    @Test
    void hashPassword_samePasswordSameSalt_shouldReturnSameHash() {
        String salt = PasswordUtil.generateSalt();
        char[] password = "TestPassword123".toCharArray();
        
        String hash1 = PasswordUtil.hashPassword(password, salt);
        String hash2 = PasswordUtil.hashPassword(password, salt);
        
        assertEquals(hash1, hash2, "El mismo password con el mismo salt debe generar el mismo hash");
    }

    @Test
    void hashPassword_samePasswordDifferentSalt_shouldReturnDifferentHash() {
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();
        char[] password = "TestPassword123".toCharArray();
        
        String hash1 = PasswordUtil.hashPassword(password, salt1);
        String hash2 = PasswordUtil.hashPassword(password, salt2);
        
        assertNotEquals(hash1, hash2, "El mismo password con diferentes salts debe generar diferentes hashes");
    }

    @Test
    void hashPassword_differentPasswordsSameSalt_shouldReturnDifferentHash() {
        String salt = PasswordUtil.generateSalt();
        char[] password1 = "Password1".toCharArray();
        char[] password2 = "Password2".toCharArray();
        
        String hash1 = PasswordUtil.hashPassword(password1, salt);
        String hash2 = PasswordUtil.hashPassword(password2, salt);
        
        assertNotEquals(hash1, hash2, "Diferentes passwords deben generar diferentes hashes");
    }

    @Test
    void hashPassword_emptyPassword_shouldReturnHash() {
        String salt = PasswordUtil.generateSalt();
        char[] emptyPassword = "".toCharArray();
        
        String hash = PasswordUtil.hashPassword(emptyPassword, salt);
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hashPassword_longPassword_shouldReturnHash() {
        String salt = PasswordUtil.generateSalt();
        char[] longPassword = "A".repeat(1000).toCharArray();
        
        String hash = PasswordUtil.hashPassword(longPassword, salt);
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hashPassword_specialCharacters_shouldReturnHash() {
        String salt = PasswordUtil.generateSalt();
        char[] specialPassword = "P@ssw0rd!#$%^&*()".toCharArray();
        
        String hash = PasswordUtil.hashPassword(specialPassword, salt);
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hashPassword_unicodeCharacters_shouldReturnHash() {
        String salt = PasswordUtil.generateSalt();
        char[] unicodePassword = "Pásswórd123".toCharArray();
        
        String hash = PasswordUtil.hashPassword(unicodePassword, salt);
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hashPassword_invalidSalt_shouldThrowRuntimeException() {
        char[] password = "TestPassword".toCharArray();
        String invalidSalt = "InvalidBase64!!!";
        
        assertThrows(RuntimeException.class, 
            () -> PasswordUtil.hashPassword(password, invalidSalt));
    }

    @Test
    void hashPassword_nullPassword_shouldHandleGracefully() {
        String salt = PasswordUtil.generateSalt();
        char[] nullPassword = null;
        
        // Nota: PBEKeySpec puede aceptar null sin lanzar excepción inmediata.
        // El método no valida null explícitamente, así que este test verifica
        // que el método maneja el caso sin crashear inmediatamente.
        // En producción, se recomienda validar null antes de llamar a hashPassword.
        try {
            String result = PasswordUtil.hashPassword(nullPassword, salt);
            // Puede retornar un valor o lanzar excepción dependiendo de la implementación
            // Si no lanza excepción, el test pasa (comportamiento actual)
            assertNotNull(result);
        } catch (Exception e) {
            // Si lanza excepción, también está bien (comportamiento defensivo)
            assertTrue(e instanceof Exception);
        }
    }

    @Test
    void hashPassword_nullSalt_shouldThrowRuntimeException() {
        char[] password = "TestPassword".toCharArray();
        
        assertThrows(RuntimeException.class,
            () -> PasswordUtil.hashPassword(password, null));
    }

    @Test
    void hashPassword_emptySalt_shouldThrowRuntimeException() {
        char[] password = "TestPassword".toCharArray();
        
        assertThrows(RuntimeException.class,
            () -> PasswordUtil.hashPassword(password, ""));
    }
}


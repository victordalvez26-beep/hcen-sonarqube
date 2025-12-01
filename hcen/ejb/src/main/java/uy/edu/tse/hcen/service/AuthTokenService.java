package uy.edu.tse.hcen.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import uy.edu.tse.hcen.dao.AuthTokenDAO;
import uy.edu.tse.hcen.model.AuthToken;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.logging.Logger;

@Stateless
public class AuthTokenService {
    
    private static final Logger LOGGER = Logger.getLogger(AuthTokenService.class.getName());
    private static final int TOKEN_LENGTH = 32; // 32 bytes = 44 caracteres en Base64
    private static final int EXPIRATION_SECONDS = 60; // 60 segundos
    
    @EJB
    private AuthTokenDAO authTokenDAO;
    
    /**
     * Genera un token temporal de autenticación.
     * 
     * @param jwtToken El JWT que se asociará con este token temporal
     * @param userUid El UID del usuario
     * @return El token temporal generado
     */
    public String generateTempToken(String jwtToken, String userUid) {
        // Generar token aleatorio seguro
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // Crear entidad AuthToken
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(EXPIRATION_SECONDS);
        AuthToken authToken = new AuthToken(token, jwtToken, userUid, expiresAt);
        
        // Guardar en base de datos
        authTokenDAO.save(authToken);
        
        LOGGER.info(String.format("Token temporal generado para usuario %s, expira en %d segundos", 
                userUid, EXPIRATION_SECONDS));
        
        return token;
    }
    
    /**
     * Valida e intercambia un token temporal por el JWT real.
     * El token se marca como usado después de ser intercambiado.
     * 
     * @param tempToken El token temporal
     * @return El JWT asociado, o null si el token es inválido
     */
    public String exchangeToken(String tempToken) {
        if (tempToken == null || tempToken.trim().isEmpty()) {
            LOGGER.warning("Token temporal vacío o null");
            return null;
        }
        
        AuthToken authToken = authTokenDAO.findByToken(tempToken);
        
        if (authToken == null) {
            LOGGER.warning("Token temporal no encontrado: " + tempToken.substring(0, Math.min(10, tempToken.length())) + "...");
            return null;
        }
        
        if (!authToken.isValid()) {
            LOGGER.warning(String.format("Token temporal inválido - Usado: %s, Expirado: %s", 
                    authToken.isUsed(), authToken.isExpired()));
            return null;
        }
        
        // Marcar como usado
        authTokenDAO.markAsUsed(tempToken);
        
        LOGGER.info(String.format("Token temporal intercambiado exitosamente para usuario %s", 
                authToken.getUserUid()));
        
        return authToken.getJwtToken();
    }
}


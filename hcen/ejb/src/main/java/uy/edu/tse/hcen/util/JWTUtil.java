package uy.edu.tse.hcen.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Logger;

public class JWTUtil {
    
    private static final Logger LOGGER = Logger.getLogger(JWTUtil.class.getName());
    
    private static final String SECRET_KEY = "TSE_2025_HCEN_SECRET_KEY_!@#$%^&*()";
    // Secret del componente periférico (decodificado de Base64)
    private static final String PERIPHERAL_SECRET_KEY = "mysupersecretkeyforhcenjwttokensshouldbelonger";
    private static final String ALGORITHM = "HmacSHA256";
    
    /**
     * Obtiene el secret del componente periférico desde variables de entorno.
     * Si no está disponible, usa el secret por defecto decodificado de Base64.
     */
    private static String getPeripheralSecret() {
        String secretEnv = System.getenv("JWT_SECRET_BASE64");
        if (secretEnv != null && !secretEnv.isBlank()) {
            try {
                byte[] decoded = java.util.Base64.getDecoder().decode(secretEnv);
                return new String(decoded, StandardCharsets.UTF_8);
            } catch (Exception e) {
                LOGGER.warning("Error decodificando JWT_SECRET_BASE64, usando default: " + e.getMessage());
            }
        }
        return PERIPHERAL_SECRET_KEY;
    }
    
    // Genera un JWT firmado
    // @param userUid: ID del usuario
    // @param expirationSeconds: Segundos hasta la expiración
    // @return JWT firmado
    public static String generateJWT(String userUid, long expirationSeconds) {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + expirationSeconds;
              
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String encodedHeader = base64UrlEncode(header);
            
            String payload = String.format(
                "{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d,\"iss\":\"HCEN\"}",
                userUid, now, exp
            );
            String encodedPayload = base64UrlEncode(payload);
            
            String dataToSign = encodedHeader + "." + encodedPayload;
            String signature = signHmacSHA256(dataToSign, SECRET_KEY);
            
            return dataToSign + "." + signature;
            
        } catch (Exception e) {
            LOGGER.severe("Error generando JWT: " + e.getMessage());
            throw new RuntimeException("Error generando JWT", e);
        }
    }
    
    // Valida un JWT y devuelve el userUid si es válido
    // @param jwt: JWT a validar
    // @return userUid: si es válido, null si no es válido o está expirado
    public static String validateJWT(String jwt) {
        try {
            if (jwt == null || jwt.isEmpty()) {
                return null;
            }
            
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                LOGGER.warning("JWT mal formado");
                return null;
            }
            
            String encodedHeader = parts[0];
            String encodedPayload = parts[1];
            String receivedSignature = parts[2];
            
            // Verificar firma - intentar con ambos secrets (HCEN Central y componente periférico)
            String dataToSign = encodedHeader + "." + encodedPayload;
            
            // Primero intentar con el secret de HCEN Central
            String expectedSignature = signHmacSHA256(dataToSign, SECRET_KEY);
            boolean signatureValid = expectedSignature.equals(receivedSignature);
            
            // Si no es válida, intentar con el secret del componente periférico
            if (!signatureValid) {
                String peripheralSecret = getPeripheralSecret();
                expectedSignature = signHmacSHA256(dataToSign, peripheralSecret);
                signatureValid = expectedSignature.equals(receivedSignature);
                
                if (signatureValid) {
                    LOGGER.info("JWT validado con secret del componente periférico");
                }
            }
            
            if (!signatureValid) {
                LOGGER.warning("Firma JWT inválida con ambos secrets");
                return null;
            }
            
            // Decodificar payload
            String payload = base64UrlDecode(encodedPayload);
            LOGGER.info("JWT payload decodificado: " + payload);
            
            // Extraer sub (userUid) y exp (expiración)
            // jjwt puede usar "sub" como string o como número
            String userUid = extractJsonField(payload, "sub");
            
            // También intentar extraer el tenantId si existe (para tokens del componente periférico)
            String tenantId = extractJsonField(payload, "tenantId");
            
            String expStr = extractJsonField(payload, "exp");
            
            if (userUid == null) {
                LOGGER.warning("JWT sin campo 'sub' (userUid) - Payload: " + payload);
                return null;
            }
            
            if (expStr == null) {
                LOGGER.warning("JWT sin campo 'exp' (expiración) - Payload: " + payload);
                return null;
            }
            
            // Verificar expiración
            long exp;
            try {
                exp = Long.parseLong(expStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("JWT con campo 'exp' inválido: " + expStr);
                return null;
            }
            
            long now = Instant.now().getEpochSecond();
            
            if (now > exp) {
                LOGGER.info("JWT expirado - exp: " + exp + ", now: " + now);
                return null;
            }
            
            LOGGER.info("JWT validado exitosamente - userUid: " + userUid + ", tenantId: " + tenantId);
            return userUid;
            
        } catch (Exception e) {
            LOGGER.severe("Error validando JWT: " + e.getMessage());
            return null;
        }
    }
    
    // Extrae el userUid de un JWT sin validar la firma (solo para debugging)
    public static String extractUserUid(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String payload = base64UrlDecode(parts[1]);
            return extractJsonField(payload, "sub");
        } catch (Exception e) {
            return null;
        }
    }
    
    // Extrae el tenantId de un JWT sin validar la firma
    public static String extractTenantId(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String payload = base64UrlDecode(parts[1]);
            return extractJsonField(payload, "tenantId");
        } catch (Exception e) {
            LOGGER.warning("Error extrayendo tenantId del JWT: " + e.getMessage());
            return null;
        }
    }
    
    // Métodos auxiliares
    private static String base64UrlEncode(String str) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }
    
    private static String base64UrlDecode(String str) {
        byte[] decoded = Base64.getUrlDecoder().decode(str);
        return new String(decoded, StandardCharsets.UTF_8);
    }
    
    private static String signHmacSHA256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        mac.init(secretKeySpec);
        byte[] signedBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes);
    }
    
    private static String extractJsonField(String json, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\":\"?([^,}\"]+)\"?";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1).replace("\"", "");
            }
        } catch (Exception e) {
            LOGGER.warning("Error extrayendo campo " + fieldName + ": " + e.getMessage());
        }
        return null;
    }
}


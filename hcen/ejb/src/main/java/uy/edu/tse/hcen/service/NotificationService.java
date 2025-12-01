package uy.edu.tse.hcen.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import uy.edu.tse.hcen.model.NotificationType;
import uy.edu.tse.hcen.model.UserNotificationPreferences;
import uy.edu.tse.hcen.repository.UserNotificationPreferencesRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;


/**
 * Servicio para enviar notificaciones push a través de Firebase Cloud Messaging (FCM).
 */
@Stateless
public class NotificationService {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());
    
    // Configuración de Firebase Service Account
    private static final String PROJECT_ID = "hcen-tse";
    private static final String CLIENT_EMAIL = "firebase-adminsdk-fbsvc@hcen-tse.iam.gserviceaccount.com";
    private static final String PRIVATE_KEY_ID = "bb3042bef598bae724f85df8cdf2fcc919fd0546";
    private static final String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCtzZcYDCPJ5zWR\n3y8Xgc/Koc+B/DctC9ajWtyLaBv+Alj6DMIF4bhSCm7mspRgdpDcU+Z/kC0OREkd\nfFjTxh4Guz7S2r78nrNsxM7bqUX9+nwtZcmc07x6xxQSjQgihJCrzVEtYBNxlS7j\nGMx7hh3QEUB3TAgUPBXiE0v94KN2AKHpXJkZbq4dB0/zY/CPmokbbXe7mwA6mTKt\ngKdRrWAcLFsyq5NMJ2gmXv2gOmIn+9O8NUw1gVyhdCCOEL1dMtTFYsxKL4ufC1MC\njwtU7G9Ygburjuq/xRQiHXyHqqsCvbw0pjwulEnHSzWSDTSlyZgF3/YMu4dhl2ua\ncsnL4g4TAgMBAAECggEABav1Mk5dx0j0RpOx8GJxatC8+iZGPAIW5F3NX1SWapjz\nrl/xikpDaZSi6dm0szErU6/+kEse1/MChSPPAkTaK1NmSXCPVaRtgS8q+6Aag0nu\no3dJjQSb3KXX0dh9sM1YAJiX/ZMVD1p+8opB91tOaGvSXrzscTyaD4vxqZHwbk6p\nLXevpCHYQL7S+VPL76iSMmz/qMRe3PFtaoBte7H/oNgGVWrWESHkCT38lb4UOWZj\n3zR71DYx7f/dWy63OgkaLWRO6CrjQ5QVhhJnlEK7rMoVhZqdT5kSDTX/h2VDF0BS\n9K/221yAAumh0sCi/CcAcLib4B3n3Ydm8A1d4yjuAQKBgQDhww+B9h3ouY7Zed2o\nx1C2x93R1h0MauzwT4w+L9Uhg4Lc07T4IrXmUkR0w5odjSeLAwZhgyOEUOPyO+LV\nhqiQPDx6o9AbhVPX6vmwgij85HSyTLgUms/UeL3/2yWhQELtDVGmYUVUSjMGx9rs\n3Pd77qluAxrwA299RUb1Hv6EUQKBgQDFFPX5oi4XbsDLrvp6cO0H6a6uJNmbjeHs\nCAc+0r6hNW6KKxzP/lsux7tac0oqz9KyB1hee9wm+YaFvJ/kPKU2/YU+62zAuROZ\nKIJ73GHHaw/zs7sJRWQiLnlp1XFE6QN7j63bTyQZ8q7q6dJ4sAEe/yWzrnEIRfv8\nH6seqsvHIwKBgQCe95380hOfYsGeivw0sxw3iCa+cbkEnGn4kNrckwvNTHAiFISS\nUMaqxnTjjzP+6PzWGqwsj427xsWrNNX1qpsY7QFdDeok5s3x9MgarJw17pvpTLI1\n+AitwNNwzJwDnl9B0iYTNNz/jDu/44xZSwKM5rGxAEjAhnrDx7s+MLPg8QKBgQCe\nQxrUAFyrDJFd/4G9WXm+5PUVexoURHesJzZKSMo0gsflxIE6tbNbeaEGHZbtkj5M\nIIHiPeyoj7BrLPHpQpoZ8rRRtpR6nyUR1OoTtQIANgV1WltUMpkaqd9uEa0H/WaC\njvq6Jm+DNtMoaeRObmg4EASiqe6zCiA0cunjkpxZqQKBgEtTjoqdPItoup23lqcR\nt2vIAfcZ2dl4acC9QYrErW4BFJfhhmBOsqlMzILyp0zoN8+Cz6/5Ju25mR+y+PHl\nn6JxtBW0t9YbCS9yBLbtO9DKhAOrF+DobbVWRwvKawMNzZJMPeNBdnixQRsQa7kg\nxLWDvN0tGfmKnG1ZP53hnZEV\n-----END PRIVATE KEY-----\n";
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String FCM_SEND_URI = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";
    
    @Inject
    private UserNotificationPreferencesRepository preferencesRepository;
    
    /**
     * Envía una notificación push a un usuario específico.
     * Verifica primero si el usuario tiene habilitado ese tipo de notificación.
     * 
     * @param userUid Identificador único del usuario
     * @param notificationType Tipo de notificación
     * @param title Título de la notificación
     * @param body Cuerpo del mensaje
     * @return true si la notificación se envió exitosamente, false en caso contrario
     */
    public boolean sendNotification(String userUid, NotificationType notificationType, String title, String body) {
        try {
            // 1. Verificar preferencias del usuario
            UserNotificationPreferences preferences = preferencesRepository.findByUserUid(userUid);
            
            if (preferences == null) {
                LOGGER.warning("No se encontraron preferencias de notificaciones para el usuario: " + userUid);
                return false;
            }
            
            // 2. Verificar si el tipo de notificación está habilitado
            if (!preferences.isNotificationEnabled(notificationType)) {
                LOGGER.info("El usuario " + userUid + " tiene deshabilitadas las notificaciones de tipo: " + notificationType.getCode());
                return false;
            }
            
            // 3. Verificar si tiene device token
            String deviceToken = preferences.getDeviceToken();
            if (deviceToken == null || deviceToken.isEmpty()) {
                LOGGER.warning("El usuario " + userUid + " no tiene un device token registrado");
                return false;
            }
            
            // 4. Enviar notificación Firebase
            return sendFirebaseNotification(deviceToken, title, body);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al enviar notificación al usuario " + userUid, e);
            return false;
        }
    }
    
    /**
     * Envía una notificación push directamente a un device token (sin verificar preferencias).
     * Útil para notificaciones de sistema o cuando se quiere forzar el envío.
     */
    public boolean sendFirebaseNotification(String deviceToken, String title, String body) {
        try {
            // 1. Obtener access token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                LOGGER.severe("No se pudo obtener el access token de Firebase");
                return false;
            }
    
            // 2. Crear mensaje FCM con JSONObject
            JSONObject data = new JSONObject();
            data.put("title", title != null ? title : "");
            data.put("body", body != null ? body : "");
    
            JSONObject message = new JSONObject();
            JSONObject inner = new JSONObject();
            inner.put("token", deviceToken);
            inner.put("data", data);
            message.put("message", inner);
    
            // 3. Enviar a FCM
            HttpURLConnection conn = (HttpURLConnection) java.net.URI.create(FCM_SEND_URI).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
    
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = message.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
    
            int responseCode = conn.getResponseCode();
    
            if (responseCode == 200) {
                LOGGER.info("Notificación enviada exitosamente a device token: "
                        + deviceToken.substring(0, Math.min(20, deviceToken.length())) + "...");
                return true;
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.severe("Error al enviar notificación FCM. Código: "
                            + responseCode + ", Respuesta: " + response.toString());
                }
                return false;
            }
    
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al enviar notificación Firebase", e);
            return false;
        }
    }
    
    /**
     * Obtiene un access token de OAuth2 usando JWT Bearer Grant.
     */
    private String getAccessToken() {
        try {
            // 1. Crear JWT firmado
            String jwt = createJWT();
    
            // 2. Intercambiar JWT por access token
            HttpURLConnection conn = (HttpURLConnection) java.net.URI.create(TOKEN_URI).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
    
            String params = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer"
                    + "&assertion=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);
    
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = params.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
    
            int responseCode = conn.getResponseCode();
    
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8))) {
    
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
    
                if (responseCode != 200) {
                    LOGGER.severe("Error al obtener access token. Código: "
                            + responseCode + ", Respuesta: " + response.toString());
                    return null;
                }
    
                JSONObject json = new JSONObject(response.toString());
                return json.optString("access_token", null);
            }
    
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener access token", e);
            return null;
        }
    }
    
    /**
     * Crea un JWT firmado con RS256 para autenticación con Google OAuth2.
     */
    private String createJWT() throws Exception {
        long now = Instant.now().getEpochSecond();
        long exp = now + 3600; // 1 hora de validez
    
        // Header
        JSONObject header = new JSONObject();
        header.put("alg", "RS256");
        header.put("typ", "JWT");
    
        // Payload
        JSONObject payload = new JSONObject();
        payload.put("iss", CLIENT_EMAIL);
        payload.put("scope", SCOPE);
        payload.put("aud", TOKEN_URI);
        payload.put("iat", now);
        payload.put("exp", exp);
    
        String encodedHeader = base64UrlEncode(header.toString().getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payload.toString().getBytes(StandardCharsets.UTF_8));
    
        // Firmar header.payload
        String dataToSign = encodedHeader + "." + encodedPayload;
        byte[] signatureBytes = signRS256Bytes(dataToSign.getBytes(StandardCharsets.UTF_8));
    
        String encodedSignature = base64UrlEncode(signatureBytes);
    
        return dataToSign + "." + encodedSignature;
    }
    
    /**
     * Firma un mensaje usando RS256 con la clave privada y devuelve bytes crudos de la firma.
     */
    private byte[] signRS256Bytes(byte[] data) throws Exception {
        String privateKeyPEM = PRIVATE_KEY
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
    
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
    
        java.security.Signature signature = java.security.Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }
    
    /**
     * Codifica bytes en Base64 URL-safe sin padding.
     */
    private String base64UrlEncode(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }
}


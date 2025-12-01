package uy.edu.tse.hcen.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import uy.edu.tse.hcen.config.GubUyConfig;
import uy.edu.tse.hcen.dao.UserDAO;
import uy.edu.tse.hcen.dto.TokenResponse;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.model.Nacionalidad;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Stateless
public class AuthService {
    
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    
    @EJB
    private UserDAO userDAO;

    @EJB
    private InusIntegrationService inusService;

    @EJB
    private DnicIntegrationService dnicService;
    
    
    // Extrae el valor de un campo JSON de una respuesta
    private String extractJsonField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"?([^\",}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\"", "");
        }
        return null;
    }
    
    private String[] parseUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            return null;
        }
        
        // El formato es: nacionalidad-tipoDocum-codigoDocum
        // Ejemplo: uy-ci-12345678
        String[] parts = uid.split("-", 3);
        if (parts.length >= 3) {
            return new String[]{parts[0], parts[1], parts[2]};
        }
        
        LOGGER.warning("Formato de UID inválido. Esperado: nacionalidad-tipoDocum-codigoDocum. Recibido: " + uid);
        return null;
    }    
    
    // Intercambia el código de autorización por tokens
    // @param code: Código de autorización recibido de ID Uruguay
    // @return TokenResponse: con access_token, id_token, etc.
    public TokenResponse exchangeCodeForTokens(String code) {
        try {
            LOGGER.info("Iniciando intercambio de código por tokens. Código (primeros 20 chars): " + 
                       (code != null && code.length() > 20 ? code.substring(0, 20) + "..." : code));
            LOGGER.info("Token endpoint: " + GubUyConfig.TOKEN_ENDPOINT);
            LOGGER.info("Redirect URI: " + GubUyConfig.REDIRECT_URI);
            
            URL url = new URL(GubUyConfig.TOKEN_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            
            String auth = GubUyConfig.CLIENT_ID + ":" + GubUyConfig.CLIENT_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            StringBuilder body = new StringBuilder();
            body.append("grant_type=authorization_code");
            body.append("&code=").append(URLEncoder.encode(code, StandardCharsets.UTF_8));
            body.append("&redirect_uri=").append(URLEncoder.encode(GubUyConfig.REDIRECT_URI, StandardCharsets.UTF_8));
            
            String requestBody = body.toString();
            LOGGER.info("Request body (sin código completo): grant_type=authorization_code&code=...&redirect_uri=" + GubUyConfig.REDIRECT_URI);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    String jsonResponse = response.toString();
                    TokenResponse tokenResponse = new TokenResponse();
                    tokenResponse.setAccess_token(extractJsonField(jsonResponse, "access_token"));
                    tokenResponse.setToken_type(extractJsonField(jsonResponse, "token_type"));
                    tokenResponse.setRefresh_token(extractJsonField(jsonResponse, "refresh_token"));
                    tokenResponse.setId_token(extractJsonField(jsonResponse, "id_token"));
                    tokenResponse.setScope(extractJsonField(jsonResponse, "scope"));
                    
                    String expiresIn = extractJsonField(jsonResponse, "expires_in");
                    if (expiresIn != null) {
                        tokenResponse.setExpires_in(Integer.parseInt(expiresIn));
                    }
                    
                    return tokenResponse;
                }
            } else {
                // Leer el cuerpo de la respuesta de error para obtener más detalles
                String errorBody = "";
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    errorBody = errorResponse.toString();
                } catch (Exception e) {
                    LOGGER.warning("No se pudo leer el cuerpo de la respuesta de error: " + e.getMessage());
                }
                
                String errorMessage = "Error intercambiando código por tokens. Código: " + responseCode;
                if (!errorBody.isEmpty()) {
                    errorMessage += " - Respuesta: " + errorBody;
                    // Intentar extraer el mensaje de error del JSON si está disponible
                    String errorDesc = extractJsonField(errorBody, "error_description");
                    if (errorDesc != null) {
                        errorMessage += " - Descripción: " + errorDesc;
                    }
                    String errorCode = extractJsonField(errorBody, "error");
                    if (errorCode != null) {
                        errorMessage += " - Error: " + errorCode;
                    }
                }
                LOGGER.severe(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error intercambiando código por tokens: " + e.getMessage());
            throw new RuntimeException("Error intercambiando código por tokens", e);
        }
    }
    
    
    // Obtiene la información del usuario usando el access token y la persiste en la base de datos
    // @param accessToken: Token de acceso obtenido del Token Endpoint
    // @return User: con los datos del ciudadano persistidos
    public User getUserInfo(String accessToken) {
        try {
            URL url = new URL(GubUyConfig.USERINFO_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    String jsonResponse = response.toString();
                    
                    String uid = extractJsonField(jsonResponse, "uid");
                    
                    // Extraer nacionalidad, tipo de documento y código de documento del UID
                    String[] uidParts = parseUid(uid);
                    String nacionalidadStr = null;
                    String tipDocum = null;
                    String codDocum = null;
                    
                    if (uidParts != null && uidParts.length >= 3) {
                        nacionalidadStr = uidParts[0];
                        tipDocum = uidParts[1] != null ? uidParts[1].toUpperCase() : null;
                        codDocum = uidParts[2];
                        LOGGER.info(String.format("Datos extraídos del UID - UID: %s, Nacionalidad: %s, TipoDoc: %s, CodDoc: %s", 
                                uid, nacionalidadStr, tipDocum, codDocum));
                    } else {
                        LOGGER.warning("No se pudo extraer datos del UID: " + uid + ". Usando valores por defecto o del JSON.");
                        // Fallback: intentar obtener del JSON si el formato del UID no es válido
                        String tipDocumJson = extractJsonField(jsonResponse, "tipo_documento");
                        tipDocum = tipDocumJson != null ? tipDocumJson.toUpperCase() : null;
                        codDocum = extractJsonField(jsonResponse, "numero_documento");
                        nacionalidadStr = extractJsonField(jsonResponse, "pais_documento");
                    }
                    
                    Nacionalidad nacionalidad = Nacionalidad.fromCodigo(nacionalidadStr);
                    if (nacionalidad == null) {
                        nacionalidad = Nacionalidad.OT; // Valor por defecto
                        LOGGER.warning("Nacionalidad no reconocida: " + nacionalidadStr + ". Usando OT como valor por defecto.");
                    }
                    
                    User gubUyUser = new User(
                        uid,
                        extractJsonField(jsonResponse, "email"),
                        extractJsonField(jsonResponse, "primer_nombre"),
                        extractJsonField(jsonResponse, "segundo_nombre"),
                        extractJsonField(jsonResponse, "primer_apellido"),
                        extractJsonField(jsonResponse, "segundo_apellido"),
                        tipDocum,
                        codDocum,
                        nacionalidad
                    );

                    // --- VALIDACIÓN PDI (DNIC) ---
                    // Verificar mayoría de edad si es la primera vez o no tenemos fecha nacimiento
                    try {
                        if (codDocum != null && tipDocum != null) {
                            // Consultamos siempre para asegurar la fecha de nacimiento y validar edad
                            DnicIntegrationService.DatosDnic datosDnic = dnicService.consultarDatosPersona(tipDocum, codDocum);
                            
                            if (datosDnic.isEncontrado()) {
                                if (!dnicService.esMayorDeEdad(datosDnic.getFechaNacimiento())) {
                                    LOGGER.warning("Usuario menor de edad intentando ingresar: " + codDocum);
                                    // Lanzamos RuntimeException para cortar el flujo. 
                                    // El frontend deberá manejar este error específico.
                                    throw new RuntimeException("MENOR_DE_EDAD: El usuario debe ser mayor de 18 años para ingresar.");
                                }
                                
                                // Si es mayor, actualizamos la fecha de nacimiento en el objeto preliminar
                                LOGGER.info("Usuario validado en DNIC. Mayor de edad. Fecha Nacimiento: " + datosDnic.getFechaNacimiento());
                                gubUyUser.setFechaNacimiento(java.sql.Date.valueOf(datosDnic.getFechaNacimiento()));
                            } else {
                                LOGGER.warning("Usuario no encontrado en DNIC: " + datosDnic.getError());
                                // Decisión de negocio: ¿Permitir ingresar si DNIC falla?
                                // Por seguridad, asumimos que si no valida, podría ser un riesgo, pero para el MVP
                                // tal vez queramos permitirlo si DNIC está caído.
                                // Por ahora logueamos y seguimos, salvo que queramos ser estrictos.
                            }
                        }
                    } catch (RuntimeException re) {
                        if (re.getMessage().startsWith("MENOR_DE_EDAD")) {
                            throw re; // Re-lanzar para que llegue al controller
                        }
                        LOGGER.warning("Error en validación DNIC (no bloqueante salvo menor): " + re.getMessage());
                    }
                    // -----------------------------

                    // --- INTEGRACIÓN INUS (Sincronización Master) ---
                    try {
                        User inusUser = null;
                        
                        // 1. PRIORIDAD 1: Buscar por UID (Clave primaria natural)
                        if (uid != null) {
                             inusUser = inusService.obtenerUsuarioPorUid(uid);
                        }
                        
                        // 2. PRIORIDAD 2: Buscar por Documento (si no se encontró por UID)
                        if (inusUser == null && codDocum != null) {
                            inusUser = inusService.obtenerUsuarioPorDocumento(tipDocum, codDocum);
                        }
                        
                        // Asegurar UID estandarizado para el usuario de Gub.uy antes de crearlo
                        if (codDocum != null) {
                            String stdUid = uy.edu.tse.hcen.utils.UserUuidUtil.generateUuid(codDocum);
                            if (gubUyUser.getUid() == null || !gubUyUser.getUid().equals(stdUid)) {
                                LOGGER.info("Estandarizando UID de Gub.uy: " + gubUyUser.getUid() + " -> " + stdUid);
                                gubUyUser.setUid(stdUid);
                            }
                        }
                        
                        if (inusUser == null) {
                            // No existe en INUS -> Crear
                            LOGGER.info("Usuario Gub.uy no encontrado en INUS (ni por Doc ni por UID). Creando en INUS...");
                            inusService.crearUsuarioEnInus(gubUyUser);
                        } else {
                            // Existe en INUS -> Usar datos de INUS como Master
                            LOGGER.info("Usuario encontrado en INUS (UID: " + inusUser.getUid() + "). Usando datos de INUS como fuente de verdad.");
                            
                            // Completar campos nulos de INUS con datos de Gub.uy
                            if (inusUser.getEmail() == null && gubUyUser.getEmail() != null) inusUser.setEmail(gubUyUser.getEmail());
                            if (inusUser.getPrimerNombre() == null && gubUyUser.getPrimerNombre() != null) inusUser.setPrimerNombre(gubUyUser.getPrimerNombre());
                            if (inusUser.getSegundoNombre() == null && gubUyUser.getSegundoNombre() != null) inusUser.setSegundoNombre(gubUyUser.getSegundoNombre());
                            if (inusUser.getPrimerApellido() == null && gubUyUser.getPrimerApellido() != null) inusUser.setPrimerApellido(gubUyUser.getPrimerApellido());
                            if (inusUser.getSegundoApellido() == null && gubUyUser.getSegundoApellido() != null) inusUser.setSegundoApellido(gubUyUser.getSegundoApellido());
                            if (inusUser.getTipDocum() == null && gubUyUser.getTipDocum() != null) inusUser.setTipDocum(gubUyUser.getTipDocum());
                            if (inusUser.getCodDocum() == null && gubUyUser.getCodDocum() != null) inusUser.setCodDocum(gubUyUser.getCodDocum());
                            if (inusUser.getNacionalidad() == null && gubUyUser.getNacionalidad() != null) inusUser.setNacionalidad(gubUyUser.getNacionalidad());
                            if (inusUser.getFechaNacimiento() == null && gubUyUser.getFechaNacimiento() != null) inusUser.setFechaNacimiento(gubUyUser.getFechaNacimiento());
                            
                            // Reemplazamos gubUyUser con inusUser para que la actualización local use los datos de INUS
                            gubUyUser = inusUser;
                        }
                    } catch (Exception e) {
                        LOGGER.warning("Error no bloqueante sincronizando con INUS: " + e.getMessage());
                    }
                    // ------------------------------------------------

                    // UID definitivo (puede ser el de GubUy o el de INUS unificado)
                    String finalUid = gubUyUser.getUid();
                    
                    // Búsqueda Local Robusta
                    User existingUser = null;
                    
                    // 1. Buscar por Documento Localmente
                    if (codDocum != null) {
                         existingUser = userDAO.findByDocumento(codDocum);
                    }
                    
                    // 2. Buscar por UID Localmente (si no se encontró por documento)
                    if (existingUser == null) {
                        existingUser = userDAO.findByUid(finalUid);
                    }
                    
                    if (existingUser != null) {
                        // Caso especial: Encontramos usuario local pero tenía un UID diferente
                        if (!existingUser.getUid().equals(finalUid)) {
                            LOGGER.warning("Usuario local encontrado por documento pero con UID diferente. Intentando unificar localmente: " 
                                    + existingUser.getUid() + " -> " + finalUid);
                        }
                    
                        // Crear un nuevo objeto User con los datos actualizados desde Gub.uy
                        User updatedUserData = new User(
                            finalUid, // Usamos el UID unificado
                            gubUyUser.getEmail(),
                            gubUyUser.getPrimerNombre(),
                            gubUyUser.getSegundoNombre(),
                            gubUyUser.getPrimerApellido(),
                            gubUyUser.getSegundoApellido(),
                            tipDocum,
                            codDocum,
                            nacionalidad
                        );
                        
                        // Preservar ID primario (PK) para que sea un update del registro existente
                        updatedUserData.setId(existingUser.getId());
                        
                        // IMPORTANTE: Preservar el ROL existente para no perder permisos de admin
                        if (existingUser.getRol() != null) {
                            updatedUserData.setRol(existingUser.getRol());
                        }

                        // Copiar campos adicionales si existen
                        if (existingUser.getFechaNacimiento() != null) updatedUserData.setFechaNacimiento(existingUser.getFechaNacimiento());
                        if (existingUser.getDepartamento() != null) updatedUserData.setDepartamento(existingUser.getDepartamento());
                        if (existingUser.getLocalidad() != null) updatedUserData.setLocalidad(existingUser.getLocalidad());
                        if (existingUser.getDireccion() != null) updatedUserData.setDireccion(existingUser.getDireccion());
                        if (existingUser.getTelefono() != null) updatedUserData.setTelefono(existingUser.getTelefono());
                        if (existingUser.getCodigoPostal() != null) updatedUserData.setCodigoPostal(existingUser.getCodigoPostal());
                        updatedUserData.setProfileCompleted(existingUser.isProfileCompleted());
                        
                        // Usar merge en lugar de saveOrUpdate para controlar mejor el ID
                        userDAO.merge(updatedUserData);
                        
                        // Recargar el usuario desde la base de datos
                        // Nota: Si cambiamos el UID, buscar por finalUid debería encontrarlo ahora
                        User finalUser = userDAO.findByUid(finalUid);
                        if (finalUser == null) {
                             // Si falló el cambio de UID (ej: restricción BD), retornamos el existente original
                             LOGGER.warning("No se pudo recargar usuario con nuevo UID. Retornando existente.");
                             return existingUser;
                        }
                        LOGGER.info("Usuario actualizado en base de datos: " + finalUser.getUid());
                        return finalUser;
                    } else {
                        // Usar los valores extraídos/corregidos
                        userDAO.saveOrUpdate(gubUyUser);
                        
                        // Recargar el usuario desde la base de datos
                        User createdUser = userDAO.findByUid(finalUid);
                        LOGGER.info("Usuario creado en base de datos: " + createdUser.getUid());
                        return createdUser;
                    }
                }
            } else {
                String errorMessage = "Error obteniendo información del usuario. Código: " + responseCode;
                LOGGER.severe(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            
        } catch (RuntimeException re) {
            if (re.getMessage() != null && re.getMessage().startsWith("MENOR_DE_EDAD")) {
                throw re;
            }
            LOGGER.severe("Error Runtime obteniendo información del usuario: " + re.getMessage());
            throw new RuntimeException("Error obteniendo información del usuario", re);
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo información del usuario: " + e.getMessage());
            throw new RuntimeException("Error obteniendo información del usuario", e);
        }
    }
}

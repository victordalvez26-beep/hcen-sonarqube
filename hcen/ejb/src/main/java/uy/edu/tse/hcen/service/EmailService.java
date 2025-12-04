package uy.edu.tse.hcen.service;

import jakarta.ejb.Stateless;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio para envío de emails usando Gmail SMTP.
 * 
 * Configuración:
 * - SMTP Host: smtp.gmail.com
 * - Puerto: 587 (TLS)
 * - Autenticación: hcentse@gmail.com
 */
@Stateless
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    
    // Configuración de Gmail SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = "hcentse@gmail.com";
    private static final String SMTP_PASSWORD = "jwvocpfxtdqskogn"; // App Password (sin espacios)
    private static final String FROM_EMAIL = "hcentse@gmail.com";
    private static final String FROM_NAME = "Sistema HCEN";

    /**
     * Envía un email de activación al administrador de una clínica recién creada.
     * 
     * @param toEmail Email del destinatario
     * @param clinicName Nombre de la clínica
     * @param adminNickname Usuario de administrador generado
     * @param activationUrl URL completa de activación
     * @param tenantId ID del tenant
     * @return true si el email se envió exitosamente
     */
    public boolean sendActivationEmail(String toEmail, String clinicName, String adminNickname, 
                                      String activationUrl, String tenantId) {
        
        if (toEmail == null || toEmail.isBlank()) {
            LOGGER.warning("Cannot send activation email: recipient email is null or empty");
            return false;
        }

        try {
            LOGGER.info(String.format("Sending activation email to: %s for clinic: %s", toEmail, clinicName));
            
            // Configurar propiedades SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            // Crear sesión con autenticación
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                }
            });

            // Crear mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Active su cuenta de administrador - " + clinicName);
            
            // Contenido HTML del email
            String htmlContent = buildActivationEmailHtml(clinicName, adminNickname, activationUrl, tenantId);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Enviar
            Transport.send(message);
            
            LOGGER.info("Activation email sent successfully to: " + toEmail);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending activation email to: " + toEmail, e);
            return false;
        }
    }

    /**
     * Construye el contenido HTML del email de activación.
     */
    private String buildActivationEmailHtml(String clinicName, String adminNickname, 
                                           String activationUrl, String tenantId) {
        
        String portalUrl = activationUrl.substring(0, activationUrl.indexOf("/activate")) + "/login";
        
        return "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
            
            "  <div style='background: linear-gradient(135deg, #1f2b7b 0%, #3b82f6 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
            "    <h1 style='color: #ffffff; margin: 0; font-size: 28px;'>🏥 HCEN</h1>" +
            "    <p style='color: #e2e8f0; margin: 10px 0 0 0; font-size: 16px;'>Historia Clínica Electrónica Nacional</p>" +
            "  </div>" +
            
            "  <div style='background: #ffffff; padding: 40px; border: 1px solid #e5e7eb; border-top: none;'>" +
            "    <h2 style='color: #1f2937; margin-top: 0;'>¡Bienvenido a HCEN!</h2>" +
            "    <p style='color: #6b7280; font-size: 16px;'>Su clínica <strong>" + clinicName + "</strong> ha sido registrada exitosamente en el sistema.</p>" +
            
            "    <div style='background: #f0fdf4; border-left: 4px solid #10b981; padding: 20px; margin: 25px 0; border-radius: 5px;'>" +
            "      <h3 style='color: #047857; margin-top: 0; font-size: 18px;'>Sus Credenciales</h3>" +
            "      <p style='margin: 10px 0;'><strong>Usuario:</strong> <code style='background: #f3f4f6; padding: 4px 8px; border-radius: 4px; font-size: 14px;'>" + adminNickname + "</code></p>" +
            "      <p style='margin: 10px 0;'><strong>Portal:</strong> <a href='" + portalUrl + "' style='color: #3b82f6;'>" + portalUrl + "</a></p>" +
            "    </div>" +
            
            "    <h3 style='color: #1f2937; font-size: 18px;'>🔐 Pasos para Activar su Cuenta</h3>" +
            "    <ol style='color: #6b7280; padding-left: 20px;'>" +
            "      <li style='margin-bottom: 10px;'>Haga clic en el botón de activación a continuación</li>" +
            "      <li style='margin-bottom: 10px;'>Cree una contraseña segura (mínimo 8 caracteres)</li>" +
            "      <li style='margin-bottom: 10px;'>Inicie sesión con su usuario: <strong>" + adminNickname + "</strong></li>" +
            "      <li style='margin-bottom: 10px;'>Configure su portal y comience a gestionar su clínica</li>" +
            "    </ol>" +
            
            "    <div style='text-align: center; margin: 35px 0;'>" +
            "      <a href='" + activationUrl + "' style='display: inline-block; background: #10b981; color: #ffffff; padding: 15px 40px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px;'>Activar Cuenta</a>" +
            "    </div>" +
            
            "    <div style='background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 25px 0; border-radius: 5px;'>" +
            "      <p style='margin: 0; color: #92400e; font-size: 14px;'>" +
            "        <strong>Importante:</strong> Este enlace es válido por <strong>48 horas</strong>. " +
            "        Si no activa su cuenta dentro de este período, deberá solicitar un nuevo enlace." +
            "      </p>" +
            "    </div>" +
            
            "    <div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb;'>" +
            "      <p style='color: #9ca3af; font-size: 13px; margin: 0;'>Si no solicitó esta activación, puede ignorar este email.</p>" +
            "      <p style='color: #9ca3af; font-size: 13px; margin: 5px 0 0 0;'>Si tiene problemas, contacte a soporte: soporte@hcen.gub.uy</p>" +
            "    </div>" +
            "  </div>" +
            
            "  <div style='background: #f8fafc; padding: 20px; text-align: center; border-radius: 0 0 10px 10px; border: 1px solid #e5e7eb; border-top: none;'>" +
            "    <p style='color: #6b7280; font-size: 12px; margin: 0;'>© 2025 HCEN - Historia Clínica Electrónica Nacional</p>" +
            "    <p style='color: #9ca3af; font-size: 11px; margin: 5px 0 0 0;'>Ministerio de Salud Pública - Gobierno de Uruguay</p>" +
            "  </div>" +
            
            "</body>" +
            "</html>";
    }

    /**
     * Envía un email de prueba para verificar la configuración SMTP.
     * Útil para debugging.
     */
    public boolean sendTestEmail(String toEmail) {
        try {
            LOGGER.info("Sending test email to: " + toEmail);
            
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Email de prueba - HCEN");
            message.setText("Este es un email de prueba del sistema HCEN.\n\nSi recibió este mensaje, la configuración SMTP está funcionando correctamente.");

            Transport.send(message);
            
            LOGGER.info("Test email sent successfully to: " + toEmail);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending test email", e);
            return false;
        }
    }

    /**
     * Envía un email de invitación para que la clínica complete su registro.
     * La clínica ingresará RUT, dirección, usuario y contraseña en el formulario.
     * 
     * @param toEmail Email del administrador de la clínica
     * @param clinicName Nombre de la clínica (ingresado por Admin HCEN)
     * @param registrationUrl URL para completar el registro
     * @param tenantId ID del tenant
     * @return true si el email se envió exitosamente
     */
    public boolean sendInvitationEmail(String toEmail, String clinicName, String registrationUrl, String tenantId) {
        if (toEmail == null || toEmail.isBlank()) {
            LOGGER.warning("Cannot send invitation email: recipient email is null or empty");
            return false;
        }

        try {
            LOGGER.info(String.format("Sending invitation email to: %s for clinic: %s", toEmail, clinicName));
            
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Complete el Registro de " + clinicName + " - HCEN");
            
            String htmlContent = buildInvitationHtml(clinicName, registrationUrl);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            
            LOGGER.info("Invitation email sent successfully to: " + toEmail);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending invitation email to " + toEmail, e);
            return false;
        }
    }

    /**
     * Construye el HTML del email de invitación para completar el registro.
     */
    private String buildInvitationHtml(String clinicName, String registrationUrl) {
        return 
            "<html lang='es'>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
            
            "  <div style='background: linear-gradient(135deg, #1f2b7b 0%, #3b82f6 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
            "    <h1 style='color: #ffffff; margin: 0; font-size: 28px;'>🏥 HCEN</h1>" +
            "    <p style='color: #e2e8f0; margin: 10px 0 0 0; font-size: 16px;'>Historia Clínica Electrónica Nacional</p>" +
            "  </div>" +
            
            "  <div style='background: #ffffff; padding: 40px; border: 1px solid #e5e7eb; border-top: none;'>" +
            "    <h2 style='color: #1f2937; margin-top: 0;'>¡Complete el Registro de su Clínica!</h2>" +
            "    <p style='color: #6b7280; font-size: 16px;'>Ha sido invitado a registrar <strong>" + clinicName + "</strong> en el sistema HCEN.</p>" +
            
            "    <div style='background: #eff6ff; border-left: 4px solid #3b82f6; padding: 20px; margin: 25px 0; border-radius: 5px;'>" +
            "      <h3 style='color: #1e40af; margin-top: 0; font-size: 18px;'>Próximos Pasos</h3>" +
            "      <p style='margin: 10px 0; color: #1e40af;'>Para completar el registro, necesitará ingresar:</p>" +
            "      <ul style='color: #1e40af; padding-left: 20px;'>" +
            "        <li>RUT de la clínica (12 dígitos)</li>" +
            "        <li>Dirección completa y departamento</li>" +
            "        <li>Nombre de usuario para acceder al portal</li>" +
            "        <li>Contraseña segura</li>" +
            "      </ul>" +
            "    </div>" +
            
            "    <div style='text-align: center; margin: 35px 0;'>" +
            "      <a href='" + registrationUrl + "' style='display: inline-block; background: #10b981; color: #ffffff; padding: 15px 40px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px;'>Completar Registro</a>" +
            "    </div>" +
            
            "    <div style='background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 25px 0; border-radius: 5px;'>" +
            "      <p style='margin: 0; color: #92400e; font-size: 14px;'>" +
            "        <strong>Importante:</strong> Este enlace es válido por <strong>48 horas</strong>. " +
            "        Si no completa el registro dentro de este período, deberá contactar al administrador de HCEN." +
            "      </p>" +
            "    </div>" +
            
            "    <div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb;'>" +
            "      <p style='color: #9ca3af; font-size: 13px; margin: 0;'>Si no solicitó este registro, puede ignorar este email.</p>" +
            "      <p style='color: #9ca3af; font-size: 13px; margin: 5px 0 0 0;'>Soporte: soporte@hcen.gub.uy</p>" +
            "    </div>" +
            "  </div>" +
            
            "  <div style='background: #f8fafc; padding: 20px; text-align: center; border-radius: 0 0 10px 10px; border: 1px solid #e5e7eb; border-top: none;'>" +
            "    <p style='color: #6b7280; font-size: 12px; margin: 0;'>© 2025 HCEN - Historia Clínica Electrónica Nacional</p>" +
            "    <p style='color: #9ca3af; font-size: 11px; margin: 5px 0 0 0;'>Ministerio de Salud Pública - Gobierno de Uruguay</p>" +
            "  </div>" +
            
            "</body>" +
            "</html>";
    }
    
    /**
     * Envía email de invitación a un Prestador de Salud para completar su registro.
     */
    public void sendPrestadorInvitationEmail(String recipientEmail, String nombrePrestador, String registroUrl) {
        try {
            LOGGER.info("📧 Enviando invitación a prestador: " + nombrePrestador + " (" + recipientEmail + ")");
            
            // Configurar propiedades SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            // Crear sesión con autenticación
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Invitación para Registrarse en HCEN - " + nombrePrestador);
            
            String htmlContent = buildPrestadorInvitationEmailBody(nombrePrestador, registroUrl);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            Transport.send(message);
            
            LOGGER.info("Email de invitación de prestador enviado exitosamente a: " + recipientEmail);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al enviar email de invitación de prestador: " + e.getMessage(), e);
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Construye el cuerpo HTML del email de invitación para Prestadores de Salud.
     */
    private String buildPrestadorInvitationEmailBody(String nombrePrestador, String registroUrl) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "  <meta charset='UTF-8'>" +
            "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "</head>" +
            "<body style='margin: 0; padding: 0; background-color: #f3f4f6; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif;'>" +
            
            "  <div style='max-width: 600px; margin: 40px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
            
            "    <!-- Header -->" +
            "    <div style='background: linear-gradient(135deg, #3b82f6 0%, #1e40af 100%); padding: 30px; text-align: center;'>" +
            "      <h1 style='color: white; margin: 0; font-size: 28px; font-weight: 700;'>HCEN</h1>" +
            "      <p style='color: #e0e7ff; margin: 5px 0 0 0; font-size: 14px;'>Historia Clínica Electrónica Nacional</p>" +
            "    </div>" +
            
            "    <!-- Body -->" +
            "    <div style='padding: 40px 30px;'>" +
            
            "      <h2 style='color: #1f2937; font-size: 24px; margin: 0 0 20px 0;'>¡Bienvenido a HCEN!</h2>" +
            
            "      <p style='color: #4b5563; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;'>" +
            "        Has sido invitado a registrar <strong>" + nombrePrestador + "</strong> como Prestador de Salud en la plataforma HCEN." +
            "      </p>" +
            
            "      <div style='background: #eff6ff; border-left: 4px solid #3b82f6; padding: 15px; margin: 25px 0;'>" +
            "        <p style='color: #1e40af; margin: 0; font-size: 14px;'>" +
            "          <strong>Próximos pasos:</strong><br/>" +
            "          Para completar tu registro, necesitarás proporcionar:<br/>" +
            "          • RUT de tu organización<br/>" +
            "          • URL de tu servidor de documentos clínicos<br/>" +
            "          • Dirección y datos de contacto" +
            "        </p>" +
            "      </div>" +
            
            "      <div style='text-align: center; margin: 30px 0;'>" +
            "        <a href='" + registroUrl + "' style='display: inline-block; background: linear-gradient(135deg, #3b82f6 0%, #1e40af 100%); color: white; text-decoration: none; padding: 15px 40px; border-radius: 8px; font-weight: 600; font-size: 16px;'>" +
            "          Completar Registro" +
            "        </a>" +
            "      </div>" +
            
            "      <div style='background: #fef3c7; border: 1px solid #fbbf24; border-radius: 8px; padding: 15px; margin: 25px 0;'>" +
            "        <p style='color: #92400e; margin: 0; font-size: 14px;'>" +
            "          <strong>Este link es válido por 7 días.</strong> Después de ese tiempo, deberás solicitar una nueva invitación." +
            "        </p>" +
            "      </div>" +
            
            "      <div style='background: #f9fafb; border-radius: 8px; padding: 20px; margin: 25px 0;'>" +
            "        <p style='color: #6b7280; font-size: 14px; margin: 0 0 10px 0;'><strong>Link de registro:</strong></p>" +
            "        <p style='color: #9ca3af; font-size: 12px; word-break: break-all; margin: 0; font-family: monospace;'>" + registroUrl + "</p>" +
            "      </div>" +
            
            "      <p style='color: #6b7280; font-size: 14px; line-height: 1.6; margin: 25px 0 0 0;'>" +
            "        Si tienes dudas o no solicitaste esta invitación, puedes ignorar este correo." +
            "      </p>" +
            
            "    </div>" +
            
            "    <!-- Footer -->" +
            "    <div style='background: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e5e7eb;'>" +
            "      <p style='color: #6b7280; font-size: 12px; margin: 0;'>© 2025 HCEN - Historia Clínica Electrónica Nacional</p>" +
            "      <p style='color: #9ca3af; font-size: 11px; margin: 5px 0 0 0;'>Ministerio de Salud Pública - Gobierno de Uruguay</p>" +
            "    </div>" +
            
            "  </div>" +
            
            "</body>" +
            "</html>";
    }
}



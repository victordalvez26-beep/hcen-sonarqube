package uy.edu.tse.hcen.service;

import jakarta.ejb.Stateless;
import uy.edu.tse.hcen.utils.DnicServiceUrlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Stateless
public class DnicIntegrationService {

    private static final Logger LOGGER = Logger.getLogger(DnicIntegrationService.class.getName());

    public static class DatosDnic {
        private LocalDate fechaNacimiento;
        private boolean encontrado;
        private String error;

        public DatosDnic(LocalDate fechaNacimiento) {
            this.fechaNacimiento = fechaNacimiento;
            this.encontrado = true;
        }

        public DatosDnic(String error) {
            this.error = error;
            this.encontrado = false;
        }

        public LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public boolean isEncontrado() { return encontrado; }
        public String getError() { return error; }
    }

    /**
     * Consulta el servicio SOAP de DNIC para obtener datos de una persona.
     * @param tipoDocumento Tipo de documento (ej: "CI")
     * @param numeroDocumento Número de documento
     * @return DatosDnic con la fecha de nacimiento si se encuentra
     */
    public DatosDnic consultarDatosPersona(String tipoDocumento, String numeroDocumento) {
        String endpointUrl = DnicServiceUrlUtil.getServiceUrl();
        LOGGER.info("Consultando DNIC SOAP para " + tipoDocumento + " " + numeroDocumento + " en " + endpointUrl);

        String soapRequest = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:dnic=\"http://agesic.gub.uy/pdi/services/dnic/1.0\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <dnic:ObtPersonaPorDocRequest>\n" +
            "         <dnic:tipoDocumento>" + tipoDocumento + "</dnic:tipoDocumento>\n" +
            "         <dnic:numeroDocumento>" + numeroDocumento + "</dnic:numeroDocumento>\n" +
            "      </dnic:ObtPersonaPorDocRequest>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = soapRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                String xmlResponse = response.toString();
                LOGGER.info("Respuesta DNIC SOAP cruda: " + xmlResponse);
                return parseSoapResponse(xmlResponse);
            } else {
                LOGGER.severe("Error HTTP al consultar DNIC: " + responseCode);
                return new DatosDnic("Error de comunicación con DNIC: " + responseCode);
            }

        } catch (Exception e) {
            LOGGER.severe("Excepción al consultar DNIC: " + e.getMessage());
            return new DatosDnic("Error interno al consultar DNIC: " + e.getMessage());
        }
    }

    private DatosDnic parseSoapResponse(String xml) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();

            // Buscar etiqueta persona usando namespace correcto
            // El XML de respuesta usa ns2:persona dentro de ns2:ObtPersonaPorDocResponse
            // Usamos "*" para el prefijo para ser más flexibles, pero el namespace URI debe coincidir o ser "*"
            NodeList personaList = doc.getElementsByTagNameNS("http://agesic.gub.uy/pdi/services/dnic/1.0", "persona");
            
            // Si no lo encuentra por namespace específico, intentamos por nombre local ignorando namespace
            if (personaList.getLength() == 0) {
                 personaList = doc.getElementsByTagName("ns2:persona");
            }
            if (personaList.getLength() == 0) {
                 personaList = doc.getElementsByTagName("persona");
            }

            if (personaList.getLength() > 0) {
                Element personaElement = (Element) personaList.item(0);
                
                // Buscar fechaNacimiento
                NodeList fechaList = personaElement.getElementsByTagNameNS("http://agesic.gub.uy/pdi/services/dnic/1.0", "fechaNacimiento");
                if (fechaList.getLength() == 0) fechaList = personaElement.getElementsByTagName("ns2:fechaNacimiento");
                if (fechaList.getLength() == 0) fechaList = personaElement.getElementsByTagName("fechaNacimiento");
                
                if (fechaList.getLength() > 0) {
                    String fechaStr = fechaList.item(0).getTextContent();
                    // fechaStr puede ser "2015-01-01Z"
                    if (fechaStr != null && fechaStr.length() >= 10) {
                        String fechaLimpia = fechaStr.substring(0, 10); // "2015-01-01"
                        LocalDate fechaNacimiento = LocalDate.parse(fechaLimpia, DateTimeFormatter.ISO_LOCAL_DATE);
                        return new DatosDnic(fechaNacimiento);
                    }
                }
            }

            // Verificar si hay error
            NodeList errorList = doc.getElementsByTagNameNS("http://agesic.gub.uy/pdi/services/dnic/1.0", "error");
            if (errorList.getLength() == 0) errorList = doc.getElementsByTagName("ns2:error");
            if (errorList.getLength() == 0) errorList = doc.getElementsByTagName("error");

            if (errorList.getLength() > 0) {
                Element errorElement = (Element) errorList.item(0);
                NodeList mensajeList = errorElement.getElementsByTagNameNS("*", "mensaje"); // "*" cubre cualquier ns
                if (mensajeList.getLength() == 0) mensajeList = errorElement.getElementsByTagName("mensaje");
                if (mensajeList.getLength() == 0) mensajeList = errorElement.getElementsByTagName("ns2:mensaje");

                String mensaje = (mensajeList.getLength() > 0) ? mensajeList.item(0).getTextContent() : "Error desconocido de DNIC";
                return new DatosDnic(mensaje);
            }

            return new DatosDnic("Respuesta inesperada de DNIC - XML: " + xml.substring(0, Math.min(xml.length(), 200)));

        } catch (Exception e) {
            LOGGER.severe("Error parseando respuesta SOAP: " + e.getMessage());
            return new DatosDnic("Error procesando datos de DNIC");
        }
    }

    /**
     * Verifica si una persona es mayor de edad (>= 18 años).
     */
    public boolean esMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) return false;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= 18;
    }
}


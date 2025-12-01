package uy.gub.agesic.pdi.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import uy.gub.agesic.pdi.model.PersonaData;
import uy.gub.agesic.pdi.repository.PersonaRepository;
import uy.gub.agesic.pdi.services.dnic.schemas.ObtPersonaPorDocRequest;
import uy.gub.agesic.pdi.services.dnic.schemas.ObtPersonaPorDocResponse;
import uy.gub.agesic.pdi.services.dnic.schemas.Persona;
// No importamos Error para evitar conflicto con java.lang.Error

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.logging.Logger;

@Endpoint
public class DnicServicioBasicoEndpoint {

    private static final Logger LOGGER = Logger.getLogger(DnicServicioBasicoEndpoint.class.getName());

    private static final String NAMESPACE_URI = "http://agesic.gub.uy/pdi/services/dnic/1.0";

    @Autowired
    private PersonaRepository personaRepository;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ObtPersonaPorDocRequest")
    @ResponsePayload
    public ObtPersonaPorDocResponse obtPersonaPorDoc(@RequestPayload ObtPersonaPorDocRequest request) {
        ObtPersonaPorDocResponse response = new ObtPersonaPorDocResponse();

        try {
            LOGGER.info("Recibida solicitud ObtPersonaPorDoc para TipoDoc: " + request.getTipoDocumento() + ", NumeroDoc: " + request.getNumeroDocumento());

            Optional<PersonaData> personaOpt = personaRepository.findByDocumento(request.getTipoDocumento(), request.getNumeroDocumento());

            if (personaOpt.isPresent()) {
                PersonaData personaData = personaOpt.get();
                Persona persona = new Persona();
                persona.setTipoDocumento(personaData.getTipoDocumento());
                persona.setNumeroDocumento(personaData.getNumeroDocumento());
                persona.setPrimerNombre(personaData.getPrimerNombre());
                persona.setSegundoNombre(personaData.getSegundoNombre());
                persona.setPrimerApellido(personaData.getPrimerApellido());
                persona.setSegundoApellido(personaData.getSegundoApellido());

                // Convertir LocalDate a XMLGregorianCalendar
                GregorianCalendar cal = new GregorianCalendar();
                cal.set(personaData.getFechaNacimiento().getYear(),
                        personaData.getFechaNacimiento().getMonthValue() - 1, // Month is 0-indexed
                        personaData.getFechaNacimiento().getDayOfMonth());
                XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
                persona.setFechaNacimiento(xmlCal);

                persona.setSexo(personaData.getSexo());
                persona.setNacionalidad(personaData.getNacionalidad());
                persona.setDepartamento(personaData.getDepartamento());
                persona.setLocalidad(personaData.getLocalidad());

                response.setPersona(persona);

                LOGGER.info("Persona encontrada: " + personaData.getPrimerNombre() + " " +
                          personaData.getPrimerApellido() +
                          " - Edad: " + personaData.getEdad() +
                          " - Mayor de edad: " + personaData.isMayorDeEdad() +
                          " - Nacionalidad: " + personaData.getNacionalidad());
            } else {
                // Persona no encontrada
                uy.gub.agesic.pdi.services.dnic.schemas.Error error = new uy.gub.agesic.pdi.services.dnic.schemas.Error();
                error.setCodigo("PERSONA_NO_ENCONTRADA");
                error.setMensaje("No se encontró una persona con el documento " +
                                request.getTipoDocumento() + " " + request.getNumeroDocumento());
                response.setError(error);

                LOGGER.warning("Persona no encontrada: " + request.getTipoDocumento() + " " +
                             request.getNumeroDocumento());
            }

        } catch (Exception e) {
            LOGGER.severe("Error procesando solicitud SOAP: " + e.getMessage());
            e.printStackTrace();

            uy.gub.agesic.pdi.services.dnic.schemas.Error error = new uy.gub.agesic.pdi.services.dnic.schemas.Error();
            error.setCodigo("ERROR_INTERNO");
            error.setMensaje("Error interno del servicio: " + e.getMessage());
            response.setError(error);
        }

        return response;
    }

    /**
     * Helper method to convert LocalDate to XMLGregorianCalendar.
     * @param date The LocalDate to convert.
     * @return The converted XMLGregorianCalendar.
     * @throws DatatypeConfigurationException If a DatatypeFactory cannot be created.
     */
    private XMLGregorianCalendar toXMLGregorianCalendar(LocalDate date) throws DatatypeConfigurationException {
        if (date == null) {
            return null;
        }
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
    }
}



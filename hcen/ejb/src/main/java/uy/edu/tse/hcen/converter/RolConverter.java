package uy.edu.tse.hcen.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uy.edu.tse.hcen.model.Rol;

@Converter
public class RolConverter implements AttributeConverter<Rol, String> {

    @Override
    public String convertToDatabaseColumn(Rol rol) {
        if (rol == null) {
            return null;
        }
        return rol.getCodigo();
    }

    @Override
    public Rol convertToEntityAttribute(String codigo) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RolConverter.class.getName());
        if (codigo == null || codigo.isEmpty()) {
            logger.warning("RolConverter: código de rol es null o vacío");
            return null;
        }
        logger.fine("RolConverter: convirtiendo código de rol: '" + codigo + "'");
        Rol rol = Rol.fromCodigo(codigo);
        if (rol == null) {
            logger.warning("RolConverter: No se pudo convertir el código de rol: '" + codigo + "' - usando valor por defecto");
            return Rol.getDefault();
        }
        logger.fine("RolConverter: código '" + codigo + "' convertido a rol: " + rol.getCodigo());
        return rol;
    }
}

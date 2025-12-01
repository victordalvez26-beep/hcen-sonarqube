package uy.edu.tse.hcen.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uy.edu.tse.hcen.model.Nacionalidad;

@Converter
public class NacionalidadConverter implements AttributeConverter<Nacionalidad, String> {

    @Override
    public String convertToDatabaseColumn(Nacionalidad nacionalidad) {
        if (nacionalidad == null) {
            return null;
        }
        return nacionalidad.getCodigo();
    }

    @Override
    public Nacionalidad convertToEntityAttribute(String codigo) {
        if (codigo == null || codigo.isEmpty()) {
            return null;
        }
        return Nacionalidad.fromCodigo(codigo);
    }
}

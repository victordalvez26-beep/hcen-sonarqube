package uy.edu.tse.hcen.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uy.edu.tse.hcen.model.Departamento;

@Converter
public class DepartamentoConverter implements AttributeConverter<Departamento, String> {

    @Override
    public String convertToDatabaseColumn(Departamento departamento) {
        if (departamento == null) {
            return null;
        }
        return departamento.name();
    }

    @Override
    public Departamento convertToEntityAttribute(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return null;
        }
        try {
            return Departamento.valueOf(nombre);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

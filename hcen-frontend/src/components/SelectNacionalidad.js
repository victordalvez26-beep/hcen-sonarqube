import React, { useState, useEffect } from 'react';
import { configService } from '../services/configService';

const SelectNacionalidad = ({ value, onChange, placeholder = "Seleccionar nacionalidad", className = "" }) => {
  const [nacionalidades, setNacionalidades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadNacionalidades = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await configService.getNacionalidades();
        setNacionalidades(data);
      } catch (err) {
        console.error('Error cargando nacionalidades:', err);
        setError('Error cargando nacionalidades');
        // Fallback con algunas nacionalidades básicas
        setNacionalidades([
          { codigo: 'UY', nombre: 'Uruguay' },
          { codigo: 'AR', nombre: 'Argentina' },
          { codigo: 'BR', nombre: 'Brasil' },
          { codigo: 'CL', nombre: 'Chile' },
          { codigo: 'OT', nombre: 'Otros' }
        ]);
      } finally {
        setLoading(false);
      }
    };

    loadNacionalidades();
  }, []);

  if (loading) {
    return (
      <select className={`form-control ${className}`} disabled>
        <option>Cargando nacionalidades...</option>
      </select>
    );
  }

  if (error) {
    console.warn('Error cargando nacionalidades, usando lista básica');
  }

  return (
    <select 
      className={`form-control ${className}`}
      value={value || ''} 
      onChange={(e) => onChange && onChange(e.target.value)}
    >
      <option value="">{placeholder}</option>
      {nacionalidades.map((nacionalidad) => (
        <option key={nacionalidad.codigo} value={nacionalidad.codigo}>
          {nacionalidad.nombre}
        </option>
      ))}
    </select>
  );
};

export default SelectNacionalidad;
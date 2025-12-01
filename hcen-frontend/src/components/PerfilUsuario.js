import React, { useState } from 'react';
import SelectNacionalidad from './SelectNacionalidad';

const PerfilUsuario = ({ user, onUpdate }) => {
  const [formData, setFormData] = useState({
    nacionalidad: ''
  });

  const handleInputChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Datos del formulario:', formData);
    // Aquí podrías enviar los datos al backend
    if (onUpdate) {
      onUpdate(formData);
    }
  };

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-md-8">
          <div className="card">
            <div className="card-header">
              <h4>Completar Perfil de Usuario</h4>
            </div>
            <div className="card-body">
              <form onSubmit={handleSubmit}>
                <div className="row">
                  <div className="col-md-6 mb-3">
                    <label htmlFor="nacionalidad" className="form-label">
                      Nacionalidad *
                    </label>
                    <SelectNacionalidad
                      value={formData.nacionalidad}
                      onChange={(value) => handleInputChange('nacionalidad', value)}
                      placeholder="Seleccione su nacionalidad"
                      className="form-control"
                    />
                  </div>
                </div>

                <div className="row">
                  <div className="col-12">
                    <button type="submit" className="btn btn-primary">
                      Guardar Perfil
                    </button>
                    <button 
                      type="button" 
                      className="btn btn-secondary ms-2"
                      onClick={() => setFormData({
                        nacionalidad: ''
                      })}
                    >
                      Limpiar
                    </button>
                  </div>
                </div>
              </form>

              {/* Mostrar datos actuales del formulario */}
              <div className="mt-4">
                <h6>Datos del formulario:</h6>
                <pre style={{backgroundColor: '#f8f9fa', padding: '10px', borderRadius: '5px', fontSize: '12px'}}>
                  {JSON.stringify(formData, null, 2)}
                </pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PerfilUsuario;
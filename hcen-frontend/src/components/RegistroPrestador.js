import React, { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import config from '../config';

/**
 * Página de registro para Prestadores de Salud.
 * El prestador accede desde el link del email y completa sus datos.
 */
function RegistroPrestador() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');
  
  const [loading, setLoading] = useState(false);
  const [registroExitoso, setRegistroExitoso] = useState(null); // datos devueltos por el backend (incluye apiKey)
  const [mensaje, setMensaje] = useState({ tipo: '', texto: '' });
  
  const [formData, setFormData] = useState({
    rut: '',
    url: '',
    departamento: '',
    localidad: '',
    direccion: '',
    telefono: ''
  });
  
  // Departamentos de Uruguay
  const departamentos = [
    'ARTIGAS', 'CANELONES', 'CERRO_LARGO', 'COLONIA', 'DURAZNO',
    'FLORES', 'FLORIDA', 'LAVALLEJA', 'MALDONADO', 'MONTEVIDEO',
    'PAYSANDU', 'RIO_NEGRO', 'RIVERA', 'ROCHA', 'SALTO',
    'SAN_JOSE', 'SORIANO', 'TACUAREMBO', 'TREINTA_Y_TRES'
  ];
  
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validaciones
    if (!formData.rut) {
      setMensaje({ tipo: 'error', texto: 'El RUT es obligatorio' });
      return;
    }
    
    if (!formData.url) {
      setMensaje({ tipo: 'error', texto: 'La URL del servidor es obligatoria' });
      return;
    }
    
    setLoading(true);
    
    try {
      const response = await fetch(`${config.BACKEND_URL}/api/prestadores-salud/completar-registro`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          token: token,
          rut: formData.rut,
          url: formData.url,
          departamento: formData.departamento,
          localidad: formData.localidad,
          direccion: formData.direccion,
          telefono: formData.telefono
        })
      });
      
      if (response.ok) {
        const data = await response.json();

        // Guardar datos completos del prestador (incluye apiKey) para mostrarlos en pantalla
        setRegistroExitoso(data);

        setMensaje({ 
          tipo: 'success', 
          texto: data.message 
            || `¡Registro completado! Tu prestador "${data.nombre}" ha sido activado en HCEN.`
        });
        
      } else {
        const errorData = await response.json();
        setMensaje({ 
          tipo: 'error', 
          texto: errorData.error || 'Error al completar el registro' 
        });
      }
    } catch (error) {
      console.error('Error:', error);
      setMensaje({ tipo: 'error', texto: 'Error de conexión. Intente nuevamente.' });
    } finally {
      setLoading(false);
    }
  };
  
  if (!token) {
    return (
      <div className="slider_area" style={{minHeight: '100vh', display: 'flex', alignItems: 'center'}}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div className="slider_text text-center">
                <h3 style={{color: '#ef4444', marginBottom: '20px'}}>Token Inválido</h3>
                <p style={{color: '#64748b', fontSize: '18px'}}>
                  El link de registro no es válido o ha expirado.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
  
  return (
    <>
      {/* Header con gradiente azul */}
      <div className="bradcam_area" style={{
        paddingTop: '120px',
        paddingBottom: '80px',
        background: 'linear-gradient(135deg, #3b82f6 0%, #1e40af 100%)',
        position: 'relative',
        overflow: 'hidden',
        marginTop: '0px'
      }}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div className="bradcam_text text-center">
                <h3 style={{
                  color: '#ffffff',
                  fontSize: '48px',
                  fontWeight: '700',
                  marginBottom: '15px',
                  textShadow: '0 2px 4px rgba(0,0,0,0.3)'
                }}>
                  Registro de Prestador de Salud
                </h3>
                <p style={{
                  color: '#e2e8f0',
                  fontSize: '18px',
                  marginBottom: '0',
                  fontWeight: '400'
                }}>
                  Complete los datos de su organización para finalizar el registro en HCEN
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Contenido principal */}
      <div className="slider_area" style={{paddingTop: '60px', paddingBottom: '80px'}}>
        <div className="container">
          <div className="row justify-content-center">
            <div className="col-xl-8 col-lg-10">
              
              {/* Mensajes */}
              {mensaje.texto && (
                <div style={{
                  padding: '15px 20px',
                  borderRadius: '8px',
                  marginBottom: '30px',
                  background: mensaje.tipo === 'success' ? '#d1fae5' : '#fee2e2',
                  color: mensaje.tipo === 'success' ? '#065f46' : '#991b1b',
                  border: `1px solid ${mensaje.tipo === 'success' ? '#a7f3d0' : '#fecaca'}`
                }}>
                  {mensaje.texto}
                </div>
              )}
              
              {/* Si el registro fue exitoso, mostrar resumen y API Key en lugar del formulario */}
              {registroExitoso ? (
                <div style={{
                  background: '#ffffff',
                  padding: '40px',
                  borderRadius: '12px',
                  boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
                }}>
                  <h3 style={{ 
                    fontSize: '22px', 
                    fontWeight: '700', 
                    marginBottom: '20px', 
                    color: '#111827' 
                  }}>
                    Datos de tu Prestador
                  </h3>

                  <div style={{ marginBottom: '20px' }}>
                    <div style={{ marginBottom: '8px' }}>
                      <strong>Nombre:</strong> {registroExitoso.nombre}
                    </div>
                    <div style={{ marginBottom: '8px' }}>
                      <strong>RUT:</strong> {registroExitoso.rut}
                    </div>
                    <div style={{ marginBottom: '8px' }}>
                      <strong>URL del servidor:</strong> {registroExitoso.url}
                    </div>
                    <div style={{ marginBottom: '8px' }}>
                      <strong>Estado:</strong> {registroExitoso.estado}
                    </div>
                  </div>

                  <div style={{
                    padding: '16px',
                    borderRadius: '8px',
                    background: '#fef3c7',
                    border: '1px solid #facc15',
                    marginBottom: '20px'
                  }}>
                    <div style={{ 
                      fontSize: '15px', 
                      fontWeight: '600', 
                      color: '#92400e',
                      marginBottom: '8px'
                    }}>
                      API Key del Prestador
                    </div>
                    <code style={{
                      display: 'block',
                      padding: '10px 12px',
                      borderRadius: '6px',
                      background: '#111827',
                      color: '#f9fafb',
                      fontSize: '14px',
                      wordBreak: 'break-all'
                    }}>
                      {registroExitoso.apiKey || '—'}
                    </code>
                    <p style={{
                      marginTop: '8px',
                      fontSize: '13px',
                      color: '#92400e'
                    }}>
                      Copia y guarda esta API Key de forma segura. La necesitarás para configurar tu componente de Prestador de Salud.
                    </p>
                  </div>

                  <div style={{ textAlign: 'center' }}>
                    <button
                      type="button"
                      onClick={() => navigate('/')}
                      style={{
                        padding: '12px 40px',
                        fontSize: '15px',
                        fontWeight: '600',
                        color: '#ffffff',
                        background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                        border: 'none',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
                      }}
                    >
                      Volver al inicio
                    </button>
                  </div>
                </div>
              ) : (
                <div style={{
                  background: '#ffffff',
                  padding: '40px',
                  borderRadius: '12px',
                  boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
                }}>
                  <form onSubmit={handleSubmit}>
                    
                    {/* RUT */}
                    <div style={{marginBottom: '25px'}}>
                      <label style={{
                        display: 'block',
                        marginBottom: '8px',
                        fontWeight: '600',
                        color: '#374151'
                      }}>
                        RUT *
                      </label>
                      <input
                        name="rut"
                        type="text"
                        value={formData.rut}
                        onChange={handleInputChange}
                        placeholder="211234560012"
                        maxLength="12"
                        required
                        style={{
                          width: '100%',
                          padding: '12px',
                          border: '1px solid #d1d5db',
                          borderRadius: '6px',
                          fontSize: '15px'
                        }}
                      />
                      <small style={{color: '#6b7280', fontSize: '13px'}}>
                        Registro Único Tributario (12 dígitos)
                      </small>
                    </div>
                    
                    {/* URL del Servidor */}
                    <div style={{marginBottom: '25px'}}>
                      <label style={{
                        display: 'block',
                        marginBottom: '8px',
                        fontWeight: '600',
                        color: '#374151'
                      }}>
                        URL del Servidor de Documentos Clínicos *
                      </label>
                      <input
                        name="url"
                        type="url"
                        value={formData.url}
                        onChange={handleInputChange}
                        placeholder="https://api.miprestador.com"
                        required
                        style={{
                          width: '100%',
                          padding: '12px',
                          border: '1px solid #d1d5db',
                          borderRadius: '6px',
                          fontSize: '15px'
                        }}
                      />
                      <small style={{color: '#6b7280', fontSize: '13px'}}>
                        URL base de su servidor que provee el servicio de documentos clínicos
                      </small>
                    </div>
                    
                    {/* Departamento */}
                    <div style={{marginBottom: '25px'}}>
                      <label style={{
                        display: 'block',
                        marginBottom: '8px',
                        fontWeight: '600',
                        color: '#374151'
                      }}>
                        Departamento
                      </label>
                      <select
                        name="departamento"
                        value={formData.departamento}
                        onChange={handleInputChange}
                        style={{
                          width: '100%',
                          padding: '12px',
                          border: '1px solid #d1d5db',
                          borderRadius: '6px',
                          fontSize: '15px'
                        }}
                      >
                        <option value="">-- Seleccione --</option>
                        {departamentos.map(dept => (
                          <option key={dept} value={dept}>
                            {dept.replace('_', ' ')}
                          </option>
                        ))}
                      </select>
                    </div>
                    
                    {/* Localidad */}
                    <div style={{marginBottom: '25px'}}>
                      <label style={{
                        display: 'block',
                        marginBottom: '8px',
                        fontWeight: '600',
                        color: '#374151'
                      }}>
                        Localidad
                      </label>
                      <input
                        name="localidad"
                        type="text"
                        value={formData.localidad}
                        onChange={handleInputChange}
                        placeholder="Ej: Montevideo"
                        style={{
                          width: '100%',
                          padding: '12px',
                          border: '1px solid #d1d5db',
                          borderRadius: '6px',
                          fontSize: '15px'
                        }}
                      />
                    </div>
                    
                    {/* Dirección */}
                    <div style={{marginBottom: '25px'}}>
                      <label style={{
                        display: 'block',
                        marginBottom: '8px',
                        fontWeight: '600',
                        color: '#374151'
                      }}>
                        Dirección
                      </label>
                      <input
                        name="direccion"
                        type="text"
                        value={formData.direccion}
                        onChange={handleInputChange}
                        placeholder="Ej: Av. Italia 2000"
                        style={{
                          width: '100%',
                          padding: '12px',
                          border: '1px solid #d1d5db',
                          borderRadius: '6px',
                          fontSize: '15px'
                        }}
                      />
                    </div>
                    
                    {/* Teléfono */}
                    <div style={{marginBottom: '30px'}}>
                      <label style={{
                        display: 'block',
                        marginBottom: '8px',
                        fontWeight: '600',
                        color: '#374151'
                      }}>
                        Teléfono
                      </label>
                      <input
                        name="telefono"
                        type="tel"
                        value={formData.telefono}
                        onChange={handleInputChange}
                        placeholder="Ej: 099 123 456"
                        style={{
                          width: '100%',
                          padding: '12px',
                          border: '1px solid #d1d5db',
                          borderRadius: '6px',
                          fontSize: '15px'
                        }}
                      />
                    </div>
                    
                    {/* Botón */}
                    <div style={{textAlign: 'center'}}>
                      <button 
                        type="submit" 
                        disabled={loading}
                        style={{
                          padding: '15px 50px',
                          fontSize: '16px',
                          fontWeight: '600',
                          color: '#ffffff',
                          background: loading ? '#9ca3af' : 'linear-gradient(135deg, #3b82f6 0%, #1e40af 100%)',
                          border: 'none',
                          borderRadius: '8px',
                          cursor: loading ? 'not-allowed' : 'pointer',
                          boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
                        }}
                      >
                        {loading ? 'Registrando...' : 'Completar Registro'}
                      </button>
                    </div>
                  </form>
                </div>
              )}
              
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default RegistroPrestador;

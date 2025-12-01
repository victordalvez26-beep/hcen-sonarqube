import React, { useState, useEffect, useCallback } from 'react';
import config from '../config';

/**
 * Componente para gestionar Prestadores de Salud.
 * Los administradores de HCEN pueden invitar prestadores que completen su registro.
 */
function GestionPrestadores() {
  const [prestadores, setPrestadores] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [mensaje, setMensaje] = useState({ tipo: '', texto: '' });
  
  const [formData, setFormData] = useState({
    nombre: '',
    contacto: ''
  });

  const mostrarMensaje = useCallback((texto, tipo) => {
    setMensaje({ texto, tipo });
    setTimeout(() => setMensaje({ tipo: '', texto: '' }), 5000);
  }, []);

  const loadPrestadores = useCallback(async () => {
    try {
      setLoading(true);
      const response = await fetch(`${config.BACKEND_URL}/api/prestadores-salud`, {
        credentials: 'include'
      });
      
      if (response.ok) {
        const data = await response.json();
        setPrestadores(data);
      } else {
        mostrarMensaje('Error cargando prestadores', 'error');
      }
    } catch (error) {
      console.error('Error:', error);
      mostrarMensaje('Error de conexión', 'error');
    } finally {
      setLoading(false);
    }
  }, [mostrarMensaje]);

  useEffect(() => {
    loadPrestadores();
  }, [loadPrestadores]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const response = await fetch(`${config.BACKEND_URL}/api/prestadores-salud/invitar`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
      
      if (response.ok) {
        mostrarMensaje('Invitación enviada exitosamente. El prestador recibirá un email para completar su registro.', 'success');
        resetForm();
        loadPrestadores();
      } else {
        const errorText = await response.text();
        mostrarMensaje('Error: ' + errorText, 'error');
      }
    } catch (error) {
      console.error('Error:', error);
      mostrarMensaje('Error de conexión', 'error');
    }
  };

  const resetForm = () => {
    setFormData({ nombre: '', contacto: '' });
    setShowForm(false);
  };

  const getEstadoBadgeColor = (estado) => {
    switch (estado) {
      case 'ACTIVO': return '#10b981';
      case 'INACTIVO': return '#6b7280';
      case 'PENDIENTE_REGISTRO': return '#3b82f6';
      case 'PENDIENTE': return '#3b82f6';
      case 'ERROR': return '#ef4444';
      default: return '#6b7280';
    }
  };

  if (loading) {
    return (
      <div className="slider_area" style={{minHeight: '100vh', display: 'flex', alignItems: 'center'}}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div className="slider_text text-center">
                <h3>Cargando prestadores...</h3>
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
                  Gestión de Prestadores de Salud
                </h3>
                <p style={{
                  color: '#e2e8f0',
                  fontSize: '18px',
                  marginBottom: '0',
                  fontWeight: '400'
                }}>
                  Administra prestadores de salud integrados al sistema HCEN
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container" style={{paddingTop: '80px', paddingBottom: '60px'}}>
        {mensaje.texto && (
          <div className={`alert ${
            mensaje.tipo === 'success' ? 'alert-success' : 
            mensaje.tipo === 'info' ? 'alert-info' :
            mensaje.tipo === 'warning' ? 'alert-warning' :
            'alert-danger'
          } alert-dismissible fade show`} role="alert" style={{
            borderRadius: '8px',
            padding: '15px 20px',
            marginBottom: '20px'
          }}>
            {mensaje.texto}
            <button type="button" className="btn-close" onClick={() => setMensaje({ tipo: '', texto: '' })}>
              <i className="fa fa-times"></i>
            </button>
          </div>
        )}

        <div className="row">
          <div className="col-xl-12">
            <div className="d-flex justify-content-between align-items-center mb-4">
              <h4 style={{color: '#1f2937', fontWeight: '600'}}>
                <i className="fa fa-user-md" style={{marginRight: '10px', color: '#3b82f6'}}></i>
                Prestadores Registrados
              </h4>
              <button 
                style={{
                  backgroundColor: '#3b82f6',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  padding: '12px 24px',
                  fontSize: '16px',
                  fontWeight: '600',
                  cursor: 'pointer',
                  transition: 'all 0.3s ease'
                }}
                onClick={() => setShowForm(true)}
                onMouseEnter={(e) => e.target.style.backgroundColor = '#2563eb'}
                onMouseLeave={(e) => e.target.style.backgroundColor = '#3b82f6'}
              >
                <i className="fa fa-plus" style={{marginRight: '8px'}}></i>
                Invitar Prestador
              </button>
            </div>

            {showForm && (
              <div className="card mb-4" style={{
                borderRadius: '15px',
                boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
                border: '1px solid #e5e7eb'
              }}>
                <div className="card-header" style={{
                  backgroundColor: '#f8fafc',
                  borderBottom: '2px solid #e5e7eb',
                  borderTopLeftRadius: '15px',
                  borderTopRightRadius: '15px',
                  padding: '20px 30px'
                }}>
                  <h5 style={{color: '#1f2937', fontWeight: '600', marginBottom: '0'}}>
                    Invitar Nuevo Prestador
                  </h5>
                </div>
                <div className="card-body" style={{padding: '30px'}}>
                  <form onSubmit={handleSubmit}>
                    <h6 style={{color: '#374151', fontWeight: '600', marginBottom: '20px', borderBottom: '2px solid #e5e7eb', paddingBottom: '10px'}}>
                      Información Básica
                    </h6>
                    
                    <div style={{padding: '20px', backgroundColor: '#eff6ff', borderRadius: '12px', marginBottom: '25px', border: '1px solid #bfdbfe'}}>
                      <p style={{margin: '0', color: '#1e40af', fontSize: '14px', lineHeight: '1.6'}}>
                        <i className="fa fa-info-circle" style={{marginRight: '10px', color: '#3b82f6'}}></i>
                        <strong>Nuevo Flujo de Registro:</strong> Solo necesita ingresar el nombre y email de contacto. El prestador recibirá un email con un enlace para completar el registro (RUT, dirección, crear usuario y contraseña).
                      </p>
                    </div>

                    <div className="row">
                      <div className="col-md-12 mb-3">
                        <label htmlFor="nombre" className="form-label" style={{fontWeight: '600', color: '#374151'}}>
                          Nombre del Prestador *
                        </label>
                        <input
                          type="text"
                          className="form-control"
                          id="nombre"
                          name="nombre"
                          value={formData.nombre}
                          onChange={(e) => setFormData({...formData, nombre: e.target.value})}
                          required
                          placeholder="Ej: Laboratorio Clínico Central"
                          style={{borderRadius: '8px', border: '2px solid #e5e7eb', padding: '12px 15px', fontSize: '15px'}}
                        />
                        <small style={{color: '#6b7280', fontSize: '13px'}}>
                          Nombre oficial del prestador de salud
                        </small>
                      </div>
                    </div>
                    
                    <div className="row">
                      <div className="col-md-12 mb-3">
                        <label htmlFor="contacto" className="form-label" style={{fontWeight: '600', color: '#374151'}}>
                          Email de Contacto *
                        </label>
                        <input
                          type="email"
                          className="form-control"
                          id="contacto"
                          name="contacto"
                          value={formData.contacto}
                          onChange={(e) => setFormData({...formData, contacto: e.target.value})}
                          required
                          placeholder="contacto@prestador.com"
                          style={{borderRadius: '8px', border: '2px solid #e5e7eb', padding: '12px 15px', fontSize: '15px'}}
                        />
                        <small style={{color: '#6b7280', fontSize: '13px'}}>
                          El prestador recibirá un email para completar el registro
                        </small>
                      </div>
                    </div>

                    <div className="d-flex gap-2 mt-4">
                      <button type="submit" style={{
                        backgroundColor: '#10b981',
                        color: '#ffffff',
                        border: 'none',
                        borderRadius: '8px',
                        padding: '10px 20px',
                        fontSize: '15px',
                        fontWeight: '600',
                        cursor: 'pointer'
                      }}>
                        Enviar Invitación
                      </button>
                      <button type="button" style={{
                        backgroundColor: '#6b7280',
                        color: '#ffffff',
                        border: 'none',
                        borderRadius: '8px',
                        padding: '10px 20px',
                        fontSize: '15px',
                        fontWeight: '600',
                        cursor: 'pointer'
                      }} onClick={resetForm}>
                        Cancelar
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            )}

            <div className="card" style={{
              borderRadius: '15px',
              boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
              border: '1px solid #e5e7eb'
            }}>
              <div className="card-body" style={{padding: '30px'}}>
                {prestadores.length === 0 ? (
                  <div className="text-center py-5">
                    <i className="fa fa-user-md" style={{fontSize: '64px', color: '#d1d5db', marginBottom: '20px'}}></i>
                    <p style={{color: '#6b7280', fontSize: '18px'}}>No hay prestadores registrados</p>
                  </div>
                ) : (
                  <div className="table-responsive">
                    <table className="table table-hover" style={{width: '100%'}}>
                      <thead style={{backgroundColor: '#f8fafc'}}>
                        <tr>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Nombre</th>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>RUT</th>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Contacto</th>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>URL Servidor</th>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Ubicación</th>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Estado</th>
                        </tr>
                      </thead>
                      <tbody>
                        {prestadores.map((prestador, index) => {
                          const uniqueKey = prestador.id || `prestador-${index}`;
                          return (
                            <tr key={uniqueKey} style={{
                              backgroundColor: '#ffffff',
                              transition: 'all 0.2s ease'
                            }}>
                              <td style={{padding: '15px 20px'}}>
                                <strong style={{color: '#1f2937'}}>{prestador.nombre}</strong>
                              </td>
                              <td style={{padding: '15px 20px', color: '#374151', fontFamily: 'monospace'}}>{prestador.rut || '-'}</td>
                              <td style={{padding: '15px 20px', color: '#374151'}}>{prestador.contacto || '-'}</td>
                              <td style={{padding: '15px 20px', color: '#374151'}}>
                                {prestador.url ? (
                                  <a
                                    href={prestador.url}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    style={{color: '#3b82f6', textDecoration: 'none'}}
                                  >
                                    {prestador.url}
                                  </a>
                                ) : (
                                  <span style={{color: '#9ca3af'}}>Pendiente</span>
                                )}
                              </td>
                              <td style={{padding: '15px 20px', color: '#374151'}}>
                                {prestador.departamento && prestador.localidad
                                  ? `${prestador.localidad}, ${prestador.departamento}`
                                  : prestador.departamento || '-'}
                              </td>
                              <td style={{padding: '15px 20px'}}>
                                <span style={{
                                  padding: '6px 12px',
                                  borderRadius: '5px',
                                  fontWeight: '600',
                                  fontSize: '12px',
                                  backgroundColor: getEstadoBadgeColor(prestador.estado),
                                  color: '#ffffff'
                                }}>
                                  {prestador.estado || 'N/A'}
                                </span>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default GestionPrestadores;

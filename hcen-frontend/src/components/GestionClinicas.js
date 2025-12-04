import React, { useState, useEffect, useCallback } from 'react';
import config from '../config';

const GestionClinicas = () => {
  const [nodos, setNodos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    nombre: '',
    contacto: '' // Email de contacto del administrador
    // Los demás datos (RUT, dirección, etc.) los ingresa la clínica al activarse
    // El estado se establece automáticamente en el backend como PENDIENTE
  });

  const [editingRUT, setEditingRUT] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [showActivationModal, setShowActivationModal] = useState(false);
  const [activationData, setActivationData] = useState(null);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [confirmData, setConfirmData] = useState(null);

  const showMessage = useCallback((msg, type) => {
    setMessage(msg);
    setMessageType(type);
    setTimeout(() => {
      setMessage('');
      setMessageType('');
    }, 5000);
  }, []);

  const showActivationDetails = useCallback((nodo, defaults = {}) => {
    if (!nodo) {
      return;
    }

    if (!nodo.activationUrl) {
      showMessage('¡Clínica activada exitosamente! El tenant está listo.', 'success');
      return;
    }

    setActivationData({
      clinicName: nodo.nombre || defaults.nombre || '',
      adminNickname: nodo.adminNickname || defaults.adminNickname || '',
      activationUrl: nodo.activationUrl,
      adminEmail: nodo.adminEmail || defaults.contacto || '',
      portalUrl: nodo.nodoPerifericoUrlBase && nodo.id
        ? `${nodo.nodoPerifericoUrlBase}/portal/clinica/${nodo.id}`
        : '',
      tenantId: nodo.id
    });
    setShowActivationModal(true);
  }, [showMessage]);

  const loadNodos = useCallback(async () => {
    try {
      setLoading(true);
      const response = await fetch(`${config.BACKEND_URL}/api/nodos`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      // Leer el cuerpo de la respuesta una sola vez
      const contentType = response.headers.get('content-type') || '';
      const isJson = contentType.includes('application/json');
      
      let responseBody = '';
      try {
        responseBody = await response.text();
      } catch (e) {
        throw new Error(`Error leyendo respuesta: ${e.message}`);
      }

      if (response.ok) {
        if (isJson && responseBody) {
          try {
            const data = JSON.parse(responseBody);
            setNodos(data);
          } catch (e) {
            throw new Error(`Error parseando JSON: ${e.message}`);
          }
        } else {
          throw new Error('Respuesta no es JSON válido');
        }
      } else {
        let errorMessage = `Error HTTP ${response.status}`;
        if (isJson && responseBody) {
          try {
            const errorJson = JSON.parse(responseBody);
            errorMessage += ': ' + (errorJson.error || errorJson.message || JSON.stringify(errorJson));
          } catch (e) {
            errorMessage += ': ' + (responseBody.length > 200 ? responseBody.substring(0, 200) + '...' : responseBody);
          }
        } else if (responseBody) {
          errorMessage += ': ' + (responseBody.length > 200 ? responseBody.substring(0, 200) + '...' : responseBody);
        }
        showMessage(errorMessage, 'error');
      }
    } catch (error) {
      console.error('Error cargando nodos:', error);
      showMessage(`Error de conexión: ${error.message}`, 'error');
    } finally {
      setLoading(false);
    }
  }, [showMessage]);

  useEffect(() => {
    loadNodos();
  }, [loadNodos]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      if (editingRUT) {
        
        const response = await fetch(`${config.BACKEND_URL}/api/nodos/${editingRUT}`, {
          method: 'PUT',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(formData)
        });

        // Leer el cuerpo de la respuesta una sola vez
        const contentType = response.headers.get('content-type') || '';
        const isJson = contentType.includes('application/json');
        
        let responseBody = '';
        try {
          responseBody = await response.text();
        } catch (e) {
          showMessage(`Error leyendo respuesta: ${e.message}`, 'error');
          return;
        }

        if (response.ok) {
          showMessage('Nodo periférico actualizado exitosamente', 'success');
          loadNodos();
        } else {
          let errorMessage = `Error HTTP ${response.status}`;
          if (isJson && responseBody) {
            try {
              const errorJson = JSON.parse(responseBody);
              errorMessage = errorJson.error || errorJson.message || errorMessage;
            } catch (e) {
              console.error('Error parseando JSON:', e);
              errorMessage += ': ' + (responseBody.length > 200 ? responseBody.substring(0, 200) + '...' : responseBody);
            }
          } else if (responseBody) {
            errorMessage += ': ' + (responseBody.length > 200 ? responseBody.substring(0, 200) + '...' : responseBody);
          }
          showMessage('Error actualizando nodo: ' + errorMessage, 'error');
          return;
        }
      } else {
        const response = await fetch(`${config.BACKEND_URL}/api/nodos`, {
          method: 'POST',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(formData)
        });
        
        if (response.ok) {
          const createdNodo = await response.json();
          showActivationDetails(createdNodo, formData);
          showMessage(
            `Invitación enviada a ${formData.contacto}. ` +
            `El administrador recibirá un email para completar el registro de la clínica.`,
            'success'
          );
          loadNodos(); // Recargar la lista para mostrar la clínica con estado PENDIENTE_ACTIVACION
        } else {
          const errorText = await response.text();
          showMessage('Error creando nodo: ' + errorText, 'error');
          return;
        }
      }

      resetForm();
    } catch (error) {
      console.error('Error en submit:', error);
      showMessage('Error de conexión', 'error');
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text).then(() => {
      showMessage('Copiado al portapapeles', 'info');
    }).catch(err => {
      console.error('Error copiando:', err);
    });
  };

  const handleEdit = (nodo) => {
    if (!nodo) {
      showMessage('Error: Nodo no válido', 'error');
      return;
    }

    setFormData({
      nombre: nodo.nombre || '',
      RUT: nodo.rut || '',
      departamento: nodo.departamento || '',
      localidad: nodo.localidad || '',
      direccion: nodo.direccion || '',
      contacto: nodo.contacto || '',
      url: nodo.url || '',
      nodoPerifericoUrlBase: nodo.nodoPerifericoUrlBase || '',
      nodoPerifericoUsuario: nodo.nodoPerifericoUsuario || '',
      nodoPerifericoPassword: nodo.nodoPerifericoPassword || ''
      // No enviar estado al editar, el backend lo maneja
    });
    setEditingRUT(nodo.rut || nodo.id);
    setShowForm(true);
  };

  const handleDelete = (nodo) => {
    if (!nodo) {
      showMessage('Error: Nodo no válido', 'error');
      return;
    }

    const identifier = nodo.rut || nodo.id;
    if (!identifier) {
      showMessage('Error: No se puede identificar el nodo a inhabilitar', 'error');
      return;
    }

    setConfirmData({
      title: 'Inhabilitar Clínica',
      message: '¿Está seguro de que desea inhabilitar esta clínica? La clínica quedará inactiva y no se podrá acceder al tenant.',
      onConfirm: async () => {
        try {
          const response = await fetch(`${config.BACKEND_URL}/api/nodos/${identifier}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json'
            }
          });

          if (response.ok || response.status === 200 || response.status === 204) {
            showMessage('Clínica inhabilitada exitosamente', 'success');
            loadNodos();
          } else {
            const errorData = await response.json().catch(() => ({ error: 'Error desconocido' }));
            showMessage('Error inhabilitando clínica: ' + (errorData.error || 'Error desconocido'), 'error');
          }
        } catch (error) {
          console.error('Error inhabilitando clínica:', error);
          showMessage('Error de conexión al inhabilitar', 'error');
        }
      },
      confirmText: 'Inhabilitar',
      confirmColor: '#ef4444'
    });
    setShowConfirmModal(true);
  };

  const handleActivate = (nodo) => {
    if (!nodo) {
      showMessage('Error: Nodo no válido', 'error');
      return;
    }

    const identifier = nodo.rut || nodo.id;
    if (!identifier) {
      showMessage('Error: No se puede identificar el nodo a activar', 'error');
      return;
    }

    setConfirmData({
      title: 'Reactivar Clínica',
      message: '¿Está seguro de que desea reactivar esta clínica? La clínica volverá a estar activa y se podrá acceder al tenant.',
      onConfirm: async () => {
        try {
          const response = await fetch(`${config.BACKEND_URL}/api/nodos/${identifier}/activar`, {
            method: 'PUT',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json'
            }
          });

          if (response.ok || response.status === 200) {
            showMessage('Clínica reactivada exitosamente', 'success');
            loadNodos();
          } else {
            const errorData = await response.json().catch(() => ({ error: 'Error desconocido' }));
            showMessage('Error reactivando clínica: ' + (errorData.error || 'Error desconocido'), 'error');
          }
        } catch (error) {
          console.error('Error reactivando clínica:', error);
          showMessage('Error de conexión al reactivar', 'error');
        }
      },
      confirmText: 'Activar',
      confirmColor: '#10b981'
    });
    setShowConfirmModal(true);
  };

  const resetForm = () => {
    setFormData({
      nombre: '',
      contacto: ''
      // El estado se establece automáticamente en el backend
    });
    setEditingRUT(null);
    setShowForm(false);
  };

  const formatDepartamentoDisplay = (depto) => {
    if (!depto) return '';
    return depto.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
  };

  const getEstadoBadgeColor = (estado) => {
    switch (estado) {
      case 'ACTIVO': return '#10b981';
      case 'INACTIVO': return '#6b7280';
      case 'MANTENIMIENTO': return '#f59e0b';
      case 'ERROR_MENSAJERIA': return '#ef4444';
      case 'PENDIENTE': return '#3b82f6';
      case 'PENDIENTE_ACTIVACION': return '#3b82f6'; // Compatibilidad con datos antiguos
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
                <h3>Cargando clínicas...</h3>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
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
                  Gestión de Clínicas
                </h3>
                <p style={{
                  color: '#e2e8f0',
                  fontSize: '18px',
                  marginBottom: '0',
                  fontWeight: '400'
                }}>
                  Administra las clínicas y centros de salud integrados al sistema
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container" style={{paddingTop: '80px', paddingBottom: '60px'}}>
        {message && (
          <div className={`alert ${
            messageType === 'success' ? 'alert-success' : 
            messageType === 'info' ? 'alert-info' :
            messageType === 'warning' ? 'alert-warning' :
            'alert-danger'
          } alert-dismissible fade show`} role="alert" style={{
            borderRadius: '8px',
            padding: '15px 20px',
            marginBottom: '20px'
          }}>
            {message}
            <button type="button" className="btn-close" onClick={() => setMessage('')}>
              <i className="fa fa-times"></i>
            </button>
          </div>
        )}

        <div className="row">
          <div className="col-xl-12">
            <div className="d-flex justify-content-between align-items-center mb-4">
              <h4 style={{color: '#1f2937', fontWeight: '600'}}>
                <i className="fa fa-hospital" style={{marginRight: '10px', color: '#3b82f6'}}></i>
                Clínicas Registradas
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
                Nuevo Nodo
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
                    {editingRUT ? 'Editar Nodo Periférico' : 'Nuevo Nodo Periférico'}
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
                        <strong>Nuevo Flujo de Registro:</strong> Solo necesita ingresar el nombre y email de contacto. El administrador de la clínica recibirá un email con un enlace para completar el registro (RUT, dirección, crear usuario y contraseña).
                      </p>
                    </div>

                    <div className="row">
                      <div className="col-md-12 mb-3">
                        <label htmlFor="nombre" className="form-label" style={{fontWeight: '600', color: '#374151'}}>
                          Nombre de la Clínica *
                        </label>
                        <input
                          type="text"
                          className="form-control"
                          id="nombre"
                          name="nombre"
                          value={formData.nombre}
                          onChange={handleInputChange}
                          required
                          placeholder="Ej: Clínica Santa María"
                          style={{borderRadius: '8px', border: '2px solid #e5e7eb', padding: '12px 15px', fontSize: '15px'}}
                        />
                        <small style={{color: '#6b7280', fontSize: '13px'}}>
                          Nombre oficial de la clínica o centro de salud
                        </small>
                      </div>
                    </div>
                    
                    <div className="row">
                      <div className="col-md-12 mb-3">
                        <label htmlFor="contacto" className="form-label" style={{fontWeight: '600', color: '#374151'}}>
                          Email del Administrador *
                        </label>
                        <input
                          type="email"
                          className="form-control"
                          id="contacto"
                          name="contacto"
                          value={formData.contacto}
                          onChange={handleInputChange}
                          required
                          placeholder="admin@clinica.com"
                          style={{borderRadius: '8px', border: '2px solid #e5e7eb', padding: '12px 15px', fontSize: '15px'}}
                        />
                        <small style={{color: '#6b7280', fontSize: '13px'}}>
                          El administrador recibirá un email para completar el registro de la clínica
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
                        {editingRUT ? 'Actualizar' : 'Guardar'}
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
                {nodos.length === 0 ? (
                  <div className="text-center py-5">
                    <i className="fa fa-hospital" style={{fontSize: '64px', color: '#d1d5db', marginBottom: '20px'}}></i>
                    <p style={{color: '#6b7280', fontSize: '18px'}}>No hay clínicas registradas</p>
                  </div>
                ) : (
                  <div className="table-responsive">
                    <table className="table table-hover" style={{width: '100%'}}>
                      <thead style={{backgroundColor: '#f8fafc'}}>
                        <tr>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Nombre</th>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>RUT</th>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Ubicación</th>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Contacto</th>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Estado</th>
                          <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Acciones</th>
                        </tr>
                      </thead>
                      <tbody>
                        {nodos.map((nodo, index) => {
                          const uniqueKey = nodo.id || nodo.rut || `nodo-${index}`;
                          return (
                            <tr key={uniqueKey} style={{
                              backgroundColor: '#ffffff',
                              transition: 'all 0.2s ease'
                            }}>
                              <td style={{padding: '15px 20px'}}>
                                <strong style={{color: '#1f2937'}}>{nodo.nombre}</strong>
                              </td>
                              <td style={{padding: '15px 20px', color: '#374151', fontFamily: 'monospace'}}>{nodo.RUT || nodo.rut || '-'}</td>
                              <td style={{padding: '15px 20px', color: '#374151'}}>
                                {formatDepartamentoDisplay(nodo.departamento)}
                                {nodo.localidad && <><br/><small style={{color: '#6b7280'}}>{nodo.localidad}</small></>}
                              </td>
                              <td style={{padding: '15px 20px', color: '#374151'}}>{nodo.contacto || '-'}</td>
                              <td style={{padding: '15px 20px'}}>
                                <span style={{
                                  padding: '6px 12px',
                                  borderRadius: '5px',
                                  fontWeight: '600',
                                  fontSize: '12px',
                                  backgroundColor: getEstadoBadgeColor(nodo.estado),
                                  color: '#ffffff'
                                }}>
                                  {nodo.estado || 'N/A'}
                                </span>
                              </td>
                              <td style={{padding: '15px 20px'}}>
                                <div style={{display: 'flex', gap: '8px'}}>
                                  <button
                                    style={{
                                      backgroundColor: '#3b82f6',
                                      color: '#ffffff',
                                      border: 'none',
                                      borderRadius: '6px',
                                      padding: '6px 12px',
                                      fontSize: '13px',
                                      fontWeight: '500',
                                      cursor: 'pointer'
                                    }}
                                    onClick={() => handleEdit(nodo)}
                                  >
                                    Editar
                                  </button>
                                  {nodo.estado === 'INACTIVO' ? (
                                    <button
                                      style={{
                                        backgroundColor: '#10b981',
                                        color: '#ffffff',
                                        border: 'none',
                                        borderRadius: '6px',
                                        padding: '6px 12px',
                                        fontSize: '13px',
                                        fontWeight: '500',
                                        cursor: 'pointer'
                                      }}
                                      onClick={() => handleActivate(nodo)}
                                    >
                                      Activar
                                    </button>
                                  ) : (
                                    <button
                                      style={{
                                        backgroundColor: '#ef4444',
                                        color: '#ffffff',
                                        border: 'none',
                                        borderRadius: '6px',
                                        padding: '6px 12px',
                                        fontSize: '13px',
                                        fontWeight: '500',
                                        cursor: 'pointer'
                                      }}
                                      onClick={() => handleDelete(nodo)}
                                    >
                                      Inhabilitar
                                    </button>
                                  )}
                                </div>
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

      {/* Modal de Información de Activación */}
      {showActivationModal && activationData && (
        <div className="modal fade show" style={{
          display: 'block',
          backgroundColor: 'rgba(0,0,0,0.5)',
          zIndex: 1050
        }}>
          <div className="modal-dialog modal-lg modal-dialog-centered">
            <div className="modal-content" style={{
              borderRadius: '15px',
              border: 'none',
              boxShadow: '0 10px 30px rgba(0,0,0,0.2)'
            }}>
              <div className="modal-header" style={{
                borderBottom: '1px solid #e5e7eb',
                padding: '25px 30px',
                backgroundColor: '#f0fdf4',
                borderTopLeftRadius: '15px',
                borderTopRightRadius: '15px'
              }}>
                <h5 className="modal-title" style={{
                  color: '#047857',
                  fontWeight: '700',
                  fontSize: '24px'
                }}>
                  <i className="fa fa-check-circle" style={{marginRight: '12px', color: '#10b981'}}></i>{/*
                  */}¡Clínica Creada Exitosamente!
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => setShowActivationModal(false)}
                  style={{
                    backgroundColor: 'transparent',
                    border: 'none',
                    fontSize: '20px',
                    color: '#6b7280',
                    cursor: 'pointer',
                    padding: '0',
                    width: '30px',
                    height: '30px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    transition: 'all 0.2s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.color = '#1f2937';
                    e.target.style.transform = 'scale(1.1)';
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.color = '#6b7280';
                    e.target.style.transform = 'scale(1)';
                  }}
                >
                  <i className="fa fa-times"></i>
                </button>
              </div>
              <div className="modal-body" style={{padding: '30px'}}>
                <div className="alert alert-info" style={{
                  backgroundColor: '#e0f2fe',
                  border: '1px solid #0ea5e9',
                  borderRadius: '8px',
                  padding: '15px',
                  marginBottom: '25px'
                }}>
                  <i className="fa fa-info-circle" style={{marginRight: '8px'}}></i>
                  <strong>Email de Activación Enviado</strong> a: {activationData.adminEmail || 'administrador de la clínica'}
                </div>

                <div style={{marginBottom: '20px'}}>
                  <h6 style={{color: '#374151', marginBottom: '15px', fontWeight: '600', fontSize: '16px'}}>
                    Información para el Administrador de la Clínica
                  </h6>
                  
                  <div style={{
                    backgroundColor: '#f8fafc',
                    padding: '20px',
                    borderRadius: '10px',
                    border: '1px solid #e5e7eb',
                    marginBottom: '15px'
                  }}>
                    {/*<div style={{marginBottom: '15px'}}>
                      <label style={{color: '#6b7280', fontSize: '13px', fontWeight: '600', display: 'block', marginBottom: '5px'}}>
                        👤 Usuario Administrador
                      </label>
                      <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                        <code style={{
                          backgroundColor: '#ffffff',
                          padding: '8px 12px',
                          borderRadius: '6px',
                          border: '1px solid #e5e7eb',
                          fontFamily: 'monospace',
                          fontSize: '14px',
                          flex: 1
                        }}>
                          {activationData.adminNickname}
                        </code>
                        <button
                          onClick={() => copyToClipboard(activationData.adminNickname)}
                          style={{
                            backgroundColor: '#3b82f6',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: '6px',
                            padding: '8px 12px',
                            fontSize: '13px',
                            cursor: 'pointer'
                          }}
                        >
                          <i className="fa fa-copy"></i>
                        </button>
                      </div>
                    </div>*/}

                    <div style={{marginBottom: '15px'}}>
                      <label style={{color: '#6b7280', fontSize: '13px', fontWeight: '600', display: 'block', marginBottom: '5px'}}>
                        URL de Activación (válida por 48 horas)
                      </label>
                      <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                        <input
                          type="text"
                          readOnly
                          value={activationData.activationUrl}
                          style={{
                            backgroundColor: '#ffffff',
                            padding: '8px 12px',
                            borderRadius: '6px',
                            border: '1px solid #e5e7eb',
                            fontSize: '13px',
                            flex: 1
                          }}
                        />
                        <button
                          onClick={() => copyToClipboard(activationData.activationUrl)}
                          style={{
                            backgroundColor: '#3b82f6',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: '6px',
                            padding: '8px 12px',
                            fontSize: '13px',
                            cursor: 'pointer'
                          }}
                        >
                          <i className="fa fa-copy"></i>
                        </button>
                      </div>
                    </div>

                    {/*<div>
                      <label style={{color: '#6b7280', fontSize: '13px', fontWeight: '600', display: 'block', marginBottom: '5px'}}>
                        🏥 URL del Portal (después de activar)
                      </label>
                      <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                        <input
                          type="text"
                          readOnly
                          value={activationData.portalUrl}
                          style={{
                            backgroundColor: '#ffffff',
                            padding: '8px 12px',
                            borderRadius: '6px',
                            border: '1px solid #e5e7eb',
                            fontSize: '13px',
                            flex: 1
                          }}
                        />
                        <button
                          onClick={() => copyToClipboard(activationData.portalUrl)}
                          style={{
                            backgroundColor: '#3b82f6',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: '6px',
                            padding: '8px 12px',
                            fontSize: '13px',
                            cursor: 'pointer'
                          }}
                        >
                          <i className="fa fa-copy"></i>
                        </button>
                      </div>
                    </div>*/}
                  </div>

                  <div className="alert alert-warning" style={{
                    backgroundColor: '#fef3c7',
                    border: '1px solid #f59e0b',
                    borderRadius: '8px',
                    padding: '15px',
                    marginBottom: '0'
                  }}>
                    <i className="fa fa-exclamation-triangle" style={{marginRight: '8px'}}></i>
                    <strong>Importante:</strong> Envíe el enlace de activación al administrador de la clínica {activationData.clinicName}.
                    El enlace es válido por 48 horas.
                  </div>
                </div>

                <div style={{
                  backgroundColor: '#f8fafc',
                  padding: '15px',
                  borderRadius: '8px',
                  border: '1px solid #e5e7eb'
                }}>
                  <h6 style={{color: '#374151', marginBottom: '10px', fontSize: '14px', fontWeight: '600'}}>
                    Instrucciones para el Administrador
                  </h6>
                  <ol style={{marginBottom: '0', paddingLeft: '20px', color: '#6b7280', fontSize: '13px'}}>
                    <li>Abrir el enlace de activación recibido por email</li>
                    <li>Crear una contraseña segura (mínimo 8 caracteres)</li>
                    {/*<li>Iniciar sesión con el usuario: <strong>{activationData.adminNickname}</strong></li>*/}
                    <li>Iniciar sesión con el usuario y contraseña generados</li>
                    <li>Acceder al portal de la clínica</li>
                  </ol>
                </div>
              </div>
              <div className="modal-footer" style={{
                borderTop: '1px solid #e5e7eb',
                padding: '20px 30px',
                backgroundColor: '#f8fafc',
                borderBottomLeftRadius: '15px',
                borderBottomRightRadius: '15px'
              }}>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={() => setShowActivationModal(false)}
                  style={{
                    padding: '10px 24px',
                    borderRadius: '8px',
                    fontWeight: '600',
                    backgroundColor: '#10b981',
                    border: 'none'
                  }}
                >
                  <i className="fa fa-check" style={{marginRight: '5px'}}></i>
                  Entendido
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal de Confirmación */}
      {showConfirmModal && confirmData && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1050,
          animation: 'fadeIn 0.2s ease-in'
        }}>
          <div style={{
            backgroundColor: '#ffffff',
            borderRadius: '12px',
            padding: '0',
            maxWidth: '500px',
            width: '90%',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
            animation: 'slideIn 0.3s ease-out'
          }}>
            <div style={{
              padding: '24px 30px',
              borderBottom: '1px solid #e5e7eb'
            }}>
              <h5 style={{
                margin: 0,
                color: '#111827',
                fontSize: '20px',
                fontWeight: '600',
                display: 'flex',
                alignItems: 'center',
                gap: '10px'
              }}>
                <i className="fa fa-exclamation-triangle" style={{color: '#f59e0b', fontSize: '24px'}}></i>
                {confirmData.title}
              </h5>
            </div>
            <div style={{
              padding: '24px 30px'
            }}>
              <p style={{
                margin: 0,
                color: '#6b7280',
                fontSize: '15px',
                lineHeight: '1.6'
              }}>
                {confirmData.message}
              </p>
            </div>
            <div style={{
              padding: '20px 30px',
              backgroundColor: '#f8fafc',
              borderBottomLeftRadius: '12px',
              borderBottomRightRadius: '12px',
              display: 'flex',
              justifyContent: 'flex-end',
              gap: '12px'
            }}>
              <button
                type="button"
                onClick={() => {
                  setShowConfirmModal(false);
                  setConfirmData(null);
                }}
                style={{
                  padding: '10px 20px',
                  borderRadius: '8px',
                  fontWeight: '600',
                  backgroundColor: '#ffffff',
                  border: '1px solid #d1d5db',
                  color: '#374151',
                  cursor: 'pointer',
                  fontSize: '14px',
                  transition: 'all 0.2s ease'
                }}
                onMouseEnter={(e) => {
                  e.target.style.backgroundColor = '#f3f4f6';
                }}
                onMouseLeave={(e) => {
                  e.target.style.backgroundColor = '#ffffff';
                }}
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={() => {
                  if (confirmData.onConfirm) {
                    confirmData.onConfirm();
                  }
                  setShowConfirmModal(false);
                  setConfirmData(null);
                }}
                style={{
                  padding: '10px 20px',
                  borderRadius: '8px',
                  fontWeight: '600',
                  backgroundColor: confirmData.confirmColor || '#ef4444',
                  border: 'none',
                  color: '#ffffff',
                  cursor: 'pointer',
                  fontSize: '14px',
                  transition: 'all 0.2s ease'
                }}
                onMouseEnter={(e) => {
                  e.target.style.opacity = '0.9';
                }}
                onMouseLeave={(e) => {
                  e.target.style.opacity = '1';
                }}
              >
                {confirmData.confirmText || 'Confirmar'}
              </button>
            </div>
          </div>
        </div>
      )}

      <style>{`
        @keyframes fadeIn {
          from {
            opacity: 0;
          }
          to {
            opacity: 1;
          }
        }
        @keyframes slideIn {
          from {
            transform: translateY(-20px);
            opacity: 0;
          }
          to {
            transform: translateY(0);
            opacity: 1;
          }
        }
      `}</style>
    </>
  );
};

export default GestionClinicas;

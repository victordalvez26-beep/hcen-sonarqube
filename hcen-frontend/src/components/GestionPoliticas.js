import React, { useState, useEffect } from 'react';
import config from '../config';

const GestionPoliticas = () => {
  const [politicas, setPoliticas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [message, setMessage] = useState('');
  const [profesionales, setProfesionales] = useState([]);
  const [clinicas, setClinicas] = useState([]);
  const [loadingProfesionales, setLoadingProfesionales] = useState(false);
  const [loadingClinicas, setLoadingClinicas] = useState(false);
  const [tipoAutorizado, setTipoAutorizado] = useState('profesional'); // 'profesional', 'clinica', 'cualquiera'
  const [formData, setFormData] = useState({
    alcance: 'TODOS_LOS_DOCUMENTOS',
    duracion: 'INDEFINIDA',
    gestion: 'AUTOMATICA',
    codDocumPaciente: '',
    profesionalAutorizado: '',
    clinicaAutorizada: '',
    tipoDocumento: '',
    fechaVencimiento: '',
    referencia: ''
  });

  useEffect(() => {
    loadPoliticas();
    loadProfesionales();
    loadClinicas();
  }, []);

  const loadProfesionales = async () => {
    try {
      setLoadingProfesionales(true);
      const response = await fetch(`${config.BACKEND_URL}/api/users/profesionales`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        setProfesionales(data);
      } else {
        console.error('Error cargando profesionales');
      }
    } catch (error) {
      console.error('Error cargando profesionales:', error);
    } finally {
      setLoadingProfesionales(false);
    }
  };

  const loadClinicas = async () => {
    try {
      setLoadingClinicas(true);
      const response = await fetch(`${config.BACKEND_URL}/api/prestadores-salud`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        setClinicas(data);
      } else {
        console.error('Error cargando clínicas');
      }
    } catch (error) {
      console.error('Error cargando clínicas:', error);
    } finally {
      setLoadingClinicas(false);
    }
  };

  const loadPoliticas = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${config.BACKEND_URL}/api/documentos/politicas`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        setPoliticas(data);
      } else {
        const errorData = await response.json();
        setMessage('Error cargando políticas: ' + (errorData.error || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error cargando políticas:', error);
      setMessage('Error de conexión al cargar políticas');
    } finally {
      setLoading(false);
    }
  };

  const filteredPoliticas = politicas.filter(politica => {
    if (!searchTerm) return true;
    
    // Buscar por profesional/clínica
    const profesionalStr = politica.profesionalAutorizado?.toLowerCase() || '';
    return profesionalStr.includes(searchTerm.toLowerCase());
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Preparar datos según el tipo de autorizado
    const politicaData = { ...formData };
    
    if (tipoAutorizado === 'cualquiera') {
      politicaData.profesionalAutorizado = 'CUALQUIER_PROFESIONAL';
    } else if (tipoAutorizado === 'clinica') {
      politicaData.profesionalAutorizado = 'CLINICA_AUTORIZADA';
      politicaData.clinicaAutorizada = formData.clinicaAutorizada;
    }
    // Si es 'profesional', ya tiene profesionalAutorizado
    
    try {
      const response = await fetch(`${config.BACKEND_URL}/api/documentos/politicas`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(politicaData)
      });

      if (response.ok) {
        const data = await response.json();
        setMessage(data.mensaje || 'Política creada exitosamente');
        setShowModal(false);
        resetForm();
        loadPoliticas();
      } else {
        const errorData = await response.json();
        setMessage('Error creando política: ' + (errorData.error || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error creando política:', error);
      setMessage('Error de conexión al crear política');
    }

    setTimeout(() => setMessage(''), 5000);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('¿Está seguro de que desea eliminar esta política?')) {
      return;
    }

    try {
      const response = await fetch(`${config.BACKEND_URL}/api/documentos/politicas/${id}`, {
        method: 'DELETE',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        setMessage('Política eliminada exitosamente');
        loadPoliticas();
      } else {
        const errorData = await response.json();
        setMessage('Error eliminando política: ' + (errorData.error || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error eliminando política:', error);
      setMessage('Error de conexión al eliminar política');
    }

    setTimeout(() => setMessage(''), 5000);
  };

  const resetForm = () => {
    setTipoAutorizado('profesional');
    setFormData({
      alcance: 'TODOS_LOS_DOCUMENTOS',
      duracion: 'INDEFINIDA',
      gestion: 'AUTOMATICA',
      codDocumPaciente: '',
      profesionalAutorizado: '',
      clinicaAutorizada: '',
      tipoDocumento: '',
      fechaVencimiento: '',
      referencia: ''
    });
  };

  const getAlcanceLabel = (alcance) => {
    const labels = {
      'TODOS_LOS_DOCUMENTOS': 'Todos los Documentos',
      'DOCUMENTOS_POR_TIPO': 'Por Tipo de Documento',
      'UN_DOCUMENTO_ESPECIFICO': 'Un Documento Específico'
    };
    return labels[alcance] || alcance;
  };

  const getDuracionLabel = (duracion) => {
    const labels = {
      'INDEFINIDA': 'Indefinida',
      'TEMPORAL': 'Temporal'
    };
    return labels[duracion] || duracion;
  };

  const getGestionLabel = (gestion) => {
    const labels = {
      'AUTOMATICA': 'Automática',
      'MANUAL': 'Manual'
    };
    return labels[gestion] || gestion;
  };

  const getAlcanceBadgeColor = (alcance) => {
    const colors = {
      'TODOS_LOS_DOCUMENTOS': '#8b5cf6',
      'DOCUMENTOS_POR_TIPO': '#3b82f6',
      'UN_DOCUMENTO_ESPECIFICO': '#10b981'
    };
    return colors[alcance] || '#6b7280';
  };

  if (loading) {
    return (
      <div className="bradcam_area" style={{
        paddingTop: '120px',
        paddingBottom: '80px',
        background: 'linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%)',
        position: 'relative',
        overflow: 'hidden',
        marginTop: '0px',
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div className="text-center">
                <div className="spinner-border text-light" role="status" style={{width: '3rem', height: '3rem'}}>
                  <span className="sr-only">Cargando...</span>
                </div>
                <p className="mt-3 text-white">Cargando políticas...</p>
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
        background: 'linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%)',
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
                  Gestión de Políticas de Acceso
                </h3>
                <p style={{
                  color: 'var(--text-light)',
                  fontSize: '18px',
                  marginBottom: '0',
                  fontWeight: '400'
                }}>
                  Administra las políticas de acceso a documentos clínicos
                </p>
              </div>
            </div>
          </div>
        </div>
        <div style={{
          position: 'absolute',
          top: '0',
          left: '0',
          right: '0',
          bottom: '0',
          background: 'url("/assets/img/banner/banner.png") center/cover',
          opacity: '0.1',
          zIndex: '1'
        }}></div>
      </div>

      <div className="container" style={{paddingTop: '80px', paddingBottom: '60px'}}>
        {message && (
          <div className={`alert alert-${message.includes('Error') ? 'danger' : 'success'} alert-dismissible fade show`} role="alert" style={{
            marginBottom: '30px',
            borderRadius: '10px',
            border: 'none',
            backgroundColor: message.includes('Error') ? '#fee2e2' : '#d1fae5',
            color: message.includes('Error') ? '#991b1b' : '#065f46'
          }}>
            <i className={`fa ${message.includes('Error') ? 'fa-exclamation-circle' : 'fa-check-circle'}`} style={{marginRight: '8px'}}></i>
            {message}
            <button type="button" className="btn-close" onClick={() => setMessage('')}>
              <i className="fa fa-times"></i>
            </button>
          </div>
        )}

        <div className="row">
          <div className="col-xl-12">
            {/* Botón para crear nueva política */}
            <div className="mb-4" style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
              <h5 style={{
                color: '#1f2937',
                fontSize: '20px',
                fontWeight: '600',
                margin: '0'
              }}>
                <i className="fa fa-shield-alt" style={{marginRight: '10px', color: '#3b82f6'}}></i>
                Políticas de Acceso ({filteredPoliticas.length})
              </h5>
              <button
                onClick={() => setShowModal(true)}
                style={{
                  backgroundColor: '#3b82f6',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  padding: '12px 24px',
                  fontSize: '16px',
                  fontWeight: '600',
                  transition: 'all 0.3s ease',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px'
                }}
                onMouseEnter={(e) => {
                  e.target.style.backgroundColor = '#2563eb';
                  e.target.style.transform = 'translateY(-2px)';
                }}
                onMouseLeave={(e) => {
                  e.target.style.backgroundColor = '#3b82f6';
                  e.target.style.transform = 'translateY(0)';
                }}
              >
                <i className="fa fa-plus"></i>
                Nueva Política
              </button>
            </div>

            {/* Barra de búsqueda */}
            <div className="card mb-4" style={{
              borderRadius: '15px',
              boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
              border: '1px solid var(--border-color)'
            }}>
              <div className="card-body" style={{padding: '30px'}}>
                <h5 className="card-title" style={{
                  color: '#1f2937',
                  marginBottom: '20px',
                  fontSize: '20px',
                  fontWeight: '600'
                }}>
                  <i className="fa fa-search" style={{marginRight: '10px', color: '#3b82f6'}}></i>
                  Buscar Políticas
                </h5>
                <div className="row">
                  <div className="col-md-12">
                    <label className="form-label">Buscar por Profesional o Clínica</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Ingrese el ID del profesional o nombre de clínica..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      style={{
                        borderRadius: '8px',
                        border: '2px solid #e5e7eb',
                        padding: '10px 15px',
                        fontSize: '16px'
                      }}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* Tabla de políticas */}
            <div className="card" style={{
              borderRadius: '15px',
              boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
              border: '1px solid var(--border-color)'
            }}>
              <div className="card-body" style={{padding: '30px'}}>
                <div className="table-responsive">
                  <table className="table table-hover" style={{
                    width: '100%',
                    borderCollapse: 'separate',
                    borderSpacing: '0 10px'
                  }}>
                    <thead>
                      <tr style={{backgroundColor: '#f8fafc'}}>
                        <th style={{padding: '15px 20px', borderTopLeftRadius: '8px', borderBottomLeftRadius: '8px', color: '#374151', fontWeight: '600'}}>ID</th>
                        <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Autorizado</th>
                        <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Alcance</th>
                        <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Duración</th>
                        <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Gestión</th>
                        <th style={{padding: '15px 20px', borderTopRightRadius: '8px', borderBottomRightRadius: '8px', color: '#374151', fontWeight: '600'}}>Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredPoliticas.map(politica => (
                        <tr key={politica.id} style={{
                          backgroundColor: '#ffffff',
                          boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
                          transition: 'all 0.2s ease',
                          verticalAlign: 'middle'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)'}
                        onMouseLeave={(e) => e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.05)'}
                        >
                                <td style={{padding: '15px 20px', borderTopLeftRadius: '8px', borderBottomLeftRadius: '8px', color: '#6b7280', fontFamily: 'monospace'}}>
                                  #{politica.id}
                                </td>
                                <td style={{padding: '15px 20px', color: '#374151'}}>
                                  {politica.profesionalAutorizado === 'CUALQUIER_PROFESIONAL' ? (
                                    <span className="badge" style={{
                                      backgroundColor: '#10b981',
                                      color: '#ffffff',
                                      padding: '8px 12px',
                                      borderRadius: '6px',
                                      fontWeight: '600',
                                      fontSize: '13px'
                                    }}>
                                      <i className="fa fa-users" style={{marginRight: '5px'}}></i>
                                      Cualquier Profesional
                                    </span>
                                  ) : politica.profesionalAutorizado === 'CLINICA_AUTORIZADA' ? (
                                    <span className="badge" style={{
                                      backgroundColor: '#3b82f6',
                                      color: '#ffffff',
                                      padding: '8px 12px',
                                      borderRadius: '6px',
                                      fontWeight: '600',
                                      fontSize: '13px'
                                    }}>
                                      <i className="fa fa-hospital" style={{marginRight: '5px'}}></i>
                                      Clínica Autorizada
                                      {politica.clinicaAutorizada && ` (ID: ${politica.clinicaAutorizada})`}
                                    </span>
                                  ) : (
                                    <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                                      <span className="badge" style={{
                                        backgroundColor: '#8b5cf6',
                                        color: '#ffffff',
                                        padding: '6px 10px',
                                        borderRadius: '6px',
                                        fontWeight: '600',
                                        fontSize: '12px'
                                      }}>
                                        <i className="fa fa-user-md" style={{marginRight: '5px'}}></i>
                                        Profesional
                                      </span>
                                      <span style={{fontFamily: 'monospace', fontSize: '14px'}}>
                                        {politica.profesionalAutorizado || 'N/A'}
                                      </span>
                                    </div>
                                  )}
                                </td>
                          <td style={{padding: '15px 20px'}}>
                            <span className="badge" style={{
                              padding: '8px 12px',
                              borderRadius: '5px',
                              fontWeight: '600',
                              fontSize: '12px',
                              backgroundColor: getAlcanceBadgeColor(politica.alcance),
                              color: '#ffffff'
                            }}>
                              {getAlcanceLabel(politica.alcance)}
                            </span>
                          </td>
                          <td style={{padding: '15px 20px', color: '#374151'}}>
                            {getDuracionLabel(politica.duracion)}
                          </td>
                          <td style={{padding: '15px 20px', color: '#374151'}}>
                            {getGestionLabel(politica.gestion)}
                          </td>
                          <td style={{padding: '15px 20px', borderTopRightRadius: '8px', borderBottomRightRadius: '8px'}}>
                            <button
                              onClick={() => handleDelete(politica.id)}
                              style={{
                                backgroundColor: '#dc2626',
                                color: '#ffffff',
                                border: 'none',
                                borderRadius: '6px',
                                padding: '8px 16px',
                                fontSize: '14px',
                                fontWeight: '500',
                                transition: 'all 0.3s ease',
                                cursor: 'pointer'
                              }}
                              onMouseEnter={(e) => {
                                e.target.style.backgroundColor = '#b91c1c';
                                e.target.style.transform = 'translateY(-1px)';
                              }}
                              onMouseLeave={(e) => {
                                e.target.style.backgroundColor = '#dc2626';
                                e.target.style.transform = 'translateY(0)';
                              }}
                            >
                              <i className="fa fa-trash" style={{marginRight: '5px'}}></i>
                              Eliminar
                            </button>
                          </td>
                        </tr>
                      ))}
                            {filteredPoliticas.length === 0 && (
                              <tr>
                                <td colSpan="6" className="text-center" style={{padding: '40px', color: '#6b7280'}}>
                                  <i className="fa fa-shield-alt" style={{fontSize: '48px', marginBottom: '15px', opacity: '0.3'}}></i>
                                  <div style={{fontSize: '18px', fontWeight: '500'}}>No se encontraron políticas</div>
                                  <div style={{fontSize: '14px', marginTop: '5px'}}>
                                    {searchTerm ? 'Intenta con otros términos de búsqueda' : 'No hay políticas configuradas'}
                                  </div>
                                </td>
                              </tr>
                            )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Modal para crear política */}
      {showModal && (
        <div className="modal fade show" style={{
          display: 'block',
          backgroundColor: 'rgba(0,0,0,0.5)',
          zIndex: 1050
        }}>
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content" style={{
              borderRadius: '15px',
              border: 'none',
              boxShadow: '0 10px 30px rgba(0,0,0,0.2)'
            }}>
              <div className="modal-header" style={{
                borderBottom: '1px solid #e5e7eb',
                padding: '20px 30px',
                backgroundColor: 'var(--background-color)',
                borderTopLeftRadius: '15px',
                borderTopRightRadius: '15px'
              }}>
                <h5 className="modal-title" style={{
                  color: 'var(--heading-color)',
                  fontWeight: '600',
                  fontSize: '20px'
                }}>
                  <i className="fa fa-shield-alt" style={{marginRight: '10px', color: 'var(--primary-color)'}}></i>
                  Nueva Política de Acceso
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => {
                    setShowModal(false);
                    resetForm();
                  }}
                  style={{fontSize: '20px'}}
                >
                  <i className="fa fa-times"></i>
                </button>
              </div>
              <form onSubmit={handleSubmit}>
                <div className="modal-body" style={{padding: '30px', maxHeight: '70vh', overflowY: 'auto'}}>
                  <div className="row">
                    <div className="col-md-12 mb-4">
                      <label className="form-label" style={{
                        color: 'var(--heading-color)',
                        fontWeight: '600',
                        marginBottom: '10px'
                      }}>
                        Tipo de Autorización <span style={{color: '#dc2626'}}>*</span>
                      </label>
                      <select
                        className="form-control"
                        value={tipoAutorizado}
                        onChange={(e) => setTipoAutorizado(e.target.value)}
                        required
                        style={{
                          borderRadius: '8px',
                          border: '2px solid #e5e7eb',
                          padding: '12px 15px',
                          fontSize: '16px'
                        }}
                      >
                        <option value="profesional">Profesional Específico</option>
                        <option value="clinica">Clínica Autorizada</option>
                        <option value="cualquiera">Cualquier Profesional</option>
                      </select>
                    </div>

                    {tipoAutorizado === 'profesional' && (
                      <div className="col-md-6 mb-4">
                        <label htmlFor="profesionalAutorizado" className="form-label" style={{
                          color: 'var(--heading-color)',
                          fontWeight: '600',
                          marginBottom: '10px'
                        }}>
                          Profesional <span style={{color: '#dc2626'}}>*</span>
                        </label>
                        {loadingProfesionales ? (
                          <div className="form-control" style={{
                            borderRadius: '8px',
                            border: '2px solid #e5e7eb',
                            padding: '12px 15px',
                            fontSize: '16px',
                            color: '#6b7280'
                          }}>
                            Cargando profesionales...
                          </div>
                        ) : (
                          <select
                            id="profesionalAutorizado"
                            className="form-control"
                            value={formData.profesionalAutorizado}
                            onChange={(e) => setFormData({...formData, profesionalAutorizado: e.target.value})}
                            required
                            style={{
                              borderRadius: '8px',
                              border: '2px solid #e5e7eb',
                              padding: '12px 15px',
                              fontSize: '16px'
                            }}
                          >
                            <option value="">Seleccione un profesional</option>
                            {profesionales.map(prof => (
                              <option key={prof.uid} value={prof.uid}>
                                {prof.nombre} ({prof.email})
                              </option>
                            ))}
                          </select>
                        )}
                      </div>
                    )}

                    {tipoAutorizado === 'clinica' && (
                      <div className="col-md-6 mb-4">
                        <label htmlFor="clinicaAutorizada" className="form-label" style={{
                          color: 'var(--heading-color)',
                          fontWeight: '600',
                          marginBottom: '10px'
                        }}>
                          Clínica <span style={{color: '#dc2626'}}>*</span>
                        </label>
                        {loadingClinicas ? (
                          <div className="form-control" style={{
                            borderRadius: '8px',
                            border: '2px solid #e5e7eb',
                            padding: '12px 15px',
                            fontSize: '16px',
                            color: '#6b7280'
                          }}>
                            Cargando clínicas...
                          </div>
                        ) : (
                          <select
                            id="clinicaAutorizada"
                            className="form-control"
                            value={formData.clinicaAutorizada}
                            onChange={(e) => setFormData({...formData, clinicaAutorizada: e.target.value})}
                            required
                            style={{
                              borderRadius: '8px',
                              border: '2px solid #e5e7eb',
                              padding: '12px 15px',
                              fontSize: '16px'
                            }}
                          >
                            <option value="">Seleccione una clínica</option>
                            {clinicas.map(clinica => (
                              <option key={clinica.id} value={clinica.id}>
                                {clinica.nombre} {clinica.rut ? `(${clinica.rut})` : ''}
                              </option>
                            ))}
                          </select>
                        )}
                      </div>
                    )}

                    {tipoAutorizado === 'cualquiera' && (
                      <div className="col-md-6 mb-4">
                        <div className="form-control" style={{
                          borderRadius: '8px',
                          border: '2px solid #e5e7eb',
                          padding: '12px 15px',
                          fontSize: '16px',
                          backgroundColor: '#f8fafc',
                          color: '#6b7280'
                        }}>
                          Cualquier profesional autorizado
                        </div>
                      </div>
                    )}

                    <div className="col-md-6 mb-4">
                      <label htmlFor="codDocumPaciente" className="form-label" style={{
                        color: 'var(--heading-color)',
                        fontWeight: '600',
                        marginBottom: '10px'
                      }}>
                        Documento del Paciente
                      </label>
                      <input
                        type="text"
                        id="codDocumPaciente"
                        className="form-control"
                        value={formData.codDocumPaciente}
                        onChange={(e) => setFormData({...formData, codDocumPaciente: e.target.value})}
                        style={{
                          borderRadius: '8px',
                          border: '2px solid #e5e7eb',
                          padding: '12px 15px',
                          fontSize: '16px'
                        }}
                        placeholder="Documento del paciente"
                      />
                    </div>

                    <div className="col-md-6 mb-4">
                      <label htmlFor="alcance" className="form-label" style={{
                        color: 'var(--heading-color)',
                        fontWeight: '600',
                        marginBottom: '10px'
                      }}>
                        Alcance <span style={{color: '#dc2626'}}>*</span>
                      </label>
                      <select
                        id="alcance"
                        className="form-control"
                        value={formData.alcance}
                        onChange={(e) => setFormData({...formData, alcance: e.target.value})}
                        required
                        style={{
                          borderRadius: '8px',
                          border: '2px solid #e5e7eb',
                          padding: '12px 15px',
                          fontSize: '16px'
                        }}
                      >
                        <option value="TODOS_LOS_DOCUMENTOS">Todos los Documentos</option>
                        <option value="DOCUMENTOS_POR_TIPO">Por Tipo de Documento</option>
                        <option value="UN_DOCUMENTO_ESPECIFICO">Un Documento Específico</option>
                      </select>
                    </div>

                    <div className="col-md-6 mb-4">
                      <label htmlFor="duracion" className="form-label" style={{
                        color: 'var(--heading-color)',
                        fontWeight: '600',
                        marginBottom: '10px'
                      }}>
                        Duración <span style={{color: '#dc2626'}}>*</span>
                      </label>
                      <select
                        id="duracion"
                        className="form-control"
                        value={formData.duracion}
                        onChange={(e) => setFormData({...formData, duracion: e.target.value})}
                        required
                        style={{
                          borderRadius: '8px',
                          border: '2px solid #e5e7eb',
                          padding: '12px 15px',
                          fontSize: '16px'
                        }}
                      >
                        <option value="INDEFINIDA">Indefinida</option>
                        <option value="TEMPORAL">Temporal</option>
                      </select>
                    </div>

                    <div className="col-md-6 mb-4">
                      <label htmlFor="gestion" className="form-label" style={{
                        color: 'var(--heading-color)',
                        fontWeight: '600',
                        marginBottom: '10px'
                      }}>
                        Gestión <span style={{color: '#dc2626'}}>*</span>
                      </label>
                      <select
                        id="gestion"
                        className="form-control"
                        value={formData.gestion}
                        onChange={(e) => setFormData({...formData, gestion: e.target.value})}
                        required
                        style={{
                          borderRadius: '8px',
                          border: '2px solid #e5e7eb',
                          padding: '12px 15px',
                          fontSize: '16px'
                        }}
                      >
                        <option value="AUTOMATICA">Automática</option>
                        <option value="MANUAL">Manual</option>
                      </select>
                    </div>

                    {formData.duracion === 'TEMPORAL' && (
                      <div className="col-md-6 mb-4">
                        <label htmlFor="fechaVencimiento" className="form-label" style={{
                          color: 'var(--heading-color)',
                          fontWeight: '600',
                          marginBottom: '10px'
                        }}>
                          Fecha de Vencimiento
                        </label>
                        <input
                          type="date"
                          id="fechaVencimiento"
                          className="form-control"
                          value={formData.fechaVencimiento}
                          onChange={(e) => setFormData({...formData, fechaVencimiento: e.target.value})}
                          style={{
                            borderRadius: '8px',
                            border: '2px solid #e5e7eb',
                            padding: '12px 15px',
                            fontSize: '16px'
                          }}
                        />
                      </div>
                    )}

                    {(formData.alcance === 'DOCUMENTOS_POR_TIPO' || formData.alcance === 'UN_DOCUMENTO_ESPECIFICO') && (
                      <div className="col-md-6 mb-4">
                        <label htmlFor="tipoDocumento" className="form-label" style={{
                          color: 'var(--heading-color)',
                          fontWeight: '600',
                          marginBottom: '10px'
                        }}>
                          Tipo de Documento
                        </label>
                        <input
                          type="text"
                          id="tipoDocumento"
                          className="form-control"
                          value={formData.tipoDocumento}
                          onChange={(e) => setFormData({...formData, tipoDocumento: e.target.value})}
                          style={{
                            borderRadius: '8px',
                            border: '2px solid #e5e7eb',
                            padding: '12px 15px',
                            fontSize: '16px'
                          }}
                          placeholder="Ej: INFORME_MEDICO"
                        />
                      </div>
                    )}

                    <div className="col-md-12 mb-4">
                      <label htmlFor="referencia" className="form-label" style={{
                        color: 'var(--heading-color)',
                        fontWeight: '600',
                        marginBottom: '10px'
                      }}>
                        Referencia
                      </label>
                      <textarea
                        id="referencia"
                        className="form-control"
                        value={formData.referencia}
                        onChange={(e) => setFormData({...formData, referencia: e.target.value})}
                        rows="3"
                        style={{
                          borderRadius: '8px',
                          border: '2px solid #e5e7eb',
                          padding: '12px 15px',
                          fontSize: '16px'
                        }}
                        placeholder="Descripción o motivo de la política"
                      />
                    </div>
                  </div>
                </div>
                <div className="modal-footer" style={{
                  borderTop: '1px solid #e5e7eb',
                  padding: '20px 30px',
                  backgroundColor: 'var(--background-color)',
                  borderBottomLeftRadius: '15px',
                  borderBottomRightRadius: '15px'
                }}>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => {
                      setShowModal(false);
                      resetForm();
                    }}
                    style={{
                      padding: '10px 20px',
                      borderRadius: '8px',
                      fontWeight: '500'
                    }}
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    className="btn btn-primary"
                    style={{
                      padding: '10px 20px',
                      borderRadius: '8px',
                      fontWeight: '500',
                      backgroundColor: 'var(--primary-color)',
                      border: 'none'
                    }}
                  >
                    <i className="fa fa-save" style={{marginRight: '5px'}}></i>
                    Crear Política
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default GestionPoliticas;


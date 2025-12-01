import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import config from '../config';

const MisClinicas = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [clinicas, setClinicas] = useState([]);
  const [loadingClinicas, setLoadingClinicas] = useState(false);
  const [error, setError] = useState(null);
  
  // Estado para el modal de políticas
  const [showModal, setShowModal] = useState(false);
  const [selectedClinica, setSelectedClinica] = useState(null);
  const [politicas, setPoliticas] = useState([]);
  const [loadingPoliticas, setLoadingPoliticas] = useState(false);

  useEffect(() => {
    checkSession();
  }, []);

  useEffect(() => {
    if (user && user.uid) {
      loadClinicas();
    }
  }, [user]);
  
  const checkSession = async () => {
    try {
      const response = await fetch(`${config.BACKEND_URL}/api/auth/session`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      const data = await response.json();
      
      if (data.authenticated) {
        setUser(data);
      } else {
        setUser(null);
        window.location.href = '/';
      }
    } catch (error) {
      console.error('Error verificando sesión:', error);
      setUser(null);
      window.location.href = '/';
    } finally {
      setLoading(false);
    }
  };

  const loadClinicas = async () => {
    setLoadingClinicas(true);
    setError(null);
    try {
      const response = await fetch(`${config.BACKEND_URL}/api/users/clinics`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Error al cargar clínicas: ${response.status}`);
      }

      const data = await response.json();
      console.log('Clínicas recibidas:', data.clinics);
      setClinicas(data.clinics || []);
    } catch (error) {
      console.error('Error cargando clínicas:', error);
      setError('No se pudieron cargar las clínicas. Por favor, intente más tarde.');
      setClinicas([]);
    } finally {
      setLoadingClinicas(false);
    }
  };

  const handleVerDetalles = async (clinica) => {
    setSelectedClinica(clinica);
    setShowModal(true);
    setLoadingPoliticas(true);
    setPoliticas([]);
    
    // Suponemos que el UID del usuario es su CI para las políticas, o usamos user.documento si está disponible
    // El endpoint espera la CI
    const ci = user.documento || user.uid.replace('uy-ci-', ''); // Ajustar según formato de UID
    
    try {
      // Llamar al endpoint que acabamos de modificar con el filtro tenantId
      const response = await fetch(`${config.BACKEND_URL}/api/documentos/politicas/paciente/${ci}?tenantId=${clinica.id}`, {
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
        console.error("Error cargando políticas:", await response.text());
        // Si falla, mostramos array vacío
        setPoliticas([]);
      }
    } catch (error) {
      console.error('Error cargando políticas:', error);
      setPoliticas([]);
    } finally {
      setLoadingPoliticas(false);
    }
  };

  const closeModal = () => {
    setShowModal(false);
    setSelectedClinica(null);
    setPoliticas([]);
  };

  if (loading) {
    return (
      <div className="slider_area" style={{minHeight: '100vh', display: 'flex', alignItems: 'center'}}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div className="slider_text text-center">
                <h3>Cargando...</h3>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!user) {
    return (
        <div className="slider_area" style={{minHeight: '100vh', display: 'flex', alignItems: 'center'}}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div className="slider_text text-center">
                <h3 style={{color: '#1f2b7b', marginBottom: '20px'}}>Acceso Restringido</h3>
                <p style={{color: '#64748b', fontSize: '18px', marginBottom: '30px'}}>
                  Debes iniciar sesión para acceder a tus clínicas
                </p>
                <a href="/" className="boxed-btn3">
                  Volver al Inicio
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      {/* Header */}
      <div className="bradcam_area" style={{
        paddingTop: '120px', 
        paddingBottom: '80px',
        background: 'linear-gradient(135deg, #1f2b7b 0%, #3b82f6 100%)',
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
                  Mis Clínicas
                </h3>
                <p style={{
                  color: '#e2e8f0',
                  fontSize: '18px',
                  marginBottom: '0',
                  fontWeight: '400'
                }}>
                  Instituciones donde estás registrado como paciente
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

      {/* Body */}
      <div className="service_area_2" style={{paddingTop: '80px', paddingBottom: '80px', backgroundColor: '#ffffff'}}>
        <div className="container">
          
          {error && (
            <div className="alert alert-warning" role="alert" style={{marginBottom: '30px'}}>
              <i className="fa fa-exclamation-triangle me-2" />
              {error}
            </div>
          )}

          <div className="row justify-content-center">
            <div className="col-xl-10">
              {loadingClinicas ? (
                <div style={{
                  backgroundColor: '#ffffff',
                  padding: '60px',
                  borderRadius: '15px',
                  textAlign: 'center',
                  boxShadow: '0 8px 25px rgba(0,0,0,0.08)'
                }}>
                  <div className="spinner-border text-primary" role="status" style={{marginBottom: '20px'}}>
                    <span className="sr-only">Cargando...</span>
                  </div>
                  <p style={{color: '#64748b', fontSize: '16px'}}>Cargando clínicas...</p>
                </div>
              ) : clinicas.length === 0 ? (
                <div style={{
                  backgroundColor: '#ffffff',
                  padding: '60px',
                  borderRadius: '15px',
                  textAlign: 'center',
                  boxShadow: '0 8px 25px rgba(0,0,0,0.08)'
                }}>
                  <i className="flaticon-hospital" style={{fontSize: '64px', color: '#cbd5e1', marginBottom: '20px'}}></i>
                  <h4 style={{color: '#475569', marginBottom: '10px'}}>
                    No tienes clínicas asociadas
                  </h4>
                  <p style={{color: '#64748b'}}>
                    Aún no te has registrado en ninguna clínica del sistema.
                  </p>
                </div>
              ) : (
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden" style={{borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', backgroundColor: '#fff'}}>
                  <div className="table-responsive">
                    <table className="table table-hover mb-0" style={{borderCollapse: 'separate', borderSpacing: '0'}}>
                      <thead style={{backgroundColor: '#ffffff'}}>
                        <tr>
                          <th style={{padding: '20px 24px', borderBottom: '1px solid #e2e8f0', color: '#475569', fontWeight: '600', fontSize: '14px', textTransform: 'uppercase', letterSpacing: '0.5px', borderTop: 'none'}}>Institución</th>
                          <th style={{padding: '20px 24px', borderBottom: '1px solid #e2e8f0', color: '#475569', fontWeight: '600', fontSize: '14px', textTransform: 'uppercase', letterSpacing: '0.5px', borderTop: 'none'}}>Fecha de Registro</th>
                          <th style={{padding: '20px 24px', borderBottom: '1px solid #e2e8f0', color: '#475569', fontWeight: '600', fontSize: '14px', textTransform: 'uppercase', letterSpacing: '0.5px', borderTop: 'none'}}>Estado</th>
                          <th style={{padding: '20px 24px', borderBottom: '1px solid #e2e8f0', color: '#475569', fontWeight: '600', fontSize: '14px', textTransform: 'uppercase', letterSpacing: '0.5px', textAlign: 'right', borderTop: 'none'}}>Acciones</th>
                        </tr>
                      </thead>
                      <tbody>
                        {clinicas.map((clinica, index) => (
                          <tr key={clinica.id} style={{transition: 'background-color 0.2s'}}>
                            <td style={{padding: '20px 24px', verticalAlign: 'middle', borderBottom: index === clinicas.length - 1 ? 'none' : '1px solid #f1f5f9'}}>
                              <div className="d-flex align-items-center">
                                <div>
                                  <span style={{fontWeight: '600', color: '#1e293b', fontSize: '16px'}}>
                                    {clinica.nombre || `Clínica #${clinica.id}`}
                                  </span>
                                </div>
                              </div>
                            </td>
                            <td style={{padding: '20px 24px', verticalAlign: 'middle', borderBottom: index === clinicas.length - 1 ? 'none' : '1px solid #f1f5f9', color: '#64748b'}}>
                              <i className="flaticon-calendar" style={{marginRight: '8px', color: '#94a3b8'}}></i>
                              {clinica.fechaAlta ? new Date(clinica.fechaAlta).toLocaleDateString('es-UY') : 'N/A'}
                            </td>
                            <td style={{padding: '20px 24px', verticalAlign: 'middle', borderBottom: index === clinicas.length - 1 ? 'none' : '1px solid #f1f5f9'}}>
                              <span style={{
                                backgroundColor: '#d1fae5', 
                                color: '#065f46', 
                                padding: '6px 12px', 
                                borderRadius: '20px', 
                                fontSize: '12px', 
                                fontWeight: '700',
                                display: 'inline-flex',
                                alignItems: 'center'
                              }}>
                                <span style={{width: '6px', height: '6px', backgroundColor: '#059669', borderRadius: '50%', marginRight: '6px'}}></span>
                                Activo
                              </span>
                            </td>
                            <td style={{padding: '20px 24px', verticalAlign: 'middle', borderBottom: index === clinicas.length - 1 ? 'none' : '1px solid #f1f5f9', textAlign: 'right'}}>
                              <button 
                                onClick={() => handleVerDetalles(clinica)}
                                className="boxed-btn3" 
                                style={{
                                  padding: '8px 16px', 
                                  fontSize: '13px', 
                                  borderRadius: '6px', 
                                  fontWeight: '600',
                                  cursor: 'pointer',
                                  minWidth: '110px',
                                  backgroundColor: '#3b82f6',
                                  color: '#ffffff',
                                  border: 'none',
                                  display: 'inline-block',
                                  lineHeight: '1.5'
                                }}
                              >
                                Ver Detalles
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
      
      {/* Modal de Políticas */}
      {showModal && selectedClinica && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          zIndex: 9999,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <div style={{
            backgroundColor: 'white',
            borderRadius: '12px',
            width: '90%',
            maxWidth: '600px',
            maxHeight: '80vh',
            overflowY: 'auto',
            padding: '30px',
            position: 'relative',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)'
          }}>
            <button 
              onClick={closeModal}
              style={{
                position: 'absolute',
                top: '20px',
                right: '20px',
                background: 'none',
                border: 'none',
                fontSize: '24px',
                cursor: 'pointer',
                color: '#64748b'
              }}
            >
              &times;
            </button>
            
            <h3 style={{
              color: '#1e293b', 
              marginBottom: '10px',
              fontWeight: '700',
              fontSize: '24px'
            }}>
              Políticas de Acceso
            </h3>
            
            <p style={{color: '#64748b', marginBottom: '25px'}}>
              Permisos otorgados a <strong>{selectedClinica.nombre}</strong>
            </p>
            
            {loadingPoliticas ? (
              <div className="text-center py-4">
                <div className="spinner-border text-primary" role="status">
                  <span className="sr-only">Cargando...</span>
                </div>
                <p className="mt-2 text-muted">Cargando políticas...</p>
              </div>
            ) : politicas.length === 0 ? (
              <div className="text-center py-4" style={{backgroundColor: '#f8fafc', borderRadius: '8px', padding: '30px'}}>
                 <i className="flaticon-locked" style={{fontSize: '48px', color: '#cbd5e1', display: 'block', marginBottom: '15px'}}></i>
                 <p style={{color: '#64748b', marginBottom: 0}}>No hay políticas de acceso activas para esta clínica.</p>
              </div>
            ) : (
              <div className="list-group">
                {politicas.map((politica) => (
                  <div key={politica.id} className="list-group-item" style={{
                    marginBottom: '10px',
                    border: '1px solid #e2e8f0',
                    borderRadius: '8px',
                    padding: '20px'
                  }}>
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <span style={{
                        backgroundColor: '#eff6ff', 
                        color: '#1d4ed8', 
                        padding: '4px 10px', 
                        borderRadius: '4px', 
                        fontSize: '12px',
                        fontWeight: '600',
                        textTransform: 'uppercase'
                      }}>
                        {politica.tipoDocumento || 'TODOS'}
                      </span>
                      <small className="text-muted">
                         Vence: {politica.fechaVencimiento ? new Date(politica.fechaVencimiento).toLocaleDateString() : 'Nunca'}
                      </small>
                    </div>
                    
                    <h5 style={{fontSize: '16px', fontWeight: '600', marginBottom: '5px', color: '#334155'}}>
                      {politica.referencia || 'Permiso de acceso'}
                    </h5>
                    
                    <div style={{fontSize: '14px', color: '#64748b'}}>
                      <strong>Alcance:</strong> {politica.alcance || 'No especificado'}
                    </div>
                  </div>
                ))}
              </div>
            )}
            
            <div className="mt-4 text-end">
              <button 
                onClick={closeModal}
                className="boxed-btn3" 
                style={{
                  padding: '10px 20px', 
                  fontSize: '14px',
                  borderRadius: '6px'
                }}
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default MisClinicas;
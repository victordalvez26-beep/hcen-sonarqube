import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import config from '../config';
import GenericPopup from './GenericPopup';

const HistoriaClinica = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [documentosClinicos, setDocumentosClinicos] = useState([]);
  const [loadingDocumentos, setLoadingDocumentos] = useState(false);
  const [error, setError] = useState(null);
  const [descargandoHistoria, setDescargandoHistoria] = useState(false);
  const [popup, setPopup] = useState({ show: false, message: '', type: 'error' });
  const [filtros, setFiltros] = useState({
    categoria: 'todos',
    institucion: 'todos',
    profesional: 'todos'
  });

  useEffect(() => {
    checkSession();
  }, []);

  useEffect(() => {
    console.log('🔄 useEffect ejecutado. User:', user);
    if (user && user.uid) {
      console.log('📋 Usuario autenticado, cargando documentos');
      loadDocumentosPorUsuario();
    } else {
      console.log('❌ Usuario no autenticado');
      console.log('User:', user);
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
        console.log('👤 Datos del usuario recibidos:', data);
        console.log('📋 Campo documento:', data.documento);
        console.log('📋 Todos los campos del usuario:', Object.keys(data));
        setUser(data);
      } else {
        console.log('❌ Sesión no válida, redirigiendo a login');
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

  const loadDocumentosPorUsuario = async () => {
    setLoadingDocumentos(true);
    setError(null);
    console.log('🔍 Cargando documentos para usuario autenticado');
    try {
      const url = `${config.BACKEND_URL}/api/metadatos-documento/usuario`;
      console.log('🌐 URL:', url);
      
      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('📡 Response status:', response.status);
      console.log('📡 Response headers:', response.headers);

      if (!response.ok) {
        throw new Error(`Error al cargar documentos: ${response.status}`);
      }

      const documentos = await response.json();
      console.log('📄 Documentos recibidos del backend:', documentos);
      console.log('🆔 IDs de documentos del backend:', documentos.map(doc => doc.id));
      
      // Mapear los documentos del backend al formato esperado por el frontend
      const documentosMapeados = documentos.map((doc, index) => ({
        id: doc.id || (index + 1),
        fecha: doc.fechaCreacion || 'N/A',
        institucion: doc.clinicaOrigen || 'Institución Desconocida',
        categoria: doc.tipoDocumento || 'Sin Categoría',
        profesional: doc.profesionalSalud || 'Profesional Desconocido',
        descripcion: doc.descripcion || 'Sin descripción disponible',
        formatoDocumento: doc.formatoDocumento,
        uriDocumento: doc.uriDocumento,
        accesoPermitido: doc.accesoPermitido !== false,
        codDocum: doc.codDocum
      }));

      console.log('💾 Documentos mapeados guardados en estado:', documentosMapeados);
      setDocumentosClinicos(documentosMapeados);
    } catch (error) {
      console.error('❌ Error cargando documentos:', error);
      console.error('❌ Error details:', error.message);
      setError('No se pudieron cargar los documentos clínicos. Por favor, intente más tarde.');
      setDocumentosClinicos([]);
    } finally {
      setLoadingDocumentos(false);
    }
  };

  const descargarHistoriaCompleta = async () => {
    setDescargandoHistoria(true);
    try {
      const url = `${config.BACKEND_URL}/api/metadatos-documento/paciente/historia`;
      console.log('📥 Descargando historia clínica completa desde:', url);
      
      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Error al descargar historia clínica: ${response.status}`);
      }

      // Obtener el blob del archivo
      const blob = await response.blob();
      
      // Crear un enlace temporal para descargar
      const urlBlob = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = urlBlob;
      
      // Obtener el nombre del archivo del header Content-Disposition si está disponible
      const contentDisposition = response.headers.get('Content-Disposition');
      let filename = 'historia-clinica-completa.pdf';
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (filenameMatch && filenameMatch[1]) {
          filename = filenameMatch[1].replace(/['"]/g, '');
        }
      }
      
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      
      // Limpiar
      document.body.removeChild(link);
      window.URL.revokeObjectURL(urlBlob);
      
      console.log('✅ Historia clínica descargada exitosamente');
    } catch (error) {
      console.error('❌ Error descargando historia clínica:', error);
      setPopup({ show: true, message: 'No se pudo descargar la historia clínica completa. Por favor, intente más tarde.', type: 'error' });
    } finally {
      setDescargandoHistoria(false);
    }
  };


  const handleFiltroChange = (campo, valor) => {
    setFiltros(prev => ({
      ...prev,
      [campo]: valor
    }));
  };

  const documentosFiltrados = documentosClinicos.filter(doc => {
    if (filtros.categoria !== 'todos' && doc.categoria !== filtros.categoria) return false;
    if (filtros.institucion !== 'todos' && doc.institucion !== filtros.institucion) return false;
    if (filtros.profesional !== 'todos' && doc.profesional !== filtros.profesional) return false;
    return true;
  });

  const resumen = useMemo(() => {
    const total = documentosClinicos.length;
    const restringidos = documentosClinicos.filter((doc) => !doc.accesoPermitido).length;
    return {
      total,
      conAcceso: total - restringidos,
      restringidos
    };
  }, [documentosClinicos]);

  const categorias = [...new Set(documentosClinicos.map(doc => doc.categoria))];
  const instituciones = [...new Set(documentosClinicos.map(doc => doc.institucion))];
  const profesionales = [...new Set(documentosClinicos.map(doc => doc.profesional))];

  const formatDate = (isoDate) => {
    if (!isoDate) return 'Sin fecha';
    const date = new Date(isoDate);
    if (Number.isNaN(date.getTime())) return isoDate;
    return date.toLocaleString('es-UY', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatCategoria = (categoria) => {
    if (!categoria) return 'Sin Categoría';
    return categoria.replaceAll('_', ' ');
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
                  Debes iniciar sesión para acceder a tu historia clínica
                </p>
                <a href="/" className="boxed-btn3" style={{
                  padding: '12px 24px',
                  fontSize: '14px',
                  textDecoration: 'none',
                  backgroundColor: '#3b82f6',
                  color: '#ffffff',
                  borderRadius: '8px',
                  fontWeight: '600',
                  display: 'inline-block'
                }}>
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
                  Mi Historia Clínica
                </h3>
                <p style={{
                  color: '#e2e8f0',
                  fontSize: '18px',
                  marginBottom: '0',
                  fontWeight: '400'
                }}>
                  Bienvenido, <strong style={{color: '#ffffff'}}>{user.nombre || 'Usuario'}</strong>
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

      <div className="container" style={{paddingTop: '60px', paddingBottom: '60px'}}>
        {/* Botón de Descarga de Historia Completa */}
        <div className="row mb-4">
          <div className="col-12">
            <div style={{ 
              backgroundColor: '#ffffff',
              borderRadius: '12px',
              padding: '20px 24px',
              boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
              border: '1px solid #e2e8f0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              flexWrap: 'wrap',
              gap: '16px'
            }}>
              <div style={{ flex: '1', minWidth: '200px' }}>
                <div style={{ 
                  display: 'flex',
                  alignItems: 'center',
                  marginBottom: '4px'
                }}>
                  <i className="flaticon-download" style={{ 
                    fontSize: '24px', 
                    color: '#3b82f6',
                    marginRight: '12px'
                  }}></i>
                  <h5 style={{ 
                    marginBottom: '0',
                    color: '#1f2b7b',
                    fontWeight: '600',
                    fontSize: '18px'
                  }}>
                    Descargar Historia Clínica Completa
                  </h5>
                </div>
                <p style={{ 
                  marginBottom: '0',
                  marginLeft: '36px',
                  color: '#64748b',
                  fontSize: '14px'
                }}>
                  Obtén todos tus documentos médicos en un solo archivo PDF
                </p>
              </div>
              <button
                type="button"
                onClick={descargarHistoriaCompleta}
                disabled={descargandoHistoria || loadingDocumentos}
                style={{
                  padding: '12px 28px',
                  backgroundColor: descargandoHistoria || loadingDocumentos ? '#cbd5e1' : '#3b82f6',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  fontWeight: '600',
                  fontSize: '15px',
                  cursor: descargandoHistoria || loadingDocumentos ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s ease',
                  boxShadow: descargandoHistoria || loadingDocumentos ? 'none' : '0 2px 8px rgba(59, 130, 246, 0.25)',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '8px',
                  whiteSpace: 'nowrap',
                  pointerEvents: descargandoHistoria || loadingDocumentos ? 'none' : 'auto'
                }}
                onMouseEnter={(e) => {
                  if (!descargandoHistoria && !loadingDocumentos) {
                    e.currentTarget.style.backgroundColor = '#2563eb';
                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(59, 130, 246, 0.35)';
                    e.currentTarget.style.transform = 'translateY(-1px)';
                  }
                }}
                onMouseLeave={(e) => {
                  if (!descargandoHistoria && !loadingDocumentos) {
                    e.currentTarget.style.backgroundColor = '#3b82f6';
                    e.currentTarget.style.boxShadow = '0 2px 8px rgba(59, 130, 246, 0.25)';
                    e.currentTarget.style.transform = 'translateY(0)';
                  }
                }}
              >
                {descargandoHistoria ? (
                  <>
                    <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true" style={{width: '16px', height: '16px', borderWidth: '2px'}}></span>
                    Descargando...
                  </>
                ) : (
                  <>
                    <i className="flaticon-download" style={{fontSize: '18px'}}></i>
                    Descargar PDF
                  </>
                )}
              </button>
            </div>
          </div>
        </div>

        {/* Tarjetas de Resumen */}
        <div className="row g-4 mb-4">
          <div className="col-md-4">
            <div className="card shadow-sm h-100" style={{ borderRadius: '16px' }}>
              <div className="card-body text-center">
                <p className="text-muted text-uppercase mb-1">Documentos Totales</p>
                <h2 style={{ fontWeight: '700' }}>{resumen.total}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card shadow-sm h-100" style={{ borderRadius: '16px' }}>
              <div className="card-body text-center">
                <p className="text-muted text-uppercase mb-1">Accesos Permitidos</p>
                <h2 style={{ fontWeight: '700', color: '#16a34a' }}>{resumen.conAcceso}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card shadow-sm h-100" style={{ borderRadius: '16px' }}>
              <div className="card-body text-center">
                <p className="text-muted text-uppercase mb-1">Restringidos</p>
                <h2 style={{ fontWeight: '700', color: '#dc2626' }}>{resumen.restringidos}</h2>
              </div>
            </div>
          </div>
        </div>

        {error && (
          <div className="alert alert-warning" role="alert" style={{marginBottom: '30px'}}>
            <i className="fa fa-exclamation-triangle me-2" />
            {error}
          </div>
        )}

        <div className="row">
          {/* Filtros - Barra lateral */}
          <div className="col-xl-3 col-lg-4">
            <div className="sidebar_widget" style={{
              backgroundColor: '#ffffff',
              padding: '35px',
              borderRadius: '15px',
              marginBottom: '30px',
              boxShadow: '0 10px 30px rgba(0,0,0,0.1)',
              border: '1px solid #e2e8f0'
            }}>
              <h4 style={{
                marginBottom: '25px', 
                color: '#1f2b7b',
                fontSize: '24px',
                fontWeight: '700',
                borderBottom: '3px solid #3b82f6',
                paddingBottom: '10px'
              }}>
                <i className="flaticon-filter" style={{marginRight: '8px'}}></i>
                Filtros
              </h4>
              
              <div className="widget_inner" style={{marginBottom: '40px'}}>
                <h5 style={{
                  fontSize: '16px', 
                  marginBottom: '20px',
                  color: '#2d3748',
                  fontWeight: '600'
                }}>
                  <i className="flaticon-file" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                  Categoría
                </h5>
                <div className="" style={{width: '100%'}}>
                  <select 
                    value={filtros.categoria} 
                    onChange={(e) => handleFiltroChange('categoria', e.target.value)}
                    style={{
                      width: '100%', 
                      padding: '15px 20px', 
                      border: '2px solid #e2e8f0', 
                      borderRadius: '10px',
                      backgroundColor: '#ffffff',
                      fontSize: '15px',
                      color: '#2d3748',
                      transition: 'all 0.3s ease',
                      outline: 'none',
                      height: '50px'
                    }}
                    onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
                    onBlur={(e) => e.target.style.borderColor = '#e2e8f0'}
                  >
                    <option value="todos">Todas las categorías</option>
                    {categorias.map(categoria => (
                      <option key={categoria} value={categoria}>{formatCategoria(categoria)}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="widget_inner" style={{marginBottom: '40px'}}>
                <h5 style={{
                  fontSize: '16px', 
                  marginBottom: '20px',
                  color: '#2d3748',
                  fontWeight: '600'
                }}>
                  <i className="flaticon-hospital" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                  Institución
                </h5>
                <div className="" style={{width: '100%'}}>
                  <select 
                    value={filtros.institucion} 
                    onChange={(e) => handleFiltroChange('institucion', e.target.value)}
                    style={{
                      width: '100%', 
                      padding: '15px 20px', 
                      border: '2px solid #e2e8f0', 
                      borderRadius: '10px',
                      backgroundColor: '#ffffff',
                      fontSize: '15px',
                      color: '#2d3748',
                      transition: 'all 0.3s ease',
                      outline: 'none',
                      height: '50px'
                    }}
                    onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
                    onBlur={(e) => e.target.style.borderColor = '#e2e8f0'}
                  >
                    <option value="todos">Todas las instituciones</option>
                    {instituciones.map(institucion => (
                      <option key={institucion} value={institucion}>{institucion}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="widget_inner" style={{marginBottom: '40px'}}>
                <h5 style={{
                  fontSize: '16px', 
                  marginBottom: '20px',
                  color: '#2d3748',
                  fontWeight: '600'
                }}>
                  <i className="flaticon-doctor" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                  Profesional
                </h5>
                <div className="" style={{width: '100%'}}>
                  <select 
                    value={filtros.profesional} 
                    onChange={(e) => handleFiltroChange('profesional', e.target.value)}
                    style={{
                      width: '100%', 
                      padding: '15px 20px', 
                      border: '2px solid #e2e8f0', 
                      borderRadius: '10px',
                      backgroundColor: '#ffffff',
                      fontSize: '15px',
                      color: '#2d3748',
                      transition: 'all 0.3s ease',
                      outline: 'none',
                      height: '50px'
                    }}
                    onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
                    onBlur={(e) => e.target.style.borderColor = '#e2e8f0'}
                  >
                    <option value="todos">Todos los profesionales</option>
                    {profesionales.map(profesional => (
                      <option key={profesional} value={profesional}>{profesional}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="widget_inner" style={{
                backgroundColor: '#f7fafc',
                padding: '20px',
                borderRadius: '10px',
                border: '1px solid #e2e8f0'
              }}>
                <h5 style={{
                  fontSize: '16px', 
                  marginBottom: '15px',
                  color: '#2d3748',
                  fontWeight: '600'
                }}>
                  <i className="flaticon-search" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                  Resultados
                </h5>
                <div style={{
                  backgroundColor: '#3b82f6',
                  color: '#ffffff',
                  padding: '12px 16px',
                  borderRadius: '8px',
                  textAlign: 'center',
                  fontWeight: '600',
                  fontSize: '16px'
                }}>
                  {documentosFiltrados.length} de {documentosClinicos.length} documentos
                </div>
              </div>
            </div>
          </div>

          {/* Lista de documentos */}
          <div className="col-xl-9 col-lg-8">
            {loadingDocumentos ? (
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
                <p style={{color: '#64748b', fontSize: '16px'}}>Cargando documentos clínicos...</p>
              </div>
            ) : error ? (
              <div style={{
                backgroundColor: '#fff1f2',
                padding: '40px',
                borderRadius: '15px',
                textAlign: 'center',
                border: '1px solid #fecaca'
              }}>
                <i className="flaticon-warning" style={{fontSize: '48px', color: '#dc2626', marginBottom: '15px'}}></i>
                <p style={{color: '#dc2626', fontSize: '16px', marginBottom: '0'}}>{error}</p>
              </div>
            ) : documentosFiltrados.length === 0 ? (
              <div style={{
                backgroundColor: '#ffffff',
                padding: '60px',
                borderRadius: '15px',
                textAlign: 'center',
                boxShadow: '0 8px 25px rgba(0,0,0,0.08)'
              }}>
                <i className="flaticon-folder" style={{fontSize: '64px', color: '#cbd5e1', marginBottom: '20px'}}></i>
                <h4 style={{color: '#475569', marginBottom: '10px'}}>
                  {documentosClinicos.length === 0 ? 'No hay documentos disponibles' : 'No se encontraron documentos'}
                </h4>
                <p style={{color: '#64748b'}}>
                  {documentosClinicos.length === 0 
                    ? 'Aún no tienes documentos clínicos registrados en el sistema.'
                    : 'Intenta cambiar los filtros para ver más resultados.'}
                </p>
              </div>
            ) : (
              <div className="row">
                {documentosFiltrados.map(documento => (
                <div key={documento.id} className="col-xl-12" style={{marginBottom: '35px'}}>
                  <div className="single_blog" style={{
                    backgroundColor: '#ffffff',
                    borderRadius: '15px',
                    boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
                    overflow: 'hidden',
                    transition: 'all 0.3s ease',
                    border: '1px solid #e2e8f0',
                    position: 'relative'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-5px)';
                    e.currentTarget.style.boxShadow = '0 15px 35px rgba(0,0,0,0.15)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 8px 25px rgba(0,0,0,0.08)';
                  }}>
                    <div className="row no-gutters">
                      {/* Imagen/Icono */}
                      <div className="col-xl-3 col-lg-4">
                        <div className="blog_thumb" style={{
                          height: '180px',
                          background: `linear-gradient(135deg, #1f2b7b 0%, #3b82f6 50%, #06b6d4 100%)`,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          position: 'relative',
                          overflow: 'hidden'
                        }}>
                          <div style={{
                            position: 'absolute',
                            top: '0',
                            left: '0',
                            right: '0',
                            bottom: '0',
                            background: 'rgba(255,255,255,0.1)',
                            backdropFilter: 'blur(10px)'
                          }}></div>
                          <i className={`flaticon-${documento.categoria === 'Policlínica' ? 'doctor' : 
                            documento.categoria === 'Laboratorio' ? 'test-tube' :
                            documento.categoria === 'Imagenología' ? 'x-ray' :
                            documento.categoria === 'Vacunación' ? 'syringe' : 
                            documento.categoria === 'Especialidad' ? 'medical' : 'file'}`} 
                            style={{
                              fontSize: '48px', 
                              color: '#ffffff',
                              zIndex: '2',
                              position: 'relative',
                              textShadow: '0 2px 4px rgba(0,0,0,0.3)'
                            }}></i>
                        </div>
                      </div>
                      
                      {/* Contenido */}
                      <div className="col-xl-9 col-lg-8">
                        <div className="blog_content" style={{padding: '25px'}}>
                          <div className="blog_meta" style={{marginBottom: '20px'}}>
                            <span style={{
                              backgroundColor: '#1f2b7b',
                              color: '#ffffff',
                              padding: '6px 14px',
                              borderRadius: '20px',
                              fontSize: '12px',
                              fontWeight: '700',
                              textTransform: 'uppercase',
                              letterSpacing: '0.5px'
                            }}>
                              {formatCategoria(documento.categoria)}
                            </span>
                            <span style={{
                              marginLeft: '12px', 
                              color: '#64748b', 
                              fontSize: '13px',
                              fontWeight: '500'
                            }}>
                              <i className="flaticon-calendar" style={{marginRight: '5px'}}></i>
                              {formatDate(documento.fecha)}
                            </span>
                          </div>
                          
                          <h3 style={{
                            fontSize: '20px',
                            fontWeight: '700',
                            marginBottom: '15px',
                            color: '#1e293b',
                            lineHeight: '1.3'
                          }}>
                            {documento.institucion}
                          </h3>
                          
                          <div style={{
                            backgroundColor: '#f8fafc',
                            padding: '10px 14px',
                            borderRadius: '8px',
                            marginBottom: '15px',
                            border: '1px solid #e2e8f0'
                          }}>
                            <p style={{
                              color: '#475569',
                              marginBottom: '0',
                              fontSize: '13px',
                              fontWeight: '500'
                            }}>
                              <i className="flaticon-user" style={{marginRight: '6px', color: '#3b82f6'}}></i>
                              <strong>Profesional:</strong> {documento.profesional}
                            </p>
                          </div>
                          
                          <p style={{
                            color: '#475569',
                            marginBottom: '20px',
                            lineHeight: '1.6',
                            fontSize: '14px'
                          }}>
                            {documento.descripcion}
                          </p>
                          
                          <div className="d-flex justify-content-between align-items-center" style={{
                            paddingTop: '15px',
                            borderTop: '1px solid #e2e8f0'
                          }}>
                            <div style={{
                              color: '#64748b', 
                              fontSize: '13px',
                              fontWeight: '500'
                            }}>
                              <i className="flaticon-calendar" style={{marginRight: '6px', color: '#3b82f6'}}></i>
                              {formatDate(documento.fecha)}
                            </div>
                            <div style={{display: 'flex', gap: '10px'}}>
                              {documento.id && documento.uriDocumento ? (
                                <a
                                  href={`${config.BACKEND_URL}/api/metadatos-documento/${documento.id}/descargar`}
                                  download={`${documento.categoria || 'documento'}-${documento.id}.pdf`}
                                  className="boxed-btn3" 
                                  style={{
                                    padding: '8px 20px',
                                    fontSize: '13px',
                                    textDecoration: 'none',
                                    backgroundColor: '#10b981',
                                    color: '#ffffff',
                                    borderRadius: '6px',
                                    fontWeight: '600',
                                    transition: 'all 0.3s ease',
                                    border: 'none',
                                    cursor: 'pointer',
                                    display: 'inline-block'
                                  }}
                                  onMouseEnter={(e) => {
                                    e.target.style.backgroundColor = '#059669';
                                    e.target.style.transform = 'translateY(-2px)';
                                  }}
                                  onMouseLeave={(e) => {
                                    e.target.style.backgroundColor = '#10b981';
                                    e.target.style.transform = 'translateY(0)';
                                  }}
                                >
                                  <i className="flaticon-download" style={{marginRight: '4px'}}></i>
                                  Descargar PDF
                                </a>
                              ) : (
                                <span
                                  className="boxed-btn3" 
                                  style={{
                                    padding: '8px 20px',
                                    fontSize: '13px',
                                    backgroundColor: '#9ca3af',
                                    color: '#ffffff',
                                    borderRadius: '6px',
                                    fontWeight: '600',
                                    cursor: 'not-allowed',
                                    display: 'inline-block',
                                    opacity: 0.6
                                  }}
                                  title="Documento no disponible para descarga"
                                >
                                  <i className="flaticon-download" style={{marginRight: '4px'}}></i>
                                  No disponible
                                </span>
                              )}
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
              </div>
            )}
          </div>
        </div>
      </div>

      <GenericPopup
        show={popup.show}
        onClose={() => setPopup({ ...popup, show: false })}
        message={popup.message}
        type={popup.type}
      />
    </>
  );
};

export default HistoriaClinica;

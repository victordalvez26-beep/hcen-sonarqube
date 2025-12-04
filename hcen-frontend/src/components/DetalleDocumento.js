import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import config from '../config';
import GenericPopup from './GenericPopup';

const DetalleDocumento = () => {
  const { id } = useParams();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [documento, setDocumento] = useState(null);
  const [popup, setPopup] = useState({ show: false, message: '', type: 'info', title: '' });

  // Datos de respaldo para demo (se eliminará cuando haya datos reales)
  const documentosClinicos = useMemo(() => [
    {
      id: 1,
      fecha: '2024-10-15',
      institucion: 'Hospital de Clínicas',
      categoria: 'Policlínica',
      profesional: 'Dr. María González',
      descripcion: 'Consulta de control de rutina con evaluación general del estado de salud.',
      detalles: {
        motivo: 'Control de rutina anual',
        sintomas: 'Sin síntomas reportados',
        examenFisico: 'Presión arterial: 120/80 mmHg, Peso: 70kg, Altura: 1.75m',
        diagnostico: 'Estado de salud general bueno',
        tratamiento: 'Continuar con hábitos saludables, próxima consulta en 6 meses',
        observaciones: 'Paciente colaborador, sin factores de riesgo identificados'
      },
      archivos: [
        { nombre: 'consulta_20241015.pdf', tipo: 'PDF', tamaño: '245 KB' },
        { nombre: 'receta_medicamentos.pdf', tipo: 'PDF', tamaño: '89 KB' }
      ]
    },
    {
      id: 2,
      fecha: '2024-10-12',
      institucion: 'Laboratorio Central',
      categoria: 'Laboratorio',
      profesional: 'Dr. Carlos Pérez',
      descripcion: 'Análisis de sangre completo incluyendo hemograma y perfil bioquímico.',
      detalles: {
        motivo: 'Control bioquímico anual',
        examenes: 'Hemograma completo, Glicemia, Colesterol total, HDL, LDL, Triglicéridos',
        resultados: 'Todos los valores dentro de parámetros normales',
        observaciones: 'Muestra tomada en ayunas de 12 horas'
      },
      archivos: [
        { nombre: 'laboratorio_20241012.pdf', tipo: 'PDF', tamaño: '1.2 MB' },
        { nombre: 'resultados_detallados.xlsx', tipo: 'Excel', tamaño: '456 KB' }
      ]
    },
    {
      id: 3,
      fecha: '2024-10-08',
      institucion: 'Centro de Diagnóstico por Imágenes',
      categoria: 'Imagenología',
      profesional: 'Dr. Ana Rodríguez',
      descripcion: 'Radiografía de tórax para control rutinario y evaluación pulmonar.',
      detalles: {
        motivo: 'Control radiológico anual',
        tecnica: 'Radiografía de tórax PA y lateral',
        hallazgos: 'Pulmones sin alteraciones patológicas, corazón de tamaño normal',
        conclusion: 'Radiografía de tórax normal',
        observaciones: 'Estudio realizado con técnica estándar'
      },
      archivos: [
        { nombre: 'rx_torax_20241008.pdf', tipo: 'PDF', tamaño: '2.1 MB' },
        { nombre: 'imagen_digital.jpg', tipo: 'Imagen', tamaño: '3.4 MB' }
      ]
    },
    {
      id: 4,
      fecha: '2024-10-15',
      institucion: 'Hospital de Clínicas',
      categoria: 'Policlínica',
      profesional: 'Dr. María González',
      descripcion: 'Prescripción de medicamentos para control de presión arterial.',
      detalles: {
        motivo: 'Control de hipertensión arterial',
        medicamentos: 'Losartán 50mg 1 comprimido al día, Hidroclorotiazida 12.5mg 1 comprimido al día',
        duracion: 'Tratamiento por 3 meses',
        seguimiento: 'Control en 1 mes para evaluar respuesta',
        observaciones: 'Tomar medicamentos en ayunas'
      },
      archivos: [
        { nombre: 'receta_20241015.pdf', tipo: 'PDF', tamaño: '156 KB' }
      ]
    },
    {
      id: 5,
      fecha: '2024-10-05',
      institucion: 'Instituto de Cardiología',
      categoria: 'Especialidad',
      profesional: 'Dr. Roberto Silva',
      descripcion: 'Evaluación cardiológica completa con electrocardiograma incluido.',
      detalles: {
        motivo: 'Evaluación cardiológica de rutina',
        examenes: 'Electrocardiograma, Ecocardiograma Doppler',
        hallazgos: 'Función cardíaca normal, sin arritmias',
        conclusion: 'Función cardíaca normal',
        seguimiento: 'Próxima evaluación en 1 año'
      },
      archivos: [
        { nombre: 'consulta_cardiologia.pdf', tipo: 'PDF', tamaño: '1.8 MB' },
        { nombre: 'ecg_20241005.pdf', tipo: 'PDF', tamaño: '890 KB' },
        { nombre: 'ecocardiograma.pdf', tipo: 'PDF', tamaño: '2.3 MB' }
      ]
    },
    {
      id: 6,
      fecha: '2024-09-28',
      institucion: 'Centro de Vacunación',
      categoria: 'Vacunación',
      profesional: 'Enf. Laura Martínez',
      descripcion: 'Aplicación de vacuna antigripal estacional para prevención.',
      detalles: {
        motivo: 'Vacunación antigripal estacional',
        vacuna: 'Vacuna antigripal trivalente 2024',
        lote: 'Lote: FLU2024-001',
        reacciones: 'Sin reacciones adversas reportadas',
        proxima: 'Próxima vacunación en octubre 2025'
      },
      archivos: [
        { nombre: 'carnet_vacunacion.pdf', tipo: 'PDF', tamaño: '678 KB' }
      ]
    },
    {
      id: 7,
      fecha: '2024-09-15',
      institucion: 'Centro de Diagnóstico por Imágenes',
      categoria: 'Imagenología',
      profesional: 'Dr. Patricia López',
      descripcion: 'Ecografía abdominal para evaluación de órganos internos.',
      detalles: {
        motivo: 'Evaluación de dolor abdominal',
        tecnica: 'Ecografía abdominal completa',
        hallazgos: 'Órganos abdominales sin alteraciones patológicas',
        conclusion: 'Ecografía abdominal normal',
        observaciones: 'Estudio realizado con transductor de alta frecuencia'
      },
      archivos: [
        { nombre: 'ecografia_abdominal.pdf', tipo: 'PDF', tamaño: '1.5 MB' },
        { nombre: 'imagenes_ecografia.zip', tipo: 'ZIP', tamaño: '4.2 MB' }
      ]
    },
    {
      id: 8,
      fecha: '2024-09-10',
      institucion: 'Laboratorio Central',
      categoria: 'Laboratorio',
      profesional: 'Dr. Miguel Torres',
      descripcion: 'Análisis de orina completo con cultivo y antibiograma.',
      detalles: {
        motivo: 'Evaluación de infección urinaria',
        examenes: 'Uroanálisis completo, Cultivo de orina, Antibiograma',
        resultados: 'Cultivo negativo, sin evidencia de infección',
        conclusion: 'Análisis de orina normal',
        observaciones: 'Muestra de orina media obtenida correctamente'
      },
      archivos: [
        { nombre: 'analisis_orina.pdf', tipo: 'PDF', tamaño: '987 KB' }
      ]
    }
  ], []);

  const checkSession = useCallback(async () => {
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
      }
    } catch (error) {
      console.error('Error verificando sesión:', error);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  const loadDocumento = useCallback(async (documentoId) => {
    try {
      const response = await fetch(`${config.BACKEND_URL}/hcen-rndc-service/api/rndc/documentos/${documentoId}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-Profesional-Id': user?.uid || ''
        }
      });

      if (!response.ok) {
        // Si no existe en el backend, usar datos de respaldo
        const doc = documentosClinicos.find(d => d.id === parseInt(documentoId));
        if (doc) {
          setDocumento(doc);
        } else {
          throw new Error('Documento no encontrado');
        }
        return;
      }

      const documentoBackend = await response.json();
      
      // Mapear el documento del backend al formato esperado
      const documentoMapeado = {
        id: documentoBackend.id || parseInt(documentoId),
        fecha: documentoBackend.fechaCreacion || 'N/A',
        institucion: documentoBackend.clinicaOrigen || 'Institución Desconocida',
        categoria: documentoBackend.tipoDocumento || 'Sin Categoría',
        profesional: documentoBackend.profesionalSalud || 'Profesional Desconocido',
        descripcion: documentoBackend.descripcion || 'Sin descripción disponible',
        formatoDocumento: documentoBackend.formatoDocumento,
        uriDocumento: documentoBackend.uriDocumento,
        accesoPermitido: documentoBackend.accesoPermitido !== false,
        detalles: {
          motivo: documentoBackend.descripcion || 'No especificado',
          observaciones: 'Documento del sistema RNDC'
        },
        archivos: documentoBackend.uriDocumento ? [
          { 
            nombre: `documento_${documentoId}.pdf`, 
            tipo: documentoBackend.formatoDocumento || 'PDF',
            url: documentoBackend.uriDocumento
          }
        ] : []
      };

      setDocumento(documentoMapeado);
    } catch (error) {
      console.error('Error cargando documento:', error);
      // Intentar usar datos de respaldo
      const doc = documentosClinicos.find(d => d.id === parseInt(documentoId));
      if (doc) {
        setDocumento(doc);
      } else {
        setPopup({ show: true, message: 'No se pudo cargar el documento. Por favor, intente más tarde.', type: 'error' });
      }
    }
  }, [documentosClinicos, user?.uid]);

  useEffect(() => {
    checkSession();
  }, [checkSession]);

  useEffect(() => {
    if (user && id) {
      loadDocumento(id);
    }
  }, [user, id, loadDocumento]);

  const handleDownload = (archivo) => {
    // Verificar si es una URL de ejemplo
    if (archivo.url && archivo.url.includes('ejemplo.com')) {
      setPopup({ 
        show: true, 
        message: `Documento: ${archivo.nombre}\n\nEste es un documento de demostración.\nEn una implementación real, el archivo se descargaría desde el servidor del hospital.`, 
        type: 'warning',
        title: 'Documento de Demostración'
      });
      return;
    }
    
    // Si es una URL real, intentar descargar
    if (archivo.url && archivo.url.startsWith('http')) {
      const link = document.createElement('a');
      link.href = archivo.url;
      link.download = archivo.nombre;
      link.target = '_blank';
      link.click();
    } else {
      // Simular descarga si no hay URL
      const link = document.createElement('a');
      link.href = `#`;
      link.download = archivo.nombre;
      link.click();
      
      setPopup({ 
        show: true, 
        message: `Descargando: ${archivo.nombre}\n\nEn una implementación real, este archivo se descargaría desde el servidor.`, 
        type: 'info',
        title: 'Descarga'
      });
    }
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
                  Debes iniciar sesión para acceder a los detalles del documento
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

  if (!documento) {
    return (
      <div className="slider_area" style={{minHeight: '100vh', display: 'flex', alignItems: 'center'}}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div className="slider_text text-center">
                <h3>Documento no encontrado</h3>
                <a href="/historia-clinica" className="boxed-btn3" style={{
                  marginTop: '20px',
                  padding: '12px 24px',
                  fontSize: '14px',
                  textDecoration: 'none',
                  backgroundColor: '#3b82f6',
                  color: '#ffffff',
                  borderRadius: '8px',
                  fontWeight: '600',
                  display: 'inline-block'
                }}>
                  Volver a Historia Clínica
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
        paddingBottom: '60px',
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
                  fontSize: '36px',
                  fontWeight: '700',
                  marginBottom: '15px',
                  textShadow: '0 2px 4px rgba(0,0,0,0.3)'
                }}>
                  Detalle del Documento
                </h3>
                <p style={{
                  color: '#e2e8f0',
                  fontSize: '16px',
                  marginBottom: '0',
                  fontWeight: '400'
                }}>
                  {documento.institucion} - {documento.fecha}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container" style={{paddingTop: '80px', paddingBottom: '60px'}}>
        <div className="row">
          <div className="col-xl-12">
            <div className="single_blog" style={{
              backgroundColor: '#ffffff',
              borderRadius: '15px',
              boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
              overflow: 'hidden',
              border: '1px solid #e2e8f0',
              marginBottom: '30px'
            }}>
              <div className="row no-gutters">
                {/* Imagen/Icono */}
                <div className="col-xl-3 col-lg-4">
                  <div className="blog_thumb" style={{
                    height: '250px',
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
                        fontSize: '64px', 
                        color: '#ffffff',
                        zIndex: '2',
                        position: 'relative',
                        textShadow: '0 2px 4px rgba(0,0,0,0.3)'
                      }}></i>
                  </div>
                </div>
                
                {/* Información básica */}
                <div className="col-xl-9 col-lg-8">
                  <div className="blog_content" style={{padding: '30px'}}>
                    <div className="blog_meta" style={{marginBottom: '20px'}}>
                      <span style={{
                        backgroundColor: '#1f2b7b',
                        color: '#ffffff',
                        padding: '8px 16px',
                        borderRadius: '25px',
                        fontSize: '14px',
                        fontWeight: '700',
                        textTransform: 'uppercase',
                        letterSpacing: '0.5px'
                      }}>
                        {documento.categoria}
                      </span>
                      <span style={{
                        marginLeft: '15px', 
                        color: '#64748b', 
                        fontSize: '14px',
                        fontWeight: '500'
                      }}>
                        <i className="flaticon-calendar" style={{marginRight: '5px'}}></i>
                        {documento.fecha}
                      </span>
                    </div>
                    
                    <h3 style={{
                      fontSize: '28px',
                      fontWeight: '700',
                      marginBottom: '15px',
                      color: '#1e293b',
                      lineHeight: '1.3'
                    }}>
                      {documento.institucion}
                    </h3>
                    
                    <div style={{
                      backgroundColor: '#f8fafc',
                      padding: '15px 20px',
                      borderRadius: '10px',
                      marginBottom: '20px',
                      border: '1px solid #e2e8f0'
                    }}>
                      <p style={{
                        color: '#475569',
                        marginBottom: '0',
                        fontSize: '15px',
                        fontWeight: '500'
                      }}>
                        <i className="flaticon-user" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                        <strong>Profesional:</strong> {documento.profesional}
                      </p>
                    </div>
                    
                    <p style={{
                      color: '#475569',
                      marginBottom: '25px',
                      lineHeight: '1.7',
                      fontSize: '16px'
                    }}>
                      {documento.descripcion}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Detalles del documento */}
            <div className="col-xl-12">
              <div className="single_blog" style={{
                backgroundColor: '#ffffff',
                borderRadius: '15px',
                boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
                border: '1px solid #e2e8f0',
                marginBottom: '30px'
              }}>
                <div style={{padding: '30px'}}>
                  <h4 style={{
                    fontSize: '24px',
                    fontWeight: '700',
                    marginBottom: '25px',
                    color: '#1e293b',
                    borderBottom: '3px solid #3b82f6',
                    paddingBottom: '10px'
                  }}>
                    <i className="flaticon-document" style={{marginRight: '10px', color: '#3b82f6'}}></i>
                    Detalles del Documento
                  </h4>
                  
                  <div className="row">
                    {Object.entries(documento.detalles).map(([key, value]) => (
                      <div key={key} className="col-xl-6 col-lg-6" style={{marginBottom: '20px'}}>
                        <div style={{
                          backgroundColor: '#f8fafc',
                          padding: '15px 20px',
                          borderRadius: '10px',
                          border: '1px solid #e2e8f0'
                        }}>
                          <h6 style={{
                            color: '#1f2b7b',
                            fontSize: '14px',
                            fontWeight: '600',
                            marginBottom: '8px',
                            textTransform: 'capitalize'
                          }}>
                            {key.replace(/([A-Z])/g, ' $1').trim()}:
                          </h6>
                          <p style={{
                            color: '#475569',
                            marginBottom: '0',
                            fontSize: '14px',
                            lineHeight: '1.5'
                          }}>
                            {value}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {/* Archivos adjuntos */}
            <div className="col-xl-12">
              <div className="single_blog" style={{
                backgroundColor: '#ffffff',
                borderRadius: '15px',
                boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
                border: '1px solid #e2e8f0'
              }}>
                <div style={{padding: '30px'}}>
                  <h4 style={{
                    fontSize: '24px',
                    fontWeight: '700',
                    marginBottom: '25px',
                    color: '#1e293b',
                    borderBottom: '3px solid #3b82f6',
                    paddingBottom: '10px'
                  }}>
                    <i className="flaticon-attachment" style={{marginRight: '10px', color: '#3b82f6'}}></i>
                    Archivos Adjuntos
                  </h4>
                  
                  <div className="row">
                    {documento.archivos.map((archivo, index) => (
                      <div key={index} className="col-xl-4 col-lg-6 col-md-6" style={{marginBottom: '20px'}}>
                        <div style={{
                          backgroundColor: '#f8fafc',
                          padding: '20px',
                          borderRadius: '10px',
                          border: '1px solid #e2e8f0',
                          textAlign: 'center',
                          transition: 'all 0.3s ease'
                        }}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.backgroundColor = '#e0f2fe';
                          e.currentTarget.style.borderColor = '#3b82f6';
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.backgroundColor = '#f8fafc';
                          e.currentTarget.style.borderColor = '#e2e8f0';
                        }}>
                          <i className="flaticon-file" style={{
                            fontSize: '32px',
                            color: '#3b82f6',
                            marginBottom: '15px'
                          }}></i>
                          <h6 style={{
                            color: '#1e293b',
                            fontSize: '14px',
                            fontWeight: '600',
                            marginBottom: '8px'
                          }}>
                            {archivo.nombre}
                          </h6>
                          <p style={{
                            color: '#64748b',
                            fontSize: '12px',
                            marginBottom: '15px'
                          }}>
                            {archivo.tipo} • {archivo.tamaño}
                          </p>
                          <button
                            onClick={() => handleDownload(archivo)}
                            style={{
                              padding: '8px 16px',
                              fontSize: '12px',
                              backgroundColor: '#3b82f6',
                              color: '#ffffff',
                              border: 'none',
                              borderRadius: '6px',
                              cursor: 'pointer',
                              transition: 'all 0.3s ease',
                              width: '100%'
                            }}
                            onMouseEnter={(e) => {
                              e.target.style.backgroundColor = '#1d4ed8';
                            }}
                            onMouseLeave={(e) => {
                              e.target.style.backgroundColor = '#3b82f6';
                            }}
                          >
                            <i className="flaticon-download" style={{marginRight: '5px'}}></i>
                            Ver Documento
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {/* Botones de acción */}
            <div className="col-xl-12">
              <div className="text-center" style={{marginTop: '30px'}}>
                <a href="/historia-clinica" className="boxed-btn3" style={{
                  marginRight: '15px',
                  padding: '12px 24px',
                  fontSize: '14px',
                  textDecoration: 'none',
                  backgroundColor: '#6b7280',
                  color: '#ffffff',
                  borderRadius: '8px',
                  fontWeight: '600',
                  display: 'inline-block'
                }}>
                  <i className="flaticon-back" style={{marginRight: '6px'}}></i>
                  Volver a Historia Clínica
                </a>
                <button
                  onClick={() => window.print()}
                  className="boxed-btn3" 
                  style={{
                    padding: '12px 24px',
                    fontSize: '14px',
                    backgroundColor: '#10b981',
                    color: '#ffffff',
                    borderRadius: '8px',
                    fontWeight: '600',
                    border: 'none',
                    cursor: 'pointer'
                  }}
                >
                  <i className="flaticon-printer" style={{marginRight: '6px'}}></i>
                  Imprimir Documento
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <GenericPopup
        show={popup.show}
        onClose={() => setPopup({ ...popup, show: false })}
        message={popup.message}
        type={popup.type}
        title={popup.title}
      />
    </>
  );
};

export default DetalleDocumento;

import React, { useEffect, useState } from 'react';
import config from '../config';

const API_BASE = `${config.BACKEND_URL}/api/reportes`;

const formatDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const numberFormatter = new Intl.NumberFormat('es-UY');

const ReportesAdmin = () => {
  const today = new Date();
  const lastWeek = new Date();
  lastWeek.setDate(today.getDate() - 6);

  const [fechas, setFechas] = useState({
    inicio: formatDate(lastWeek),
    fin: formatDate(today)
  });

  const [resumenDocs, setResumenDocs] = useState(null);
  const [resumenUsuarios, setResumenUsuarios] = useState(null);
  const [evolucion, setEvolucion] = useState([]);
  const [porEspecialidad, setPorEspecialidad] = useState([]);
  const [porFormato, setPorFormato] = useState([]);
  const [porTenant, setPorTenant] = useState([]);
  const [profesionales, setProfesionales] = useState([]);
  const [resumenPoliticas, setResumenPoliticas] = useState(null);
  const [politicasPorAlcance, setPoliticasPorAlcance] = useState([]);
  const [politicasPorDuracion, setPoliticasPorDuracion] = useState([]);
  const [evolucionPoliticas, setEvolucionPoliticas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchJson = async (url) => {
    try {
      const response = await fetch(url, {
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

      if (!response.ok) {
        // Intentar parsear como JSON si es posible
        let errorMessage = `Error HTTP ${response.status}`;
        if (isJson && responseBody) {
          try {
            const errorJson = JSON.parse(responseBody);
            errorMessage += ': ' + (errorJson.error || errorJson.message || JSON.stringify(errorJson));
          } catch (e) {
            // Si no es JSON válido, usar el texto tal cual (limitado a 200 caracteres)
            errorMessage += ': ' + (responseBody.length > 200 ? responseBody.substring(0, 200) + '...' : responseBody);
          }
        } else if (responseBody) {
          // Si es HTML u otro formato, extraer información útil
          if (responseBody.includes('<title>')) {
            const titleMatch = responseBody.match(/<title>(.*?)<\/title>/i);
            errorMessage += titleMatch ? ': ' + titleMatch[1] : ': Respuesta HTML recibida';
          } else {
            errorMessage += ': ' + (responseBody.length > 200 ? responseBody.substring(0, 200) + '...' : responseBody);
          }
        }
        throw new Error(errorMessage);
      }

      // Parsear como JSON solo si el content-type indica que es JSON
      if (isJson && responseBody) {
        try {
          return JSON.parse(responseBody);
        } catch (e) {
          throw new Error(`Error parseando JSON: ${e.message}. Respuesta: ${responseBody.substring(0, 100)}...`);
        }
      } else if (responseBody) {
        // Si no es JSON pero hay contenido, intentar parsearlo de todas formas
        try {
          return JSON.parse(responseBody);
        } catch (e) {
          throw new Error(`Respuesta no es JSON válido. Content-Type: ${contentType}. Respuesta: ${responseBody.substring(0, 100)}...`);
        }
      }
      
      return null;
    } catch (err) {
      console.error(`Error en fetchJson para ${url}:`, err);
      throw err;
    }
  };

  const loadReportes = async () => {
    try {
      setLoading(true);
      setError('');

      const params = new URLSearchParams({
        fechaInicio: fechas.inicio,
        fechaFin: fechas.fin
      });

      // Cargar reportes con manejo individual de errores
      const results = await Promise.allSettled([
        fetchJson(`${API_BASE}/documentos/resumen`).catch(e => { console.error('Error en documentos/resumen:', e); throw e; }),
        fetchJson(`${API_BASE}/documentos/evolucion?${params.toString()}`).catch(e => { console.error('Error en documentos/evolucion:', e); throw e; }),
        fetchJson(`${API_BASE}/documentos/especialidad`).catch(e => { console.error('Error en documentos/especialidad:', e); throw e; }),
        fetchJson(`${API_BASE}/documentos/formato`).catch(e => { console.error('Error en documentos/formato:', e); throw e; }),
        fetchJson(`${API_BASE}/documentos/tenant`).catch(e => { console.error('Error en documentos/tenant:', e); throw e; }),
        fetchJson(`${API_BASE}/usuarios/resumen`).catch(e => { console.error('Error en usuarios/resumen:', e); throw e; }),
        fetchJson(`${API_BASE}/usuarios/profesionales`).catch(e => { console.error('Error en usuarios/profesionales:', e); throw e; }),
        // Reportes de políticas (opcionales, pueden fallar si el servicio no está disponible)
        fetchJson(`${API_BASE}/politicas/resumen`).catch(e => { console.error('Error en politicas/resumen:', e); throw e; }),
        fetchJson(`${API_BASE}/politicas/alcance`).catch(e => { console.error('Error en politicas/alcance:', e); throw e; }),
        fetchJson(`${API_BASE}/politicas/duracion`).catch(e => { console.error('Error en politicas/duracion:', e); throw e; }),
        fetchJson(`${API_BASE}/politicas/evolucion?${params.toString()}`).catch(e => { console.error('Error en politicas/evolucion:', e); throw e; })
      ]);

      // Procesar resultados
      const [docsResumen, docsEvolucion, docsEspecialidad, docsFormato, docsTenant, usuariosResumen, usuariosProfesionales, politicasResumen, politicasAlcance, politicasDuracion, politicasEvolucion] = results.map((result, index) => {
        if (result.status === 'fulfilled') {
          const value = result.value;
          // Verificar que los valores sean del tipo correcto
          if (index === 0 || index === 5 || index === 7) {
            // Resúmenes (objetos o null)
            // Si el valor tiene una propiedad 'error', es un error y retornamos null
            if (value && typeof value === 'object' && !Array.isArray(value) && value.error) {
              console.warn(`Reporte ${index} contiene error:`, value.error);
              return null;
            }
            return (value && typeof value === 'object' && !Array.isArray(value)) ? value : null;
          } else {
            // Listas (arrays)
            // Si el valor tiene una propiedad 'error', es un error y retornamos array vacío
            if (value && typeof value === 'object' && value.error) {
              console.warn(`Reporte ${index} contiene error:`, value.error);
              return [];
            }
            return Array.isArray(value) ? value : [];
          }
        } else {
          console.error(`Error cargando reporte ${index}:`, result.reason);
          // Retornar valores por defecto según el tipo
          if (index === 0 || index === 5 || index === 7) return null; // resúmenes
          return []; // listas
        }
      });

      setResumenDocs(docsResumen);
      setEvolucion(docsEvolucion || []);
      setPorEspecialidad(docsEspecialidad || []);
      setPorFormato(docsFormato || []);
      setPorTenant(docsTenant || []);
      setResumenUsuarios(usuariosResumen);
      setProfesionales(usuariosProfesionales || []);
      setResumenPoliticas(politicasResumen);
      setPoliticasPorAlcance(politicasAlcance || []);
      setPoliticasPorDuracion(politicasDuracion || []);
      setEvolucionPoliticas(politicasEvolucion || []);

      // Verificar si hay errores críticos
      const errores = results.filter(r => r.status === 'rejected');
      if (errores.length > 0) {
        const mensajesError = errores.map(e => e.reason?.message || 'Error desconocido').join('; ');
        setError(`Algunos reportes no pudieron cargarse: ${mensajesError}`);
      }
    } catch (err) {
      console.error('Error cargando reportes:', err);
      setError(err.message || 'Error al obtener los reportes. Verifique que el backend esté corriendo y que tenga permisos de administrador.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReportes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fechas.inicio, fechas.fin]);

  if (loading) {
    return (
      <div className="bradcam_area" style={{
        paddingTop: '120px',
        paddingBottom: '80px',
        background: 'linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%)',
        position: 'relative',
        overflow: 'hidden',
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        <div className="text-center">
          <div className="spinner-border text-light" role="status" style={{ width: '3rem', height: '3rem' }}>
            <span className="sr-only">Cargando...</span>
          </div>
          <p className="mt-3 text-white">Generando reportes...</p>
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
                  fontSize: '46px',
                  fontWeight: '700',
                  marginBottom: '15px'
                }}>
                  Panel de Reportes
                </h3>
                <p style={{ color: 'var(--text-light)', fontSize: '18px' }}>
                  Indicadores clave del RNDC y usuarios administrativos
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container" style={{ paddingTop: '70px', paddingBottom: '60px' }}>
        {error && (
          <div className="alert alert-warning" role="alert" style={{ 
            borderRadius: '10px',
            backgroundColor: '#fef3c7',
            border: '1px solid #fbbf24',
            color: '#92400e'
          }}>
            <i className="fa fa-exclamation-triangle" style={{ marginRight: '8px' }}></i>
            {error}
          </div>
        )}

        <div className="card mb-4" style={{ borderRadius: '16px', boxShadow: '0 20px 45px rgba(15,23,42,0.08)' }}>
          <div className="card-body">
            <div className="row">
              <div className="col-md-6 mb-3">
                <label className="form-label" style={{ fontWeight: '600', color: 'var(--heading-color)' }}>
                  Fecha inicio
                </label>
                <input
                  type="date"
                  className="form-control"
                  value={fechas.inicio}
                  max={fechas.fin}
                  onChange={(e) => setFechas({ ...fechas, inicio: e.target.value })}
                />
              </div>
              <div className="col-md-6 mb-3">
                <label className="form-label" style={{ fontWeight: '600', color: 'var(--heading-color)' }}>
                  Fecha fin
                </label>
                <input
                  type="date"
                  className="form-control"
                  value={fechas.fin}
                  min={fechas.inicio}
                  max={formatDate(new Date())}
                  onChange={(e) => setFechas({ ...fechas, fin: e.target.value })}
                />
              </div>
            </div>
          </div>
        </div>

        {/* Sección: RNDC - Documentos */}
        <div className="mb-5">
          <h4 style={{
            color: '#1f2937',
            fontSize: '24px',
            fontWeight: '700',
            marginBottom: '25px',
            paddingBottom: '15px',
            borderBottom: '3px solid #3b82f6'
          }}>
            <i className="fa fa-file-medical" style={{ marginRight: '10px', color: '#3b82f6' }}></i>
            RNDC - Documentos Clínicos
          </h4>
          
          <div className="row">
            <div className="col-md-4 mb-4">
              <div className="card" style={{
                borderRadius: '16px',
                border: 'none',
                background: 'linear-gradient(135deg, #1e3a8a, #3b82f6)',
                color: '#fff',
                boxShadow: '0 15px 30px rgba(30,58,138,0.35)'
              }}>
                <div className="card-body">
                  <p className="text-uppercase mb-1" style={{ letterSpacing: '1px', opacity: 0.8, fontSize: '12px' }}>Total Documentos</p>
                  <h2 style={{ fontWeight: '700', fontSize: '32px' }}>{resumenDocs ? numberFormatter.format(resumenDocs.total || 0) : '-'}</h2>
                  <small style={{ opacity: 0.9 }}>Registrados en el RNDC</small>
                </div>
              </div>
            </div>
            <div className="col-md-4 mb-4">
              <div className="card" style={{
                borderRadius: '16px',
                border: 'none',
                background: '#dbeafe',
                color: '#1d4ed8',
                boxShadow: '0 12px 24px rgba(59,130,246,0.25)'
              }}>
                <div className="card-body">
                  <p className="text-uppercase mb-1" style={{ letterSpacing: '1px', opacity: 0.8, fontSize: '12px' }}>Breaking the Glass</p>
                  <h2 style={{ fontWeight: '700', fontSize: '32px' }}>{resumenDocs ? numberFormatter.format(resumenDocs.breakingTheGlass || 0) : '-'}</h2>
                  <small style={{ opacity: 0.8 }}>Accesos excepcionales</small>
                </div>
              </div>
            </div>
            <div className="col-md-4 mb-4">
              <div className="card" style={{
                borderRadius: '16px',
                border: 'none',
                background: '#dcfce7',
                color: '#15803d',
                boxShadow: '0 12px 24px rgba(34,197,94,0.25)'
              }}>
                <div className="card-body">
                  <p className="text-uppercase mb-1" style={{ letterSpacing: '1px', opacity: 0.8, fontSize: '12px' }}>Accesos Normales</p>
                  <h2 style={{ fontWeight: '700', fontSize: '32px' }}>{resumenDocs ? numberFormatter.format(resumenDocs.noBreakingTheGlass || 0) : '-'}</h2>
                  <small style={{ opacity: 0.8 }}>Accesos autorizados</small>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Sección: Políticas */}
        {resumenPoliticas && (
          <div className="mb-5">
            <h4 style={{
              color: '#1f2937',
              fontSize: '24px',
              fontWeight: '700',
              marginBottom: '25px',
              paddingBottom: '15px',
              borderBottom: '3px solid #8b5cf6'
            }}>
              <i className="fa fa-shield-alt" style={{ marginRight: '10px', color: '#8b5cf6' }}></i>
              Políticas de Acceso
            </h4>
            
            <div className="row">
              <div className="col-md-4 mb-4">
                <div className="card" style={{
                  borderRadius: '16px',
                  border: 'none',
                  background: 'linear-gradient(135deg, #6d28d9, #8b5cf6)',
                  color: '#fff',
                  boxShadow: '0 15px 30px rgba(139,92,246,0.35)'
                }}>
                  <div className="card-body">
                    <p className="text-uppercase mb-1" style={{ letterSpacing: '1px', opacity: 0.8, fontSize: '12px' }}>Total Políticas</p>
                    <h2 style={{ fontWeight: '700', fontSize: '32px' }}>{resumenPoliticas ? numberFormatter.format(resumenPoliticas.total || 0) : '-'}</h2>
                    <small style={{ opacity: 0.9 }}>Políticas activas</small>
                  </div>
                </div>
              </div>
              <div className="col-md-4 mb-4">
                <div className="card" style={{
                  borderRadius: '16px',
                  border: 'none',
                  background: '#ede9fe',
                  color: '#6d28d9',
                  boxShadow: '0 12px 24px rgba(139,92,246,0.25)'
                }}>
                  <div className="card-body">
                    <p className="text-uppercase mb-1" style={{ letterSpacing: '1px', opacity: 0.8, fontSize: '12px' }}>Indefinidas</p>
                    <h2 style={{ fontWeight: '700', fontSize: '32px' }}>{resumenPoliticas ? numberFormatter.format(resumenPoliticas.indefinidas || 0) : '-'}</h2>
                    <small style={{ opacity: 0.8 }}>Sin fecha de vencimiento</small>
                  </div>
                </div>
              </div>
              <div className="col-md-4 mb-4">
                <div className="card" style={{
                  borderRadius: '16px',
                  border: 'none',
                  background: '#f3e8ff',
                  color: '#7c3aed',
                  boxShadow: '0 12px 24px rgba(167,139,250,0.25)'
                }}>
                  <div className="card-body">
                    <p className="text-uppercase mb-1" style={{ letterSpacing: '1px', opacity: 0.8, fontSize: '12px' }}>Temporales</p>
                    <h2 style={{ fontWeight: '700', fontSize: '32px' }}>{resumenPoliticas ? numberFormatter.format(resumenPoliticas.temporales || 0) : '-'}</h2>
                    <small style={{ opacity: 0.8 }}>Con fecha de vencimiento</small>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-4">
              <div className="col-lg-6 mb-4">
                <div className="card h-100" style={{ borderRadius: '16px', boxShadow: '0 18px 40px rgba(15,23,42,0.07)' }}>
                  <div className="card-body">
                    <h5 className="card-title mb-3" style={{ color: '#1f2937', fontWeight: '600' }}>
                      <i className="fa fa-chart-pie" style={{ marginRight: '8px', color: '#8b5cf6' }}></i>
                      Por Alcance
                    </h5>
                    {politicasPorAlcance.length === 0 ? (
                      <div className="text-center text-muted py-4">
                        <i className="fa fa-info-circle" style={{ fontSize: '24px', marginBottom: '10px', opacity: 0.5 }}></i>
                        <div>Sin datos disponibles.</div>
                      </div>
                    ) : (
                      <ul className="list-group list-group-flush">
                        {politicasPorAlcance.map((item, index) => (
                          <li key={index} className="list-group-item d-flex justify-content-between align-items-center" style={{ border: 'none', padding: '12px 0' }}>
                            <span style={{ fontSize: '14px' }}>{item.alcance || 'Sin alcance'}</span>
                            <span className="badge" style={{ backgroundColor: '#8b5cf6', fontSize: '12px' }}>{numberFormatter.format(item.total)}</span>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                </div>
              </div>

              <div className="col-lg-6 mb-4">
                <div className="card h-100" style={{ borderRadius: '16px', boxShadow: '0 18px 40px rgba(15,23,42,0.07)' }}>
                  <div className="card-body">
                    <h5 className="card-title mb-3" style={{ color: '#1f2937', fontWeight: '600' }}>
                      <i className="fa fa-clock" style={{ marginRight: '8px', color: '#8b5cf6' }}></i>
                      Por Duración
                    </h5>
                    {politicasPorDuracion.length === 0 ? (
                      <div className="text-center text-muted py-4">
                        <i className="fa fa-info-circle" style={{ fontSize: '24px', marginBottom: '10px', opacity: 0.5 }}></i>
                        <div>Sin datos disponibles.</div>
                      </div>
                    ) : (
                      <ul className="list-group list-group-flush">
                        {politicasPorDuracion.map((item, index) => (
                          <li key={index} className="list-group-item d-flex justify-content-between align-items-center" style={{ border: 'none', padding: '12px 0' }}>
                            <span style={{ fontSize: '14px' }}>{item.duracion || 'Sin duración'}</span>
                            <span className="badge" style={{ backgroundColor: '#8b5cf6', fontSize: '12px' }}>{numberFormatter.format(item.total)}</span>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Sección: Usuarios */}
        <div className="mb-5">
          <h4 style={{
            color: '#1f2937',
            fontSize: '24px',
            fontWeight: '700',
            marginBottom: '25px',
            paddingBottom: '15px',
            borderBottom: '3px solid #10b981'
          }}>
            <i className="fa fa-users" style={{ marginRight: '10px', color: '#10b981' }}></i>
            Usuarios
          </h4>
          
          <div className="row">
            <div className="col-md-4 mb-4">
              <div className="card" style={{
                borderRadius: '16px',
                border: 'none',
                background: '#dcfce7',
                color: '#15803d',
                boxShadow: '0 12px 24px rgba(34,197,94,0.25)'
              }}>
                <div className="card-body">
                  <p className="text-uppercase mb-1" style={{ letterSpacing: '1px', opacity: 0.8, fontSize: '12px' }}>Perfiles Completos</p>
                  <h2 style={{ fontWeight: '700', fontSize: '32px' }}>{resumenUsuarios ? numberFormatter.format(resumenUsuarios.perfilesCompletos || 0) : '-'}</h2>
                  <small style={{ opacity: 0.8 }}>Usuarios con datos confirmados</small>
                </div>
              </div>
            </div>
            <div className="col-md-4 mb-4">
              <div className="card" style={{
                borderRadius: '16px',
                border: 'none',
                background: '#fee2e2',
                color: '#b91c1c',
                boxShadow: '0 12px 24px rgba(239,68,68,0.25)'
              }}>
                <div className="card-body">
                  <p className="text-uppercase mb-1" style={{ letterSpacing: '1px', opacity: 0.8, fontSize: '12px' }}>Administradores</p>
                  <h2 style={{ fontWeight: '700', fontSize: '32px' }}>{resumenUsuarios ? numberFormatter.format(resumenUsuarios.administradores || 0) : '-'}</h2>
                  <small style={{ opacity: 0.8 }}>Usuarios con permisos elevados</small>
                </div>
              </div>
            </div>
            <div className="col-md-4 mb-4">
              <div className="card" style={{
                borderRadius: '16px',
                border: 'none',
                background: '#e0e7ff',
                color: '#4338ca',
                boxShadow: '0 12px 24px rgba(99,102,241,0.25)'
              }}>
                <div className="card-body">
                  <p className="text-uppercase mb-1" style={{ letterSpacing: '1px', opacity: 0.8, fontSize: '12px' }}>Total Usuarios</p>
                  <h2 style={{ fontWeight: '700', fontSize: '32px' }}>{resumenUsuarios ? numberFormatter.format(resumenUsuarios.totalUsuarios || 0) : '-'}</h2>
                  <small style={{ opacity: 0.8 }}>Usuarios registrados</small>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Sección: RNDC - Análisis Detallado */}
        <div className="mb-5">
          <h5 style={{
            color: '#1f2937',
            fontSize: '20px',
            fontWeight: '600',
            marginBottom: '20px',
            paddingLeft: '10px',
            borderLeft: '4px solid #3b82f6'
          }}>
            Análisis Detallado de Documentos
          </h5>
          
          <div className="row">
            <div className="col-lg-6 mb-4">
              <div className="card h-100" style={{ borderRadius: '16px', boxShadow: '0 18px 40px rgba(15,23,42,0.07)' }}>
                <div className="card-body">
                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <h5 className="card-title mb-0" style={{ color: '#1f2937', fontWeight: '600' }}>
                      <i className="fa fa-chart-line" style={{ marginRight: '8px', color: '#3b82f6' }}></i>
                      Evolución de Documentos
                    </h5>
                    <small className="text-muted">Período seleccionado</small>
                  </div>
                  {evolucion.length === 0 ? (
                    <div className="text-center text-muted py-4">
                      <i className="fa fa-info-circle" style={{ fontSize: '24px', marginBottom: '10px', opacity: 0.5 }}></i>
                      <div>No hay datos para el rango seleccionado.</div>
                    </div>
                  ) : (
                    <div className="table-responsive">
                      <table className="table table-sm table-hover">
                        <thead style={{ backgroundColor: '#f8fafc' }}>
                          <tr>
                            <th style={{ fontWeight: '600' }}>Fecha</th>
                            <th className="text-end" style={{ fontWeight: '600' }}>Documentos</th>
                          </tr>
                        </thead>
                        <tbody>
                          {evolucion.map((item) => (
                            <tr key={item.fecha}>
                              <td>{item.fecha}</td>
                              <td className="text-end">
                                <span className="badge bg-primary">{numberFormatter.format(item.total)}</span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="col-lg-6 mb-4">
              <div className="card h-100" style={{ borderRadius: '16px', boxShadow: '0 18px 40px rgba(15,23,42,0.07)' }}>
                <div className="card-body">
                  <h5 className="card-title mb-3" style={{ color: '#1f2937', fontWeight: '600' }}>
                    <i className="fa fa-tags" style={{ marginRight: '8px', color: '#3b82f6' }}></i>
                    Por Tipo de Documento
                  </h5>
                  {porEspecialidad.length === 0 ? (
                    <div className="text-center text-muted py-4">
                      <i className="fa fa-info-circle" style={{ fontSize: '24px', marginBottom: '10px', opacity: 0.5 }}></i>
                      <div>Sin datos disponibles.</div>
                    </div>
                  ) : (
                    <ul className="list-group list-group-flush">
                      {porEspecialidad.slice(0, 10).map((item, index) => (
                        <li key={index} className="list-group-item d-flex justify-content-between align-items-center" style={{ border: 'none', padding: '12px 0' }}>
                          <span style={{ fontSize: '14px' }}>{item.especialidad || 'Sin tipo'}</span>
                          <span className="badge bg-primary rounded-pill" style={{ fontSize: '12px' }}>{numberFormatter.format(item.total)}</span>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>
            </div>
          </div>

          <div className="row">
            <div className="col-lg-6 mb-4">
              <div className="card h-100" style={{ borderRadius: '16px', boxShadow: '0 18px 40px rgba(15,23,42,0.07)' }}>
                <div className="card-body">
                  <h5 className="card-title mb-3" style={{ color: '#1f2937', fontWeight: '600' }}>
                    <i className="fa fa-file" style={{ marginRight: '8px', color: '#3b82f6' }}></i>
                    Por Formato
                  </h5>
                  {porFormato.length === 0 ? (
                    <div className="text-center text-muted py-4">
                      <i className="fa fa-info-circle" style={{ fontSize: '24px', marginBottom: '10px', opacity: 0.5 }}></i>
                      <div>Sin datos disponibles.</div>
                    </div>
                  ) : (
                    <ul className="list-group list-group-flush">
                      {porFormato.map((item, index) => (
                        <li key={index} className="list-group-item d-flex justify-content-between align-items-center" style={{ border: 'none', padding: '12px 0' }}>
                          <span style={{ fontSize: '14px' }}>{item.formato || 'Desconocido'}</span>
                          <span className="badge bg-secondary rounded-pill" style={{ fontSize: '12px' }}>{numberFormatter.format(item.total)}</span>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>
            </div>

            <div className="col-lg-6 mb-4">
              <div className="card h-100" style={{ borderRadius: '16px', boxShadow: '0 18px 40px rgba(15,23,42,0.07)' }}>
                <div className="card-body">
                  <h5 className="card-title mb-3" style={{ color: '#1f2937', fontWeight: '600' }}>
                    <i className="fa fa-hospital" style={{ marginRight: '8px', color: '#3b82f6' }}></i>
                    Por Clínica (Tenant)
                  </h5>
                  {porTenant.length === 0 ? (
                    <div className="text-center text-muted py-4">
                      <i className="fa fa-info-circle" style={{ fontSize: '24px', marginBottom: '10px', opacity: 0.5 }}></i>
                      <div>Sin datos disponibles.</div>
                    </div>
                  ) : (
                    <ul className="list-group list-group-flush">
                      {porTenant.map((item, index) => (
                        <li key={index} className="list-group-item d-flex justify-content-between align-items-center" style={{ border: 'none', padding: '12px 0' }}>
                          <span style={{ fontSize: '14px' }}>Tenant {item.tenantId || 'N/A'}</span>
                          <span className="badge bg-success rounded-pill" style={{ fontSize: '12px' }}>{numberFormatter.format(item.total)}</span>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Sección: Usuarios - Detalle */}
        <div className="mb-5">
          <h5 style={{
            color: '#1f2937',
            fontSize: '20px',
            fontWeight: '600',
            marginBottom: '20px',
            paddingLeft: '10px',
            borderLeft: '4px solid #10b981'
          }}>
            Detalle de Usuarios
          </h5>
          
          <div className="row">
            <div className="col-lg-6 mb-4">
              <div className="card h-100" style={{ borderRadius: '16px', boxShadow: '0 18px 40px rgba(15,23,42,0.07)' }}>
                <div className="card-body">
                  <h5 className="card-title mb-3" style={{ color: '#1f2937', fontWeight: '600' }}>
                    <i className="fa fa-user-tag" style={{ marginRight: '8px', color: '#10b981' }}></i>
                    Usuarios por Rol
                  </h5>
                  {resumenUsuarios?.porRol ? (
                    <div className="table-responsive">
                      <table className="table table-sm table-hover">
                        <thead style={{ backgroundColor: '#f8fafc' }}>
                          <tr>
                            <th style={{ fontWeight: '600' }}>Rol</th>
                            <th className="text-end" style={{ fontWeight: '600' }}>Cantidad</th>
                          </tr>
                        </thead>
                        <tbody>
                          {Object.entries(resumenUsuarios.porRol).map(([rol, total]) => (
                            <tr key={rol}>
                              <td>
                                <span className="badge" style={{
                                  backgroundColor: rol === 'AD' ? '#fee2e2' : rol === 'US' ? '#dbeafe' : '#e0e7ff',
                                  color: rol === 'AD' ? '#b91c1c' : rol === 'US' ? '#1d4ed8' : '#4338ca',
                                  padding: '6px 12px',
                                  borderRadius: '6px',
                                  fontWeight: '600'
                                }}>
                                  {rol === 'AD' ? 'Administrador' : rol === 'US' ? 'Usuario de la Salud' : rol}
                                </span>
                              </td>
                              <td className="text-end">
                                <span className="badge bg-primary">{numberFormatter.format(total)}</span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    <div className="text-center text-muted py-4">
                      <i className="fa fa-info-circle" style={{ fontSize: '24px', marginBottom: '10px', opacity: 0.5 }}></i>
                      <div>Sin información disponible.</div>
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="col-lg-6 mb-4">
              <div className="card h-100" style={{ borderRadius: '16px', boxShadow: '0 18px 40px rgba(15,23,42,0.07)' }}>
                <div className="card-body">
                  <h5 className="card-title mb-3" style={{ color: '#1f2937', fontWeight: '600' }}>
                    <i className="fa fa-user-md" style={{ marginRight: '8px', color: '#10b981' }}></i>
                    Profesionales Registrados
                  </h5>
                  <div className="mb-3">
                    <span className="badge bg-primary rounded-pill" style={{ fontSize: '14px', padding: '8px 16px' }}>
                      {numberFormatter.format(profesionales.length)} profesionales
                    </span>
                  </div>
                  {profesionales.length === 0 ? (
                    <div className="text-center text-muted py-4">
                      <i className="fa fa-info-circle" style={{ fontSize: '24px', marginBottom: '10px', opacity: 0.5 }}></i>
                      <div>No se encontraron profesionales registrados.</div>
                    </div>
                  ) : (
                    <div className="table-responsive" style={{ maxHeight: '400px', overflowY: 'auto' }}>
                      <table className="table table-sm table-hover">
                        <thead style={{ backgroundColor: '#f8fafc', position: 'sticky', top: 0, zIndex: 10 }}>
                          <tr>
                            <th style={{ fontWeight: '600', fontSize: '13px' }}>Nombre</th>
                            <th style={{ fontWeight: '600', fontSize: '13px' }}>Email</th>
                            <th style={{ fontWeight: '600', fontSize: '13px' }}>Perfil</th>
                          </tr>
                        </thead>
                        <tbody>
                          {profesionales.slice(0, 10).map((prof) => (
                            <tr key={prof.id}>
                              <td style={{ fontSize: '13px' }}>{prof.nombreCompleto || '-'}</td>
                              <td style={{ fontSize: '13px' }}>{prof.email || '-'}</td>
                              <td>
                                <span className={`badge ${prof.perfilCompleto ? 'bg-success' : 'bg-warning text-dark'}`} style={{ fontSize: '11px' }}>
                                  {prof.perfilCompleto ? 'Completo' : 'Pendiente'}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                      {profesionales.length > 10 && (
                        <div className="text-center mt-2">
                          <small className="text-muted">Mostrando 10 de {profesionales.length} profesionales</small>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default ReportesAdmin;



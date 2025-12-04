import React, { useEffect, useState, useCallback } from 'react';
import config from '../config';
import GenericPopup from './GenericPopup';

const Home = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isMinor, setIsMinor] = useState(false);
  const [popup, setPopup] = useState({ show: false, message: '', type: 'error' });

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

  useEffect(() => {
    // Verificar si hay token temporal en la URL para intercambiar
    const urlParams = new URLSearchParams(window.location.search);
    const loginStatus = urlParams.get('login');
    const tempToken = urlParams.get('token');
    const error = urlParams.get('error');
    
    console.log(' [DEBUG] Home.js useEffect ejecutado');
    console.log(' [DEBUG] URL completa:', window.location.href);
    console.log(' [DEBUG] loginStatus:', loginStatus);
    console.log(' [DEBUG] tempToken:', tempToken ? 'PRESENTE' : 'NO PRESENTE');
    console.log(' [DEBUG] tempToken valor:', tempToken);
    
    // Verificar si ya se procesó el token (evitar llamadas duplicadas)
    const tokenProcessed = sessionStorage.getItem('token_exchange_processed');
    
    if (error === 'menor_de_edad') {
      console.warn('⛔ Usuario identificado como menor de edad');
      setIsMinor(true);
      setLoading(false);
      window.history.replaceState({}, document.title, window.location.pathname);
      return;
    }

    if (loginStatus === 'success' && tempToken && !tokenProcessed) {
      console.log('Login exitoso! Intercambiando token temporal...');
      sessionStorage.setItem('token_exchange_processed', 'true');
      exchangeTokenAndSetCookie(tempToken);
    } else if (loginStatus === 'success' && tempToken && tokenProcessed) {
      console.log('Token ya fue procesado, limpiando URL...');
      window.history.replaceState({}, document.title, window.location.pathname);
      checkSession();
    } else {
        checkSession();
    }
  }, [checkSession]);
  
  const exchangeTokenAndSetCookie = async (tempToken) => {
    // Validar que el token no esté vacío
    if (!tempToken || tempToken.trim() === '') {
      console.error('Token temporal vacío o inválido');
      sessionStorage.removeItem('token_exchange_processed');
      window.history.replaceState({}, document.title, window.location.pathname);
      checkSession();
      return;
    }

    try {
      console.log('🔄 Intercambiando token temporal:', tempToken.substring(0, 10) + '...');
      // Intercambiar token temporal por JWT real
      const response = await fetch(`${config.BACKEND_URL}/api/auth/exchange-token`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ token: tempToken })
      });
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ error: 'Error desconocido' }));
        // Si el token ya fue usado o expiró, limpiar el flag para permitir reintento
        if (response.status === 400 || response.status === 401) {
          sessionStorage.removeItem('token_exchange_processed');
        }
        // El exchange es opcional - la cookie ya está seteada por el callback
        // Si falla, simplemente verificar sesión (la cookie ya está)
        console.warn('Exchange de token falló, pero la cookie JWT ya está seteada por el callback');
        console.warn('Continuando con verificación de sesión...');
        window.history.replaceState({}, document.title, window.location.pathname);
        checkSession();
        return; // No lanzar error, solo continuar
      }
      
      const data = await response.json();
      // El backend ya setea la cookie cross-site, no necesitamos hacerlo aquí
      // El JWT se puede recibir pero no se usa para setear cookie propia
      
      console.log('Token recibido del backend - Cookie establecida por el backend (cross-site)');
      
      // Limpiar URL inmediatamente (remover token de la barra de direcciones)
      window.history.replaceState({}, document.title, window.location.pathname);
      
      // Limpiar el flag de procesamiento
      sessionStorage.removeItem('token_exchange_processed');
      
      // Verificar sesión
      checkSession();
      
    } catch (error) {
      console.error('Error intercambiando token:', error);
      // No mostrar popup si el token ya fue procesado (evitar spam)
      if (!sessionStorage.getItem('token_exchange_processed')) {
        setPopup({ show: true, message: 'Error al completar el login: ' + error.message, type: 'error' });
      }
      window.history.replaceState({}, document.title, window.location.pathname);
      setLoading(false);
    }
  };

  const handleGubUyLogin = () => {
    const authUrl = new URL('https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize');
    authUrl.searchParams.set('client_id', '890192');
    authUrl.searchParams.set('response_type', 'code');
    authUrl.searchParams.set('scope', 'openid personal_info email');
    authUrl.searchParams.set('redirect_uri', config.CALLBACK_URL);
    authUrl.searchParams.set('state', Math.random().toString(36).substring(2, 15));
    
    window.location.href = authUrl.toString();
  };

  if (isMinor) {
    return (
      <div className="slider_area" style={{minHeight: '100vh', display: 'flex', alignItems: 'center', backgroundColor: '#f2f3f7'}}>
        <div className="container">
          <div className="row justify-content-center">
            <div className="col-xl-8 col-lg-8">
              <div className="welcome_hcen_info text-center" style={{backgroundColor: 'white', padding: '50px', borderRadius: '10px', boxShadow: '0 5px 15px rgba(0,0,0,0.1)'}}>
                <div style={{marginBottom: '30px'}}>
                  <i className="flaticon-warning" style={{fontSize: '60px', color: '#ff4b4b'}}></i>
                </div>
                <h3 style={{color: '#1f2b7b', marginBottom: '20px'}}>Acceso Restringido</h3>
                <p style={{fontSize: '18px', color: '#666', marginBottom: '30px'}}>
                  Lo sentimos, el acceso a la Historia Clínica Electrónica Nacional no está permitido para menores de 18 años.
                </p>
                <div className="alert alert-info" role="alert" style={{textAlign: 'left', marginBottom: '30px'}}>
                  <h5 className="alert-heading"><i className="fa fa-info-circle"></i> Información Importante</h5>
                  <p className="mb-0">
                    Si usted considera que esto es un error, por favor verifique sus datos en la Dirección Nacional de Identificación Civil (DNIC).
                  </p>
                </div>
                <a href="/" className="boxed-btn5" onClick={(e) => { e.preventDefault(); setIsMinor(false); }}>Volver al Inicio</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

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

  return (
    <div style={{minHeight: '100vh', backgroundColor: '#f8fafc'}}>
      {/* Hero Section */}
      <div className="slider_area" style={{minHeight: '70vh'}}>
        <div className="slider_active">
          <div className="single_slider d-flex align-items-center slider_bg_1 overlay" style={{
            minHeight: '70vh',
            background: 'linear-gradient(135deg, #1f2b7b 0%, #3b82f6 50%, #06b6d4 100%)',
            position: 'relative',
            overflow: 'hidden'
          }}>
            <div style={{
              position: 'absolute',
              top: '0',
              left: '0',
              right: '0',
              bottom: '0',
              background: 'url("/assets/img/banner/banner.png") center/cover',
              opacity: '0.2',
              zIndex: '1'
            }}></div>
            <div className="container" style={{position: 'relative', zIndex: '2'}}>
              <div className="row">
                <div className="col-xl-12">
                  <div className="slider_text text-center">
                    <span style={{
                      color: '#e2e8f0',
                      fontSize: '20px',
                      fontWeight: '500',
                      textTransform: 'uppercase',
                      letterSpacing: '2px',
                      marginBottom: '20px',
                      display: 'block'
                    }}>
                      HCEN
                    </span>
                    <h3 style={{
                      fontSize: '56px',
                      fontWeight: '800',
                      color: '#ffffff',
                      marginBottom: '25px',
                      textShadow: '0 4px 8px rgba(0,0,0,0.3)',
                      lineHeight: '1.2'
                    }}>
                      <span style={{color: '#fbbf24'}}>Historia Clínica</span> <br />
                      Electrónica Nacional
                    </h3>
                    <p style={{
                      color: '#e2e8f0', 
                      marginTop: '20px', 
                      marginBottom: '50px',
                      fontSize: '18px',
                      fontWeight: '400',
                      maxWidth: '600px',
                      margin: '20px auto 50px',
                      lineHeight: '1.6'
                    }}>
                      Accede de forma segura a tu información médica y mantén un control completo de tu salud
                    </p>
                    {user ? (
                      <a href="/historia-clinica" style={{
                        padding: '18px 40px',
                        fontSize: '16px',
                        fontWeight: '600',
                        borderRadius: '50px',
                        textTransform: 'uppercase',
                        letterSpacing: '1px',
                        boxShadow: '0 8px 25px rgba(0,0,0,0.2)',
                        transition: 'all 0.3s ease',
                        backgroundColor: '#ffffff',
                        color: '#1f2b7b',
                        textDecoration: 'none',
                        display: 'inline-block',
                        border: '2px solid #1f2b7b',
                        cursor: 'pointer'
                      }}
                      onMouseEnter={(e) => {
                        e.target.style.backgroundColor = '#f8fafc';
                        e.target.style.transform = 'translateY(-2px)';
                      }}
                      onMouseLeave={(e) => {
                        e.target.style.backgroundColor = '#ffffff';
                        e.target.style.transform = 'translateY(0)';
                      }}>
                        <i className="flaticon-medical" style={{marginRight: '10px'}}></i>
                        Ver Mi Historia Clínica
                      </a>
                    ) : (
                      <button
                        type="button"
                        onClick={handleGubUyLogin}
                        style={{
                          cursor: 'pointer',
                          padding: '18px 40px',
                          fontSize: '16px',
                          fontWeight: '600',
                          borderRadius: '50px',
                          textTransform: 'uppercase',
                          letterSpacing: '1px',
                          boxShadow: '0 8px 25px rgba(0,0,0,0.2)',
                          transition: 'all 0.3s ease',
                          backgroundColor: '#ffffff',
                          color: '#1f2b7b',
                          textDecoration: 'none',
                          display: 'inline-block',
                          border: '2px solid #1f2b7b'
                        }}
                        onMouseEnter={(e) => {
                          e.target.style.backgroundColor = '#f8fafc';
                          e.target.style.transform = 'translateY(-2px)';
                        }}
                        onMouseLeave={(e) => {
                          e.target.style.backgroundColor = '#ffffff';
                          e.target.style.transform = 'translateY(0)';
                        }}
                      >
                        <i className="flaticon-user" style={{marginRight: '10px'}}></i>
                        Iniciar Sesión con gub.uy
                      </button>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Información sobre HCEN */}
      <div style={{padding: '80px 0', backgroundColor: '#ffffff'}}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div style={{maxWidth: '900px', margin: '0 auto', textAlign: 'center'}}>
                <h2 style={{
                  fontSize: '42px',
                  fontWeight: '700',
                  color: '#1f2937',
                  marginBottom: '30px',
                  lineHeight: '1.3'
                }}>
                  ¿Qué es HCEN?
                </h2>
                <div style={{
                  fontSize: '18px',
                  color: '#4b5563',
                  lineHeight: '1.8',
                  textAlign: 'left',
                  marginBottom: '40px'
                }}>
                  <p style={{marginBottom: '20px'}}>
                    La <strong>Historia Clínica Electrónica Nacional (HCEN)</strong> es una plataforma que permite acceder a la Historia Clínica Digital de los usuarios del sistema de salud y que posibilita el registro de cualquier evento médico independientemente del lugar geográfico y prestador de salud en donde se dé la asistencia.
                  </p>
                  <p style={{marginBottom: '20px'}}>
                    Los usuarios del <strong>SNIS (Sistema Nacional Integrado de Salud)</strong> mayores de 18 años pueden acceder a su Historia Clínica Digital para:
                  </p>
                  <ul style={{
                    listStyle: 'none',
                    padding: '0',
                    margin: '20px 0',
                    textAlign: 'left'
                  }}>
                    <li style={{
                      padding: '12px 0',
                      paddingLeft: '30px',
                      position: 'relative',
                      fontSize: '17px',
                      color: '#374151'
                    }}>
                      <span style={{
                        position: 'absolute',
                        left: '0',
                        color: '#3b82f6',
                        fontSize: '20px',
                        fontWeight: 'bold'
                      }}>✓</span>
                      Ver todos sus eventos asistenciales registrados
                    </li>
                    <li style={{
                      padding: '12px 0',
                      paddingLeft: '30px',
                      position: 'relative',
                      fontSize: '17px',
                      color: '#374151'
                    }}>
                      <span style={{
                        position: 'absolute',
                        left: '0',
                        color: '#3b82f6',
                        fontSize: '20px',
                        fontWeight: 'bold'
                      }}>✓</span>
                      Poner a disposición su información clínica para los equipos de salud que lo asistan
                    </li>
                    <li style={{
                      padding: '12px 0',
                      paddingLeft: '30px',
                      position: 'relative',
                      fontSize: '17px',
                      color: '#374151'
                    }}>
                      <span style={{
                        position: 'absolute',
                        left: '0',
                        color: '#3b82f6',
                        fontSize: '20px',
                        fontWeight: 'bold'
                      }}>✓</span>
                      Acceder desde cualquier parte del país y en cualquier prestador de salud
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Información de Contacto */}
      <div style={{padding: '80px 0', backgroundColor: '#f8fafc'}}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div style={{maxWidth: '900px', margin: '0 auto'}}>
                <h2 style={{
                  fontSize: '42px',
                  fontWeight: '700',
                  color: '#1f2937',
                  marginBottom: '20px',
                  textAlign: 'center'
                }}>
                  Información de Contacto
                </h2>
                <p style={{
                  fontSize: '18px',
                  color: '#6b7280',
                  textAlign: 'center',
                  marginBottom: '50px'
                }}>
                  Agencia de Gobierno Electrónico y Sociedad de la Información y del Conocimiento
                </p>
                
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                  gap: '30px',
                  marginTop: '40px'
                }}>
                  <div style={{
                    backgroundColor: '#ffffff',
                    padding: '30px',
                    borderRadius: '12px',
                    boxShadow: '0 4px 6px rgba(0,0,0,0.07)',
                    textAlign: 'center',
                    transition: 'transform 0.3s ease, box-shadow 0.3s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-5px)';
                    e.currentTarget.style.boxShadow = '0 8px 15px rgba(0,0,0,0.1)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 4px 6px rgba(0,0,0,0.07)';
                  }}>
                    <div style={{
                      fontSize: '36px',
                      color: '#3b82f6',
                      marginBottom: '15px'
                    }}>
                      <i className="fa fa-map-marker"></i>
                    </div>
                    <h3 style={{
                      fontSize: '20px',
                      fontWeight: '600',
                      color: '#1f2937',
                      marginBottom: '15px'
                    }}>
                      Dirección
                    </h3>
                    <p style={{
                      fontSize: '16px',
                      color: '#6b7280',
                      lineHeight: '1.6',
                      margin: '0'
                    }}>
                      Liniers 1324 piso 4<br />
                      Montevideo, Uruguay
                    </p>
                  </div>

                  <div style={{
                    backgroundColor: '#ffffff',
                    padding: '30px',
                    borderRadius: '12px',
                    boxShadow: '0 4px 6px rgba(0,0,0,0.07)',
                    textAlign: 'center',
                    transition: 'transform 0.3s ease, box-shadow 0.3s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-5px)';
                    e.currentTarget.style.boxShadow = '0 8px 15px rgba(0,0,0,0.1)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 4px 6px rgba(0,0,0,0.07)';
                  }}>
                    <div style={{
                      fontSize: '36px',
                      color: '#3b82f6',
                      marginBottom: '15px'
                    }}>
                      <i className="fa fa-phone"></i>
                    </div>
                    <h3 style={{
                      fontSize: '20px',
                      fontWeight: '600',
                      color: '#1f2937',
                      marginBottom: '15px'
                    }}>
                      Teléfono
                    </h3>
                    <p style={{
                      fontSize: '16px',
                      color: '#6b7280',
                      lineHeight: '1.6',
                      margin: '0'
                    }}>
                      <a href="tel:+59829012929" style={{
                        color: '#3b82f6',
                        textDecoration: 'none',
                        fontWeight: '500'
                      }}>
                        (+598) 2901 2929
                      </a>
                    </p>
                  </div>

                  <div style={{
                    backgroundColor: '#ffffff',
                    padding: '30px',
                    borderRadius: '12px',
                    boxShadow: '0 4px 6px rgba(0,0,0,0.07)',
                    textAlign: 'center',
                    transition: 'transform 0.3s ease, box-shadow 0.3s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-5px)';
                    e.currentTarget.style.boxShadow = '0 8px 15px rgba(0,0,0,0.1)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 4px 6px rgba(0,0,0,0.07)';
                  }}>
                    <div style={{
                      fontSize: '36px',
                      color: '#3b82f6',
                      marginBottom: '15px'
                    }}>
                      <i className="fa fa-clock-o"></i>
                    </div>
                    <h3 style={{
                      fontSize: '20px',
                      fontWeight: '600',
                      color: '#1f2937',
                      marginBottom: '15px'
                    }}>
                      Horario de Atención
                    </h3>
                    <p style={{
                      fontSize: '16px',
                      color: '#6b7280',
                      lineHeight: '1.6',
                      margin: '0'
                    }}>
                      Lunes a viernes<br />
                      9:00 a 17:00 h
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div style={{
        padding: '40px 0',
        backgroundColor: '#1f2937',
        color: '#e5e7eb',
        textAlign: 'center'
      }}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <p style={{
                fontSize: '16px',
                margin: '0',
                color: '#9ca3af'
              }}>
                © {new Date().getFullYear()} Historia Clínica Electrónica Nacional (HCEN) - Agencia de Gobierno Electrónico y Sociedad de la Información y del Conocimiento
              </p>
            </div>
          </div>
        </div>
      </div>

      <GenericPopup
        show={popup.show}
        onClose={() => setPopup({ ...popup, show: false })}
        message={popup.message}
        type={popup.type}
      />
    </div>
  );
};

export default Home;
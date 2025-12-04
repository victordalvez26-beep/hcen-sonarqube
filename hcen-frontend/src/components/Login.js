import React, { useEffect, useState } from 'react';
import PerfilUsuario from './PerfilUsuario';
import GestionClinicas from './GestionClinicas';
import config from '../config';

const Login = () => {
  const [popup, setPopup] = useState({ show: false, message: '', type: 'error' });
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isMinor, setIsMinor] = useState(false);

  useEffect(() => {
    console.log(' [DEBUG] Login.js useEffect ejecutado');
    console.log(' [DEBUG] URL completa:', window.location.href);
    const urlParams = new URLSearchParams(window.location.search);
    const loginStatus = urlParams.get('login');
    const logoutStatus = urlParams.get('logout');
    const error = urlParams.get('error');
    const tempToken = urlParams.get('token');
    
    console.log(' [DEBUG] loginStatus:', loginStatus);
    console.log(' [DEBUG] tempToken:', tempToken ? 'PRESENTE' : 'NO PRESENTE');
    
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
    } else if (loginStatus === 'success') {
      console.log('Login exitoso! Verificando sesión...');
      window.history.replaceState({}, document.title, window.location.pathname);
      checkSession();
    } else if (logoutStatus === 'success') {
      console.log('Logout exitoso');
      sessionStorage.removeItem('token_exchange_processed');
      setUser(null);
      setLoading(false);
      window.history.replaceState({}, document.title, window.location.pathname);
    } else {
      checkSession();
    }
    
    if (error) {
      console.error('Error en autenticación:', error);
      sessionStorage.removeItem('token_exchange_processed');
      setPopup({ show: true, message: 'Error en la autenticación: ' + error, type: 'error' });
      window.history.replaceState({}, document.title, window.location.pathname);
      setLoading(false);
    }
  }, []);
  
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
      
      console.log('Token intercambiado - Cookie establecida por el backend (cross-site)');
      
      // Limpiar URL inmediatamente (remover token de la barra de direcciones)
      window.history.replaceState({}, document.title, window.location.pathname);
      
      // Limpiar el flag de procesamiento
      sessionStorage.removeItem('token_exchange_processed');
      
      // Verificar sesión
      checkSession();
      
    } catch (error) {
      console.error('Error intercambiando token:', error);
      // El exchange es opcional - la cookie ya está seteada por el callback
      // Si falla, simplemente verificar sesión (la cookie ya está)
      console.warn('Exchange de token falló, pero la cookie JWT ya está seteada por el callback');
      console.warn('Continuando con verificación de sesión...');
      window.history.replaceState({}, document.title, window.location.pathname);
      checkSession();
      // No mostrar alert - la cookie ya está y el login funcionará
    }
  };
  
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
        console.log('Sesión activa para:', data.nombre);
        
        // Obtener información completa del usuario incluyendo el rol
        try {
          const profileResponse = await fetch(`${config.BACKEND_URL}/api/users/profile`, {
            method: 'GET',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          if (profileResponse.ok) {
            const profileData = await profileResponse.json();
            setUser({...data, ...profileData});
          } else {
            setUser(data);
          }
        } catch (profileError) {
          console.error('Error obteniendo perfil del usuario:', profileError);
          setUser(data);
        }
      } else {
        console.log('No hay sesión activa');
        setUser(null);
      }
    } catch (error) {
      console.error('Error verificando sesión:', error);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const handleGubUyLogin = () => {
    const state = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    sessionStorage.setItem('oauth_state', state);
    
    const authUrl = new URL('https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize');
    authUrl.searchParams.set('response_type', 'code');
    authUrl.searchParams.set('client_id', '890192');
    authUrl.searchParams.set('redirect_uri', config.CALLBACK_URL);
    authUrl.searchParams.set('scope', 'openid personal_info email');
    authUrl.searchParams.set('state', state);
    
    window.location.href = authUrl.toString();
  };

  const handleLogout = () => {
    window.location.href = `${config.BACKEND_URL}/api/auth/logout`;
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
                <a href="/" className="boxed-btn5">Volver al Inicio</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="slider_area" style={{minHeight: '100vh', display: 'flex', alignItems: 'center', backgroundColor: 'var(--background-color)'}}>
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
    <>
      <header>
        <div className="header-area">
          <div id="sticky-header" className="main-header-area">
            <div className="container">
              <div className="row align-items-center">
                <div className="col-xl-3 col-lg-3">
                  <div className="logo-img">
                    <a href="/">
                      <img src="/assets/img/logo.png" alt="HCEN" style={{maxHeight: '60px'}} />
                    </a>
                  </div>
                </div>
                <div className="col-xl-9 col-lg-9">
                  <div className="menu_wrap d-none d-lg-block">
                    <div className="menu_wrap_inner d-flex align-items-center justify-content-end">
                      <div className="main-menu">
                        <nav>
                          <ul id="navigation">
                            <li><a href="/">Inicio</a></li>
                            <li><a href="/about">Acerca de</a></li>
                            <li><a href="/contact">Contacto</a></li>
                          </ul>
                        </nav>
                      </div>
                      {user && (
                        <div className="book_room">
                          <div className="book_btn">
                            <a onClick={handleLogout} style={{cursor: 'pointer'}}>Cerrar Sesión</a>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
                <div className="col-12">
                  <div className="mobile_menu d-block d-lg-none"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      {user ? (
        <div className="slider_area" style={{minHeight: '100vh'}}>
          <div className="single_slider d-flex align-items-center slider_bg_1 overlay" style={{minHeight: '100vh'}}>
            <div className="container">
              <div className="row">
                <div className="col-xl-12">
                  <div className="slider_text text-center">
                    <span>Bienvenido a HCEN</span>
                    <h3><span>{user.nombre || 'Usuario'}</span></h3>
                    
                    <div className="row justify-content-center mt-5">
                      <div className="col-xl-8">
                        <div className="welcome_hcen_info" style={{backgroundColor: 'rgba(255,255,255,0.95)', padding: '40px', borderRadius: '10px'}}>
                          <h4 style={{color: '#1f2b7b', marginBottom: '30px'}}>Información del Usuario</h4>
                          <ul style={{listStyle: 'none', padding: 0}}>
                            <li style={{marginBottom: '15px', color: '#333'}}> 
                              <i className="flaticon-verified" style={{color: '#1f2b7b'}}></i> 
                              <strong>UID:</strong> {user.uid || 'N/A'}
                            </li>
                            <li style={{marginBottom: '15px', color: '#333'}}> 
                              <i className="flaticon-verified" style={{color: '#1f2b7b'}}></i> 
                              <strong>Nombre Completo:</strong> {user.nombre || 'N/A'}
                            </li>
                            <li style={{marginBottom: '15px', color: '#333'}}> 
                              <i className="flaticon-verified" style={{color: '#1f2b7b'}}></i> 
                              <strong>Email:</strong> {user.email || 'N/A'}
                            </li>
                            <li style={{marginBottom: '15px', color: '#333'}}> 
                              <i className="flaticon-verified" style={{color: '#1f2b7b'}}></i> 
                              <strong>Documento:</strong> {user.documento || 'No disponible'}
                            </li>
                            <li style={{marginBottom: '15px', color: '#333'}}> 
                              <i className="flaticon-verified" style={{color: '#1f2b7b'}}></i> 
                              <strong>Rol:</strong> {user.rolDescripcion || 'Usuario de la Salud'}
                            </li>
                          </ul>
                          
                          <details style={{marginTop: '30px', textAlign: 'left'}}>
                            <summary style={{cursor: 'pointer', color: '#1f2b7b', fontWeight: 'bold'}}>
                              Ver información detallada
                            </summary>
                            <pre style={{backgroundColor: '#f5f5f5', padding: '15px', borderRadius: '5px', marginTop: '15px', maxHeight: '300px', overflow: 'auto', fontSize: '12px'}}>
                              {JSON.stringify(user, null, 2)}
                            </pre>
                          </details>
                        </div>
                      </div>
                    </div>
                    
                    {/* Contenido según rol del usuario */}
                    <div className="row justify-content-center mt-4">
                      <div className="col-xl-12">
                        {user.rol === 'AD' ? (
                          // Administrador HCEN - Gestión de Clínicas
                          <GestionClinicas />
                        ) : (
                          // Usuario de la Salud - Perfil de Usuario
                          <PerfilUsuario 
                            user={user} 
                            onUpdate={(data) => {
                              console.log('Perfil actualizado:', data);
                              // Aquí podrías actualizar el usuario en el backend
                            }}
                          />
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div className="slider_area">
          <div className="slider_active">
            <div className="single_slider d-flex align-items-center slider_bg_1 overlay" style={{minHeight: '100vh'}}>
              <div className="container">
                <div className="row">
                  <div className="col-xl-12">
                    <div className="slider_text text-center">
                      <span>HCEN</span>
                      <h3><span>Historia Clínica</span> <br />
                        Electrónica Nacional</h3>
                      <p style={{color: '#fff', marginTop: '20px', marginBottom: '40px'}}>
                        Accede de forma segura a tu información médica
                      </p>
                      <a onClick={handleGubUyLogin} className="boxed-btn5" style={{cursor: 'pointer'}}>
                        Iniciar Sesión con gub.uy
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="emergency_contact" style={{marginTop: '0'}}>
        <div className="conatiner-fluid p-0">
          <div className="row no-gutters">
            <div className="col-xl-4 col-lg-4">
              <div className="single_emergency d-flex align-items-center justify-content-center emergency_bg_1 overlay_skyblue">
                <div className="info">
                  <span>Emergencias:</span>
                  <h3>0800 1234</h3>
                </div>
                <div className="info_icon">
                  <i className="flaticon-call"></i>
                </div>
              </div>
            </div>
            <div className="col-xl-4 col-lg-4">
              <div className="single_emergency d-flex align-items-center justify-content-center emergency_bg_2 overlay_skyblue">
                <div className="info">
                  <span>Información:</span>
                  <h3>info@hcen.gub.uy</h3>
                </div>
                <div className="info_icon">
                  <i className="flaticon-envelope"></i>
                </div>
              </div>
            </div>
            <div className="col-xl-4 col-lg-4">
              <div className="single_emergency d-flex align-items-center justify-content-center emergency_bg_1 overlay_skyblue">
                <div className="info">
                  <span>Horario de atención:</span>
                  <h3>24/7</h3>
                </div>
                <div className="info_icon">
                  <i className="flaticon-clock"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <footer className="footer">
        <div className="footer_top">
          <div className="container">
            <div className="row">
              <div className="col-xl-4 col-md-6 col-lg-4">
                <div className="footer_widget">
                  <div className="footer_logo">
                    <a href="/">
                      <img src="/assets/img/logo.png" alt="HCEN" style={{maxWidth: '150px'}} />
                    </a>
                  </div>
                  <p>
                    HCEN - Historia Clínica Electrónica Nacional
                  </p>
                </div>
              </div>
              <div className="col-xl-4 col-md-6 col-lg-4">
                <div className="footer_widget">
                  <h3 className="footer_title">
                    Enlaces Útiles
                  </h3>
                  <ul>
                    <li><a href="https://www.gub.uy">Gobierno de Uruguay</a></li>
                    <li><a href="https://www.msp.gub.uy">Ministerio de Salud Pública</a></li>
                    <li><a href="https://www.gub.uy/tramites">Trámites</a></li>
                  </ul>
                </div>
              </div>
              <div className="col-xl-4 col-md-6 col-lg-4">
                <div className="footer_widget">
                  <h3 className="footer_title">
                    Contacto
                  </h3>
                  <p>
                    Montevideo, Uruguay<br />
                    Email: info@hcen.gub.uy<br />
                    Tel: 0800 1234
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="copy-right_text">
          <div className="container">
            <div className="footer_border"></div>
            <div className="row">
              <div className="col-xl-12">
                <p className="copy_right text-center">
                  © 2024 HCEN. Todos los derechos reservados.
                </p>
              </div>
            </div>
          </div>
        </div>
      </footer>

      <GenericPopup
        show={popup.show}
        onClose={() => setPopup({ ...popup, show: false })}
        message={popup.message}
        type={popup.type}
      />
    </>
  );
};

export default Login;

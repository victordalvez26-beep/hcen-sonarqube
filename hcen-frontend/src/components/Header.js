import React, { useState, useEffect } from 'react';
import config from '../config';
import '../styles/header.css';

const Header = ({ user, activePage = '', viewRole = null, setViewRole = null }) => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [windowWidth, setWindowWidth] = useState(window.innerWidth);

  useEffect(() => {
    const handleResize = () => {
      setWindowWidth(window.innerWidth);
      if (windowWidth > 768) {
        setIsMobileMenuOpen(false);
      }
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [windowWidth]);

  const isMobile = windowWidth < 768;
  
  // Determinar el rol a usar para mostrar opciones (viewRole si está disponible, sino user.rol)
  const displayRole = viewRole || (user ? user.rol : null);
  
  // Función para cambiar el rol de vista (solo para admin)
  const toggleViewRole = () => {
    if (setViewRole && user && user.rol === 'AD') {
      const newRole = viewRole === 'US' ? 'AD' : 'US';
      setViewRole(newRole);
      // Redirigir a home cuando cambia la vista y está en página de admin
      if (newRole === 'US' && (window.location.pathname.startsWith('/gestion-') || window.location.pathname === '/reportes')) {
        window.location.href = '/';
      }
    }
  };

  return (
    <header className="header">
      <div className="header-container">
        <div className="header-flex">
          {/* Logo */}
          <div className="header-logo-container">
            <a href="/" className="header-logo-link">
              <img 
                src="/assets/img/logo.png" 
                alt="HCEN" 
                className="header-logo-img"
              />
              <span className="header-logo-text">
                HCEN
              </span>
              {/* Indicador de vista cuando admin está en modo usuario */}
              {user && user.rol === 'AD' && viewRole === 'US' && (
                <span className="header-vista-badge">
                  Vista Usuario
                </span>
              )}
            </a>
          </div>

          {/* Mobile Menu Button */}
          {isMobile && (
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="header-mobile-menu-btn"
              aria-label="Toggle menu"
            >
              {isMobileMenuOpen ? '✕' : '☰'}
            </button>
          )}

          {/* Navigation */}
          <nav className={`header-nav ${isMobile && !isMobileMenuOpen ? 'hidden' : ''}`}>
            <div className="header-nav-links">
              <a 
                href="/" 
                onClick={() => isMobile && setIsMobileMenuOpen(false)}
                className={`header-nav-link ${activePage === 'home' ? 'active' : ''}`}
                onMouseEnter={(e) => e.target.style.color = '#ffffff'}
                onMouseLeave={(e) => e.target.style.color = activePage === 'home' ? '#ffffff' : '#e5e7eb'}
              >
                Inicio
                {activePage === 'home' && !isMobile && (
                  <div className="header-nav-link-indicator" />
                )}
              </a>
              
              {user && displayRole === 'US' && <a 
                href="/historia-clinica" 
                onClick={() => isMobile && setIsMobileMenuOpen(false)}
                className={`header-nav-link ${activePage === 'historia' ? 'active' : ''}`}
                onMouseEnter={(e) => e.target.style.color = '#ffffff'}
                onMouseLeave={(e) => e.target.style.color = activePage === 'historia' ? '#ffffff' : '#e5e7eb'}
              >
                Historia Clínica
                {activePage === 'historia' && !isMobile && (
                  <div className="header-nav-link-indicator" />
                )}
              </a>}

              {/* Opciones para Usuario de Salud (cuando viewRole es US o el usuario real es US) */}
              {user && displayRole !== 'AD' && (
                <>
                <a 
                  href="/mi-perfil" 
                  onClick={() => isMobile && setIsMobileMenuOpen(false)}
                  className={`header-nav-link ${activePage === 'mi-perfil' ? 'active' : ''}`}
                  onMouseEnter={(e) => e.target.style.color = '#ffffff'}
                  onMouseLeave={(e) => e.target.style.color = activePage === 'mi-perfil' ? '#ffffff' : '#e5e7eb'}
                >
                  Mi Perfil
                  {activePage === 'mi-perfil' && !isMobile && (
                    <div className="header-nav-link-indicator" />
                  )}
                </a>

                <a 
                  href="/mis-clinicas" 
                  onClick={() => isMobile && setIsMobileMenuOpen(false)}
                  className={`header-nav-link ${activePage === 'mis-clinicas' ? 'active' : ''}`}
                  onMouseEnter={(e) => e.target.style.color = '#ffffff'}
                  onMouseLeave={(e) => e.target.style.color = activePage === 'mis-clinicas' ? '#ffffff' : '#e5e7eb'}
                >
                  Mis Clínicas
                  {activePage === 'mis-clinicas' && !isMobile && (
                    <div className="header-nav-link-indicator" />
                  )}
                </a>
                </>
              )}

              {/* Opciones para Administrador (cuando viewRole es AD) */}
              {user && displayRole === 'AD' && (
                <>
                  <a 
                    href="/gestion-clinicas" 
                    onClick={() => isMobile && setIsMobileMenuOpen(false)}
                    className={`header-nav-link ${activePage === 'gestion-clinicas' ? 'active' : ''}`}
                    onMouseEnter={(e) => e.target.style.color = '#ffffff'}
                    onMouseLeave={(e) => e.target.style.color = activePage === 'gestion-clinicas' ? '#ffffff' : '#e5e7eb'}
                  >
                    {isMobile ? 'Clínicas' : 'Gestión de Clínicas'}
                    {activePage === 'gestion-clinicas' && !isMobile && (
                      <div className="header-nav-link-indicator" />
                    )}
                  </a>
                  <a 
                    href="/gestion-usuarios" 
                    onClick={() => isMobile && setIsMobileMenuOpen(false)}
                    className={`header-nav-link ${activePage === 'gestion-usuarios' ? 'active' : ''}`}
                    onMouseEnter={(e) => e.target.style.color = '#ffffff'}
                    onMouseLeave={(e) => e.target.style.color = activePage === 'gestion-usuarios' ? '#ffffff' : '#e5e7eb'}
                  >
                    {isMobile ? 'Usuarios' : 'Gestión de Usuarios'}
                    {activePage === 'gestion-usuarios' && !isMobile && (
                      <div className="header-nav-link-indicator" />
                    )}
                  </a>
                  <a 
                    href="/gestion-prestadores" 
                    onClick={() => isMobile && setIsMobileMenuOpen(false)}
                    className={`header-nav-link ${activePage === 'gestion-prestadores' ? 'active' : ''}`}
                    onMouseEnter={(e) => e.target.style.color = '#ffffff'}
                    onMouseLeave={(e) => e.target.style.color = activePage === 'gestion-prestadores' ? '#ffffff' : '#e5e7eb'}
                  >
                    {isMobile ? 'Prestadores' : 'Gestión de Prestadores'}
                    {activePage === 'gestion-prestadores' && !isMobile && (
                      <div className="header-nav-link-indicator" />
                    )}
                  </a>
                  <a 
                    href="/reportes" 
                    onClick={() => isMobile && setIsMobileMenuOpen(false)}
                    className={`header-nav-link ${activePage === 'reportes' ? 'active' : ''}`}
                    onMouseEnter={(e) => e.target.style.color = '#ffffff'}
                    onMouseLeave={(e) => e.target.style.color = activePage === 'reportes' ? '#ffffff' : '#e5e7eb'}
                  >
                    Reportes
                    {activePage === 'reportes' && !isMobile && (
                      <div className="header-nav-link-indicator" />
                    )}
                  </a>
                </>
              )}


              <a 
                href="/contact" 
                onClick={() => isMobile && setIsMobileMenuOpen(false)}
                className={`header-nav-link ${activePage === 'contact' ? 'active' : ''}`}
                onMouseEnter={(e) => e.target.style.color = '#ffffff'}
                onMouseLeave={(e) => e.target.style.color = activePage === 'contact' ? '#ffffff' : '#e5e7eb'}
              >
                Contacto
                {activePage === 'contact' && !isMobile && (
                  <div className="header-nav-link-indicator" />
                )}
              </a>
            </div>

            {/* Botones de Acción (Toggle Vista y Cerrar Sesión/Login) */}
            <div className="header-actions">
              {/* Botón Toggle de Vista (solo para Admin) */}
              {user && user.rol === 'AD' && setViewRole && (
                <button
                  onClick={toggleViewRole}
                  className={`header-toggle-btn ${viewRole === 'US' ? 'view-user' : 'view-admin'}`}
                  title={viewRole === 'US' ? 'Cambiar a Vista Administrador' : 'Cambiar a Vista Usuario de Salud'}
                >
                  {viewRole === 'US' ? 'Vista Admin' : 'Vista Usuario'}
                </button>
              )}

              {/* Botón Cerrar Sesión o Iniciar Sesión */}
              {user ? (
                <a 
                  href={`${config.BACKEND_URL}/api/auth/logout_hcen`}
                  onClick={() => isMobile && setIsMobileMenuOpen(false)}
                  className="header-logout-btn"
                >
                  Cerrar Sesión
                </a>
              ) : (
                <button
                  type="button"
                  onClick={() => {
                    const authUrl = new URL('https://auth-testing.iduruguay.gub.uy/oidc/v1/authorize');
                    authUrl.searchParams.set('client_id', '890192');
                    authUrl.searchParams.set('response_type', 'code');
                    authUrl.searchParams.set('scope', 'openid personal_info email');
                    authUrl.searchParams.set('redirect_uri', config.CALLBACK_URL);
                    authUrl.searchParams.set('state', Math.random().toString(36).substring(2, 15));
                    window.location.href = authUrl.toString();
                  }}
                  className="header-login-btn"
                >
                  Iniciar Sesión
                </button>
              )}
            </div>
          </nav>
        </div>
      </div>
    </header>
  );
};

export default Header;

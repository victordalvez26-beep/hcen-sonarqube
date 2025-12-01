import React, { useEffect, useState, useRef } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation, Navigate } from 'react-router-dom';
import Home from './components/Home';
import HistoriaClinica from './components/HistoriaClinica';
import DetalleDocumento from './components/DetalleDocumento';
import CompleteProfile from './components/CompleteProfile';
import GestionClinicas from './components/GestionClinicas';
import GestionUsuarios from './components/GestionUsuarios';
import GestionPrestadores from './components/GestionPrestadores';
import GestionPoliticas from './components/GestionPoliticas';
import MiPerfil from './components/MiPerfil';
import MisClinicas from './components/MisClinicas';
import ReportesAdmin from './components/ReportesAdmin';
import RegistroPrestador from './components/RegistroPrestador';
import Contacto from './components/Contacto';
import Header from './components/Header';
import config from './config';
import './App.css';
import './styles/colors.css';
import './styles/components.css';

function AppContent() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [viewRole, setViewRole] = useState(null); // Rol de vista activo (para cambio de perspectiva)
  const location = useLocation();
  const previousUserId = useRef(null); // Para rastrear cambios de usuario
  const viewRoleInitialized = useRef(false); // Para evitar inicializaciones múltiples

  // Función para obtener viewRole desde localStorage
  const getViewRoleFromStorage = (userId) => {
    if (!userId) return null;
    const stored = localStorage.getItem(`viewRole_${userId}`);
    return stored || null;
  };

  // Función para guardar viewRole en localStorage
  const saveViewRoleToStorage = (userId, role) => {
    if (userId && role) {
      localStorage.setItem(`viewRole_${userId}`, role);
    } else if (userId) {
      localStorage.removeItem(`viewRole_${userId}`);
    }
  };

  useEffect(() => {
    checkSession();
  }, []);
  
  // Inicializar viewRole solo cuando el usuario cambia (nuevo login o logout)
  // Usar localStorage para persistir la preferencia del usuario
  useEffect(() => {
    const currentUserId = user?.uid || null;
    
    // Si el usuario cambió (nuevo login o logout)
    if (previousUserId.current !== currentUserId) {
      const oldUserId = previousUserId.current;
      previousUserId.current = currentUserId;
      viewRoleInitialized.current = false;
      
      if (user?.uid) {
        // Intentar cargar viewRole desde localStorage
        const storedViewRole = getViewRoleFromStorage(user.uid);
        
        // Validar que el rol almacenado sea coherente con los permisos reales
        // Si el usuario NO es admin, NO puede tener viewRole 'AD'
        if (storedViewRole && (storedViewRole === 'US' || (storedViewRole === 'AD' && user.rol === 'AD'))) {
          // Si hay un viewRole guardado y es válido, usarlo
          setViewRole(storedViewRole);
        } else {
          // Si no hay viewRole guardado o es inválido, inicializar con el rol real
          setViewRole(user.rol);
          saveViewRoleToStorage(user.uid, user.rol);
        }
        viewRoleInitialized.current = true;
      } else {
        // Resetear si no hay usuario (logout)
        setViewRole(null);
        if (oldUserId) {
          localStorage.removeItem(`viewRole_${oldUserId}`);
        }
      }
    } else if (user?.uid && !viewRoleInitialized.current) {
      // Si el usuario no cambió pero aún no se inicializó (puede pasar en re-renders)
      // Cargar desde localStorage o usar el rol real
      const storedViewRole = getViewRoleFromStorage(user.uid);
      
      // Validar coherencia
      if (storedViewRole && (storedViewRole === 'US' || (storedViewRole === 'AD' && user.rol === 'AD'))) {
        setViewRole(storedViewRole);
      } else {
        setViewRole(user.rol);
        saveViewRoleToStorage(user.uid, user.rol);
      }
      viewRoleInitialized.current = true;
    }
    // Si el usuario no cambió y ya está inicializado, NO hacer nada
    // Esto previene que se resetee cuando el objeto user cambia de referencia
  }, [user]); // Solo dependemos de user
  
  // Función wrapper para setViewRole que también guarda en localStorage
  const setViewRoleWithStorage = (newRole) => {
    setViewRole(newRole);
    if (user?.uid) {
      saveViewRoleToStorage(user.uid, newRole);
    }
  };

  // Efecto adicional para restaurar viewRole desde localStorage si se pierde
  // Esto puede pasar si el componente se re-renderiza y el estado se pierde
  useEffect(() => {
    if (user?.uid && user.rol === 'AD' && !viewRole) {
      // Si es admin y no hay viewRole, intentar restaurar desde localStorage
      const storedViewRole = getViewRoleFromStorage(user.uid);
      if (storedViewRole && (storedViewRole === 'AD' || storedViewRole === 'US')) {
        setViewRole(storedViewRole);
      }
    }
  }, [user?.uid, user?.rol, viewRole]);
  
  const checkSession = async () => {
    try {
      const response = await fetch(`${config.BACKEND_URL}/api/auth/session`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      const sessionData = await response.json();
      
      if (sessionData.authenticated) {
        // Obtener perfil completo (que lee de INUS)
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
                // Combinar datos: perfil tiene prioridad, sesión tiene rol/auth
                setUser({ ...sessionData, ...profileData });
            } else {
                console.error('Error obteniendo perfil:', profileResponse.status);
                setUser(sessionData); // Fallback
            }
        } catch (error) {
            console.error('Error de red obteniendo perfil:', error);
            setUser(sessionData); // Fallback
        }
      } else {
        setUser(null);
      }
    } catch (error) {
      console.error('Error verificando sesión:', error);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'var(--background-color)'
      }}>
        <div className="text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="sr-only">Cargando...</span>
          </div>
          <p className="mt-3">Cargando HCEN...</p>
        </div>
      </div>
    );
  }

  const getActivePage = () => {
    if (location.pathname === '/') return 'home';
    if (location.pathname === '/historia-clinica') return 'historia';
    if (location.pathname.startsWith('/documento/')) return 'historia';
    if (location.pathname === '/gestion-clinicas') return 'gestion-clinicas';
    if (location.pathname === '/gestion-usuarios') return 'gestion-usuarios';
    if (location.pathname === '/gestion-prestadores') return 'gestion-prestadores';
    if (location.pathname === '/gestion-politicas') return 'gestion-politicas';
    if (location.pathname === '/reportes') return 'reportes';
    if (location.pathname === '/mis-clinicas') return 'mis-clinicas';
    if (location.pathname === '/mi-perfil') return 'mi-perfil';
    if (location.pathname === '/contact') return 'contact';
    return '';
  };

  const ProtectedRoute = ({ children }) => {
    if (!user) {
      return <Navigate to="/" replace />;
    }
    if (user && !user.profileCompleted && location.pathname !== '/complete-profile') {
      return <Navigate to="/complete-profile" replace />;
    }
    return children;
  };

  const AdminRoute = ({ children }) => {
    if (!user) {
      return <Navigate to="/" replace />;
    }
    if (user && !user.profileCompleted) {
      return <Navigate to="/complete-profile" replace />;
    }
    if (user && user.rol !== 'AD') {
      return (
        <div className="slider_area" style={{minHeight: '100vh', display: 'flex', alignItems: 'center'}}>
          <div className="container">
            <div className="row">
              <div className="col-xl-12">
                <div className="slider_text text-center">
                  <h3 style={{color: 'var(--primary-color)', marginBottom: '20px'}}>Acceso Restringido</h3>
                  <p style={{color: 'var(--text-secondary)', fontSize: '18px', marginBottom: '30px'}}>
                    Esta sección es solo para administradores
                  </p>
                  <a href="/" className="boxed-btn3" style={{
                    padding: '12px 24px',
                    fontSize: '14px',
                    textDecoration: 'none',
                    backgroundColor: 'var(--primary-color)',
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
    return children;
  };

  return (
    <div className="App">
      {location.pathname !== '/complete-profile' && (
        <Header 
          user={user} 
          activePage={getActivePage()} 
          viewRole={viewRole}
          setViewRole={setViewRoleWithStorage}
        />
      )}
      <div style={{ paddingTop: location.pathname !== '/complete-profile' ? '70px' : '0' }} className="main-content-wrapper">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/complete-profile" element={
            user && !user.profileCompleted ? <CompleteProfile /> : <Navigate to="/" replace />
          } />
          <Route path="/registro-prestador" element={<RegistroPrestador />} />
          <Route path="/historia-clinica" element={
            <ProtectedRoute>
              <HistoriaClinica />
            </ProtectedRoute>
          } />
          <Route path="/documento/:id" element={
            <ProtectedRoute>
              <DetalleDocumento />
            </ProtectedRoute>
          } />
        <Route path="/gestion-clinicas" element={
          <AdminRoute>
            <GestionClinicas />
          </AdminRoute>
        } />
        <Route path="/gestion-usuarios" element={
          <AdminRoute>
            <GestionUsuarios currentUser={user} onSessionUpdate={checkSession} />
          </AdminRoute>
        } />
        <Route path="/gestion-prestadores" element={
          <AdminRoute>
            <GestionPrestadores />
          </AdminRoute>
        } />
        <Route path="/gestion-politicas" element={
          <AdminRoute>
            <GestionPoliticas />
          </AdminRoute>
        } />
          <Route path="/reportes" element={
          <AdminRoute>
            <ReportesAdmin />
          </AdminRoute>
        } />
          <Route path="/mis-clinicas" element={
            <ProtectedRoute>
              <MisClinicas />
            </ProtectedRoute>
          } />
          <Route path="/mi-perfil" element={
            <ProtectedRoute>
              <MiPerfil />
            </ProtectedRoute>
          } />
          <Route path="/contact" element={<Contacto />} />
        </Routes>
      </div>
    </div>
  );
}

function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

export default App;

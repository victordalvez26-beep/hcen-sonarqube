import React, { useState, useEffect } from 'react';
import config from '../config';

const GestionUsuarios = ({ currentUser, onSessionUpdate }) => {
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchType, setSearchType] = useState('nombre'); // 'nombre' o 'documento'
  const [selectedUser, setSelectedUser] = useState(null);
  const [newRole, setNewRole] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [userDetails, setUserDetails] = useState(null);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${config.BACKEND_URL}/api/users/all`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        setUsuarios(data);
      } else {
        const errorData = await response.json();
        setMessage('Error cargando usuarios: ' + (errorData.error || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error cargando usuarios:', error);
      setMessage('Error de conexión al cargar usuarios');
    } finally {
      setLoading(false);
    }
  };

  const filteredUsuarios = usuarios.filter(usuario => {
    if (!searchTerm) return true;
    
    if (searchType === 'nombre') {
      return usuario.nombre.toLowerCase().includes(searchTerm.toLowerCase());
    } else {
      return usuario.documento.includes(searchTerm);
    }
  });

  const handleRoleChange = (usuario) => {
    setSelectedUser(usuario);
    setNewRole(usuario.rol);
    setShowModal(true);
  };

  const confirmRoleChange = async () => {
    if (!selectedUser || !newRole) return;

    try {
      const response = await fetch(`${config.BACKEND_URL}/api/users/role`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          uid: selectedUser.uid,
          rol: newRole
        })
      });

      if (response.ok) {
        const data = await response.json();
        setMessage(data.message || `Rol actualizado exitosamente para ${selectedUser.nombre}`);
        
        // Actualizar la lista local
        setUsuarios(prev => prev.map(usuario => 
          usuario.uid === selectedUser.uid 
            ? { 
                ...usuario, 
                rol: newRole,
                rolDescripcion: newRole === 'AD' ? 'Administrador HCEN' : 'Usuario de la Salud'
              }
            : usuario
        ));

        // Si el usuario modificado es el actual, actualizar la sesión global
        if (currentUser && selectedUser && currentUser.uid === selectedUser.uid) {
            if (onSessionUpdate) {
                // Pequeño delay para asegurar que el backend procesó todo si es asíncrono,
                // aunque aquí ya recibimos response.ok
                setTimeout(() => onSessionUpdate(), 100);
            }
        }
      } else {
        const errorData = await response.json();
        setMessage('Error actualizando rol: ' + (errorData.error || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error actualizando rol:', error);
      setMessage('Error de conexión al actualizar rol');
    }

    setShowModal(false);
    setSelectedUser(null);
    setNewRole('');

    // Limpiar mensaje después de 5 segundos
    setTimeout(() => setMessage(''), 5000);
  };

  const handleViewDetails = async (uid) => {
    setLoadingDetails(true);
    setShowDetailModal(true);
    setUserDetails(null);
    
    try {
      const response = await fetch(`${config.BACKEND_URL}/api/users/${uid}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        setUserDetails(data);
      } else {
        // Intentar parsear error
        try {
            const errorData = await response.json();
            setMessage('Error: ' + (errorData.error || 'No se pudo cargar detalles'));
        } catch (e) {
            setMessage('Error cargando detalles del usuario');
        }
        setShowDetailModal(false);
      }
    } catch (error) {
      console.error('Error cargando detalles:', error);
      setMessage('Error de conexión al cargar detalles');
      setShowDetailModal(false);
    } finally {
      setLoadingDetails(false);
    }
  };

  const getRoleBadgeColor = (rol) => {
    return rol === 'AD' ? '#8b5cf6' : '#3b82f6';
  };

  const getRoleBadgeText = (rol) => {
    return rol === 'AD' ? 'Administrador' : 'Usuario';
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
                <p className="mt-3 text-white">Cargando usuarios...</p>
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
                  Gestión de Usuarios
                </h3>
                <p style={{
                  color: 'var(--text-light)',
                  fontSize: '18px',
                  marginBottom: '0',
                  fontWeight: '400'
                }}>
                  Administra los roles y permisos de los usuarios del sistema
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
          <div className="alert alert-success alert-dismissible fade show" role="alert" style={{
            marginBottom: '30px',
            borderRadius: '10px',
            border: 'none',
            backgroundColor: '#d1fae5',
            color: '#065f46'
          }}>
            <i className="fa fa-check-circle" style={{marginRight: '8px'}}></i>
            {message}
            <button type="button" className="btn-close" onClick={() => setMessage('')}>
              <i className="fa fa-times"></i>
            </button>
          </div>
        )}

        <div className="row">
          <div className="col-xl-12">
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
                  Buscar Usuarios
                </h5>
                <div className="row">
                  <div className="col-md-3">
                    <label className="form-label">Tipo de búsqueda</label>
                    <select 
                      className="form-control" 
                      value={searchType} 
                      onChange={(e) => setSearchType(e.target.value)}
                      style={{
                        borderRadius: '8px',
                        border: '2px solid #e5e7eb',
                        padding: '10px 15px'
                      }}
                    >
                      <option value="nombre">Por Nombre</option>
                      <option value="documento">Por Documento</option>
                    </select>
                  </div>
                  <div className="col-md-9">
                    <label className="form-label">Término de búsqueda</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder={searchType === 'nombre' ? 'Ingrese el nombre del usuario...' : 'Ingrese el número de documento...'}
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

            {/* Tabla de usuarios */}
            <div className="card" style={{
              borderRadius: '15px',
              boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
              border: '1px solid var(--border-color)'
            }}>
              <div className="card-body" style={{padding: '30px'}}>
                <h5 className="card-title" style={{
                  color: '#1f2937',
                  marginBottom: '25px',
                  fontSize: '20px',
                  fontWeight: '600'
                }}>
                  <i className="fa fa-users" style={{marginRight: '10px', color: '#3b82f6'}}></i>
                  Lista de Usuarios ({filteredUsuarios.length})
                </h5>
                
                <div className="table-responsive">
                  <table className="table table-hover" style={{
                    width: '100%',
                    borderCollapse: 'separate',
                    borderSpacing: '0 10px'
                  }}>
                    <thead>
                      <tr style={{backgroundColor: '#f8fafc'}}>
                        <th style={{padding: '15px 20px', borderTopLeftRadius: '8px', borderBottomLeftRadius: '8px', color: '#374151', fontWeight: '600'}}>Usuario</th>
                        <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Email</th>
                        <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Documento</th>
                        <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Rol Actual</th>
                        <th style={{padding: '15px 20px', color: '#374151', fontWeight: '600'}}>Perfil</th>
                        <th style={{padding: '15px 20px', borderTopRightRadius: '8px', borderBottomRightRadius: '8px', color: '#374151', fontWeight: '600'}}>Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredUsuarios.map(usuario => (
                        <tr key={usuario.id} style={{
                          backgroundColor: '#ffffff',
                          boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
                          transition: 'all 0.2s ease',
                          verticalAlign: 'middle'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)'}
                        onMouseLeave={(e) => e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.05)'}
                        >
                          <td style={{padding: '15px 20px', borderTopLeftRadius: '8px', borderBottomLeftRadius: '8px'}}>
                            <div>
                              <div style={{color: '#1f2937', fontWeight: '600', fontSize: '16px'}}>
                                {usuario.nombre}
                              </div>
                              <div style={{color: '#6b7280', fontSize: '14px'}}>
                                {usuario.uid}
                              </div>
                            </div>
                          </td>
                          <td style={{padding: '15px 20px', color: '#374151'}}>{usuario.email}</td>
                          <td style={{padding: '15px 20px', color: '#374151', fontFamily: 'monospace'}}>{usuario.documento}</td>
                          <td style={{padding: '15px 20px'}}>
                            <span className="badge" style={{
                              padding: '8px 12px',
                              borderRadius: '5px',
                              fontWeight: '600',
                              fontSize: '12px',
                              backgroundColor: getRoleBadgeColor(usuario.rol),
                              color: '#ffffff'
                            }}>
                              {getRoleBadgeText(usuario.rol)}
                            </span>
                          </td>
                          <td style={{padding: '15px 20px'}}>
                            <span className={`badge ${usuario.profileCompleted ? 'badge-success' : 'badge-warning'}`} style={{
                              padding: '6px 10px',
                              borderRadius: '4px',
                              fontWeight: '500',
                              fontSize: '11px',
                              backgroundColor: usuario.profileCompleted ? '#d1fae5' : '#fef3c7',
                              color: usuario.profileCompleted ? '#065f46' : '#92400e'
                            }}>
                              {usuario.profileCompleted ? 'Completado' : 'Pendiente'}
                            </span>
                          </td>
                          <td style={{padding: '15px 20px', borderTopRightRadius: '8px', borderBottomRightRadius: '8px'}}>
                            <div style={{display: 'flex', alignItems: 'center'}}>
                                <button
                                  onClick={() => handleViewDetails(usuario.uid)}
                                  className="btn btn-info btn-sm"
                                  style={{
                                    color: '#ffffff',
                                    border: 'none',
                                    borderRadius: '6px',
                                    padding: '8px 12px',
                                    fontSize: '14px',
                                    marginRight: '8px',
                                    cursor: 'pointer',
                                    backgroundColor: '#0ea5e9',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    width: '36px',
                                    height: '36px'
                                  }}
                                  title="Ver Detalles Completos (INUS)"
                                >
                                  <i className="fa fa-eye"></i>
                                </button>
                                <button
                                  onClick={() => handleRoleChange(usuario)}
                                  style={{
                                    backgroundColor: '#3b82f6',
                                    color: '#ffffff',
                                    border: 'none',
                                    borderRadius: '6px',
                                    padding: '8px 16px',
                                    fontSize: '14px',
                                    fontWeight: '500',
                                    transition: 'all 0.3s ease',
                                    cursor: 'pointer',
                                    height: '36px'
                                  }}
                                  onMouseEnter={(e) => {
                                    e.target.style.backgroundColor = '#2563eb';
                                    e.target.style.transform = 'translateY(-1px)';
                                  }}
                                  onMouseLeave={(e) => {
                                    e.target.style.backgroundColor = '#3b82f6';
                                    e.target.style.transform = 'translateY(0)';
                                  }}
                                >
                                  <i className="fa fa-edit" style={{marginRight: '5px'}}></i>
                                  Cambiar Rol
                                </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                      {filteredUsuarios.length === 0 && (
                        <tr>
                          <td colSpan="6" className="text-center" style={{padding: '40px', color: '#6b7280'}}>
                            <i className="fa fa-search" style={{fontSize: '48px', marginBottom: '15px', opacity: '0.3'}}></i>
                            <div style={{fontSize: '18px', fontWeight: '500'}}>No se encontraron usuarios</div>
                            <div style={{fontSize: '14px', marginTop: '5px'}}>
                              {searchTerm ? 'Intenta con otros términos de búsqueda' : 'No hay usuarios registrados'}
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

      {/* Modal para cambiar rol */}
      {showModal && selectedUser && (
        <div className="modal fade show" style={{
          display: 'block',
          backgroundColor: 'rgba(0,0,0,0.5)',
          zIndex: 1050
        }}>
          <div className="modal-dialog modal-dialog-centered">
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
                  <i className="fa fa-user-edit" style={{marginRight: '10px', color: 'var(--primary-color)'}}></i>
                  Cambiar Rol de Usuario
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => setShowModal(false)}
                  style={{fontSize: '20px'}}
                >
                  <i className="fa fa-times"></i>
                </button>
              </div>
              <div className="modal-body" style={{padding: '30px'}}>
                <div className="mb-4">
                  <h6 style={{color: 'var(--heading-color)', marginBottom: '15px', fontWeight: '600'}}>
                    Información del Usuario
                  </h6>
                  <div style={{
                    backgroundColor: '#f8fafc',
                    padding: '15px',
                    borderRadius: '8px',
                    border: '1px solid #e5e7eb'
                  }}>
                    <div style={{marginBottom: '8px'}}>
                      <strong>Nombre:</strong> {selectedUser.nombre}
                    </div>
                    <div style={{marginBottom: '8px'}}>
                      <strong>Email:</strong> {selectedUser.email}
                    </div>
                    <div style={{marginBottom: '8px'}}>
                      <strong>Documento:</strong> {selectedUser.documento}
                    </div>
                    <div>
                      <strong>Rol Actual:</strong> 
                      <span className="badge" style={{
                        marginLeft: '8px',
                        padding: '4px 8px',
                        borderRadius: '4px',
                        backgroundColor: getRoleBadgeColor(selectedUser.rol),
                        color: '#ffffff',
                        fontSize: '12px'
                      }}>
                        {getRoleBadgeText(selectedUser.rol)}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="mb-4">
                  <label htmlFor="newRole" className="form-label" style={{
                    color: 'var(--heading-color)',
                    fontWeight: '600',
                    marginBottom: '10px'
                  }}>
                    Nuevo Rol
                  </label>
                  <select
                    id="newRole"
                    className="form-control"
                    value={newRole}
                    onChange={(e) => setNewRole(e.target.value)}
                    style={{
                      borderRadius: '8px',
                      border: '2px solid #e5e7eb',
                      padding: '12px 15px',
                      fontSize: '16px'
                    }}
                  >
                    <option value="US">Usuario de la Salud</option>
                    <option value="AD">Administrador HCEN</option>
                  </select>
                  <div className="form-text" style={{color: 'var(--text-secondary)', fontSize: '14px', marginTop: '5px'}}>
                    Los administradores tienen acceso completo al sistema, incluyendo la gestión de usuarios y clínicas.
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
                  onClick={() => setShowModal(false)}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '8px',
                    fontWeight: '500'
                  }}
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={confirmRoleChange}
                  disabled={!newRole || newRole === selectedUser.rol}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '8px',
                    fontWeight: '500',
                    backgroundColor: 'var(--primary-color)',
                    border: 'none'
                  }}
                >
                  <i className="fa fa-save" style={{marginRight: '5px'}}></i>
                  Confirmar Cambio
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal de Detalles (INUS) */}
      {showDetailModal && (
        <div className="modal fade show" style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1060 }}>
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content" style={{ borderRadius: '15px', border: 'none', boxShadow: '0 10px 30px rgba(0,0,0,0.2)' }}>
              <div className="modal-header" style={{ borderBottom: '1px solid #e5e7eb', padding: '20px 30px', backgroundColor: '#f8fafc', borderTopLeftRadius: '15px', borderTopRightRadius: '15px' }}>
                <h5 className="modal-title" style={{ color: '#1f2937', fontWeight: '600', fontSize: '20px' }}>
                  <i className="fa fa-id-card" style={{ marginRight: '10px', color: '#0ea5e9' }}></i>
                  Detalles del Usuario (INUS)
                </h5>
                <button type="button" className="btn-close" onClick={() => setShowDetailModal(false)}></button>
              </div>
              <div className="modal-body" style={{ padding: '30px' }}>
                {loadingDetails ? (
                  <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status" style={{width: '3rem', height: '3rem'}}></div>
                    <p className="mt-3 text-muted">Consultando información maestra en INUS...</p>
                  </div>
                ) : userDetails ? (
                  <div className="row">
                    <div className="col-md-6 mb-4">
                        <label style={{color: '#6b7280', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px', display: 'block'}}>Nombre Completo</label>
                        <div style={{padding: '10px 15px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e5e7eb', color: '#1f2937', fontWeight: '500'}}>
                            {[userDetails.primerNombre, userDetails.segundoNombre, userDetails.primerApellido, userDetails.segundoApellido].filter(Boolean).join(' ') || 'N/A'}
                        </div>
                    </div>
                    <div className="col-md-6 mb-4">
                        <label style={{color: '#6b7280', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px', display: 'block'}}>Documento</label>
                        <div style={{padding: '10px 15px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e5e7eb', color: '#1f2937', fontFamily: 'monospace'}}>
                            {userDetails.tipDocum} {userDetails.codDocum}
                        </div>
                    </div>
                    <div className="col-md-6 mb-4">
                        <label style={{color: '#6b7280', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px', display: 'block'}}>Email</label>
                        <div style={{padding: '10px 15px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e5e7eb', color: '#1f2937'}}>
                            {userDetails.email || 'N/A'}
                        </div>
                    </div>
                    <div className="col-md-6 mb-4">
                        <label style={{color: '#6b7280', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px', display: 'block'}}>Teléfono</label>
                        <div style={{padding: '10px 15px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e5e7eb', color: '#1f2937'}}>
                            {userDetails.telefono || 'N/A'}
                        </div>
                    </div>
                    <div className="col-md-12 mb-4">
                        <label style={{color: '#6b7280', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px', display: 'block'}}>Dirección</label>
                        <div style={{padding: '10px 15px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e5e7eb', color: '#1f2937'}}>
                            {userDetails.direccion ? `${userDetails.direccion}, ${userDetails.localidad || ''}, ${userDetails.departamento || ''}` : 'N/A'}
                        </div>
                    </div>
                    <div className="col-md-6 mb-4">
                        <label style={{color: '#6b7280', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px', display: 'block'}}>Nacionalidad</label>
                        <div style={{padding: '10px 15px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e5e7eb', color: '#1f2937'}}>
                            {userDetails.nacionalidad || 'N/A'}
                        </div>
                    </div>
                    <div className="col-md-6 mb-4">
                        <label style={{color: '#6b7280', fontSize: '12px', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px', display: 'block'}}>UID del Sistema</label>
                        <div style={{padding: '10px 15px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e5e7eb', color: '#6b7280', fontFamily: 'monospace', fontSize: '13px'}}>
                            {userDetails.uid}
                        </div>
                    </div>
                  </div>
                ) : (
                  <div className="alert alert-warning">No se encontró información detallada para este usuario.</div>
                )}
              </div>
              <div className="modal-footer" style={{ borderTop: '1px solid #e5e7eb', padding: '20px 30px', backgroundColor: '#f8fafc', borderBottomLeftRadius: '15px', borderBottomRightRadius: '15px' }}>
                <button 
                    type="button" 
                    className="btn btn-secondary" 
                    onClick={() => setShowDetailModal(false)}
                    style={{padding: '10px 20px', borderRadius: '8px', fontWeight: '500'}}
                >
                    Cerrar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default GestionUsuarios;

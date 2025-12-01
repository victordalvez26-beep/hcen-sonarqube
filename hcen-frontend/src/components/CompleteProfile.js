import React, { useState } from 'react';
import config from '../config';

const CompleteProfile = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    fechaNacimiento: '',
    departamento: '',
    localidad: '',
    direccion: '',
    telefono: '',
    codigoPostal: '',
    nacionalidad: 'UY'
  });

  const departamentos = [
    'ARTIGAS', 'CANELONES', 'CERRO_LARGO', 'COLONIA', 'DURAZNO', 
    'FLORES', 'FLORIDA', 'LAVALLEJA', 'MALDONADO', 'MONTEVIDEO', 
    'PAYSANDU', 'RIO_NEGRO', 'RIVERA', 'ROCHA', 'SALTO', 
    'SAN_JOSE', 'SORIANO', 'TACUAREMBO', 'TREINTA_Y_TRES'
  ];

  const departamentosDisplay = {
    'ARTIGAS': 'Artigas',
    'CANELONES': 'Canelones',
    'CERRO_LARGO': 'Cerro Largo',
    'COLONIA': 'Colonia',
    'DURAZNO': 'Durazno',
    'FLORES': 'Flores',
    'FLORIDA': 'Florida',
    'LAVALLEJA': 'Lavalleja',
    'MALDONADO': 'Maldonado',
    'MONTEVIDEO': 'Montevideo',
    'PAYSANDU': 'Paysandú',
    'RIO_NEGRO': 'Río Negro',
    'RIVERA': 'Rivera',
    'ROCHA': 'Rocha',
    'SALTO': 'Salto',
    'SAN_JOSE': 'San José',
    'SORIANO': 'Soriano',
    'TACUAREMBO': 'Tacuarembó',
    'TREINTA_Y_TRES': 'Treinta y Tres'
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    if (!formData.fechaNacimiento || !formData.departamento || !formData.localidad || 
        !formData.direccion || !formData.telefono || !formData.codigoPostal || !formData.nacionalidad) {
      setError('Todos los campos son obligatorios');
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(`${config.BACKEND_URL}/api/users/complete-profile`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      });

      const data = await response.json();

      if (response.ok && data.success) {
        window.location.href = '/';
      } else {
        setError(data.error || 'Error completando el perfil');
      }
    } catch (error) {
      console.error('Error:', error);
      setError('Error de conexión. Por favor, intente nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #1f2b7b 0%, #3b82f6 100%)',
      paddingTop: '80px',
      paddingBottom: '80px',
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
        opacity: '0.1',
        zIndex: '1'
      }}></div>
      
      <div className="container" style={{position: 'relative', zIndex: '2'}}>
        <div className="row justify-content-center">
          <div className="col-xl-8 col-lg-10">
            <div style={{
              backgroundColor: '#ffffff',
              borderRadius: '20px',
              padding: '50px',
              boxShadow: '0 20px 60px rgba(0,0,0,0.2)',
              border: '1px solid #e2e8f0'
            }}>
              <div className="text-center" style={{marginBottom: '40px'}}>
                <i className="flaticon-user" style={{fontSize: '64px', color: '#3b82f6', marginBottom: '20px'}}></i>
                <h2 style={{
                  color: '#1f2b7b',
                  fontSize: '36px',
                  fontWeight: '700',
                  marginBottom: '15px'
                }}>
                  Completar Perfil
                </h2>
                <p style={{
                  color: '#64748b',
                  fontSize: '16px',
                  marginBottom: '0'
                }}>
                  Para acceder a tu historia clínica, necesitamos algunos datos adicionales
                </p>
              </div>

              {error && (
                <div style={{
                  backgroundColor: '#fee2e2',
                  border: '1px solid #fecaca',
                  color: '#dc2626',
                  padding: '15px 20px',
                  borderRadius: '10px',
                  marginBottom: '30px',
                  textAlign: 'center',
                  fontWeight: '500'
                }}>
                  <i className="flaticon-warning" style={{marginRight: '8px'}}></i>
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit}>
                <div className="row">
                  <div className="col-md-6" style={{marginBottom: '25px'}}>
                    <label style={{
                      display: 'block',
                      marginBottom: '10px',
                      color: '#1e293b',
                      fontWeight: '600',
                      fontSize: '14px'
                    }}>
                      <i className="flaticon-calendar" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                      Fecha de Nacimiento *
                    </label>
                    <input
                      type="date"
                      name="fechaNacimiento"
                      value={formData.fechaNacimiento}
                      onChange={handleChange}
                      required
                      style={{
                        width: '100%',
                        padding: '15px 20px',
                        border: '2px solid #e2e8f0',
                        borderRadius: '10px',
                        fontSize: '15px',
                        color: '#2d3748',
                        transition: 'all 0.3s ease',
                        outline: 'none'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
                      onBlur={(e) => e.target.style.borderColor = '#e2e8f0'}
                    />
                  </div>

                  <div className="col-md-6" style={{marginBottom: '25px'}}>
                    <label style={{
                      display: 'block',
                      marginBottom: '10px',
                      color: '#1e293b',
                      fontWeight: '600',
                      fontSize: '14px'
                    }}>
                      <i className="flaticon-location" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                      Departamento *
                    </label>
                    <select
                      name="departamento"
                      value={formData.departamento}
                      onChange={handleChange}
                      required
                      style={{
                        width: '100%',
                        padding: '15px 20px',
                        border: '2px solid #e2e8f0',
                        borderRadius: '10px',
                        fontSize: '15px',
                        color: '#2d3748',
                        transition: 'all 0.3s ease',
                        outline: 'none',
                        backgroundColor: '#ffffff'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
                      onBlur={(e) => e.target.style.borderColor = '#e2e8f0'}
                    >
                      <option value="">Seleccione un departamento</option>
                      {departamentos.map(dept => (
                        <option key={dept} value={dept}>{departamentosDisplay[dept]}</option>
                      ))}
                    </select>
                  </div>

                  <div className="col-md-6" style={{marginBottom: '25px'}}>
                    <label style={{
                      display: 'block',
                      marginBottom: '10px',
                      color: '#1e293b',
                      fontWeight: '600',
                      fontSize: '14px'
                    }}>
                      <i className="flaticon-location" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                      Localidad *
                    </label>
                    <input
                      type="text"
                      name="localidad"
                      value={formData.localidad}
                      onChange={handleChange}
                      required
                      placeholder="Ej: Ciudad de la Costa"
                      style={{
                        width: '100%',
                        padding: '15px 20px',
                        border: '2px solid #e2e8f0',
                        borderRadius: '10px',
                        fontSize: '15px',
                        color: '#2d3748',
                        transition: 'all 0.3s ease',
                        outline: 'none'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
                      onBlur={(e) => e.target.style.borderColor = '#e2e8f0'}
                    />
                  </div>

                  <div className="col-md-6" style={{marginBottom: '25px'}}>
                    <label style={{
                      display: 'block',
                      marginBottom: '10px',
                      color: '#1e293b',
                      fontWeight: '600',
                      fontSize: '14px'
                    }}>
                      <i className="flaticon-phone" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                      Teléfono *
                    </label>
                    <input
                      type="tel"
                      name="telefono"
                      value={formData.telefono}
                      onChange={handleChange}
                      required
                      placeholder="Ej: 099123456"
                      style={{
                        width: '100%',
                        padding: '15px 20px',
                        border: '2px solid #e2e8f0',
                        borderRadius: '10px',
                        fontSize: '15px',
                        color: '#2d3748',
                        transition: 'all 0.3s ease',
                        outline: 'none'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
                      onBlur={(e) => e.target.style.borderColor = '#e2e8f0'}
                    />
                  </div>

                  <div className="col-md-8" style={{marginBottom: '25px'}}>
                    <label style={{
                      display: 'block',
                      marginBottom: '10px',
                      color: '#1e293b',
                      fontWeight: '600',
                      fontSize: '14px'
                    }}>
                      <i className="flaticon-home" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                      Dirección *
                    </label>
                    <input
                      type="text"
                      name="direccion"
                      value={formData.direccion}
                      onChange={handleChange}
                      required
                      placeholder="Ej: Av. Italia 2525"
                      style={{
                        width: '100%',
                        padding: '15px 20px',
                        border: '2px solid #e2e8f0',
                        borderRadius: '10px',
                        fontSize: '15px',
                        color: '#2d3748',
                        transition: 'all 0.3s ease',
                        outline: 'none'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
                      onBlur={(e) => e.target.style.borderColor = '#e2e8f0'}
                    />
                  </div>

                  <div className="col-md-4" style={{marginBottom: '25px'}}>
                    <label style={{
                      display: 'block',
                      marginBottom: '10px',
                      color: '#1e293b',
                      fontWeight: '600',
                      fontSize: '14px'
                    }}>
                      <i className="flaticon-location" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                      Código Postal *
                    </label>
                    <input
                      type="text"
                      name="codigoPostal"
                      value={formData.codigoPostal}
                      onChange={handleChange}
                      required
                      placeholder="Ej: 11600"
                      style={{
                        width: '100%',
                        padding: '15px 20px',
                        border: '2px solid #e2e8f0',
                        borderRadius: '10px',
                        fontSize: '15px',
                        color: '#2d3748',
                        transition: 'all 0.3s ease',
                        outline: 'none'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
                      onBlur={(e) => e.target.style.borderColor = '#e2e8f0'}
                    />
                  </div>

                  <div className="col-md-12" style={{marginBottom: '25px'}}>
                    <label style={{
                      display: 'block',
                      marginBottom: '10px',
                      color: '#1e293b',
                      fontWeight: '600',
                      fontSize: '14px'
                    }}>
                      <i className="flaticon-globe" style={{marginRight: '8px', color: '#3b82f6'}}></i>
                      Nacionalidad *
                    </label>
                    <select
                      name="nacionalidad"
                      value={formData.nacionalidad}
                      onChange={handleChange}
                      required
                      style={{
                        width: '100%',
                        padding: '15px 20px',
                        border: '2px solid #e2e8f0',
                        borderRadius: '10px',
                        fontSize: '15px',
                        color: '#2d3748',
                        transition: 'all 0.3s ease',
                        outline: 'none',
                        backgroundColor: '#ffffff'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#3b82f6'}
                      onBlur={(e) => e.target.style.borderColor = '#e2e8f0'}
                    >
                      <option value="UY">Uruguaya</option>
                      <option value="AR">Argentina</option>
                      <option value="BR">Brasileña</option>
                      <option value="PY">Paraguaya</option>
                      <option value="CL">Chilena</option>
                      <option value="BO">Boliviana</option>
                      <option value="PE">Peruana</option>
                      <option value="VE">Venezolana</option>
                      <option value="CO">Colombiana</option>
                      <option value="EC">Ecuatoriana</option>
                      <option value="OTHER">Otra</option>
                    </select>
                  </div>
                </div>

                <div style={{
                  borderTop: '1px solid #e2e8f0',
                  paddingTop: '30px',
                  marginTop: '20px'
                }}>
                  <button
                    type="submit"
                    disabled={loading}
                    style={{
                      width: '100%',
                      padding: '18px 40px',
                      fontSize: '16px',
                      fontWeight: '700',
                      backgroundColor: loading ? '#94a3b8' : '#3b82f6',
                      color: '#ffffff',
                      border: 'none',
                      borderRadius: '10px',
                      cursor: loading ? 'not-allowed' : 'pointer',
                      transition: 'all 0.3s ease',
                      textTransform: 'uppercase',
                      letterSpacing: '1px'
                    }}
                    onMouseEnter={(e) => {
                      if (!loading) e.target.style.backgroundColor = '#1d4ed8';
                    }}
                    onMouseLeave={(e) => {
                      if (!loading) e.target.style.backgroundColor = '#3b82f6';
                    }}
                  >
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm" style={{marginRight: '10px'}}></span>
                        Guardando...
                      </>
                    ) : (
                      <>
                        <i className="flaticon-checkmark" style={{marginRight: '10px'}}></i>
                        Completar Perfil
                      </>
                    )}
                  </button>

                  <p style={{
                    textAlign: 'center',
                    marginTop: '20px',
                    color: '#64748b',
                    fontSize: '13px',
                    marginBottom: '0'
                  }}>
                    <i className="flaticon-info" style={{marginRight: '5px'}}></i>
                    Todos los campos son obligatorios para acceder a tu historia clínica
                  </p>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CompleteProfile;


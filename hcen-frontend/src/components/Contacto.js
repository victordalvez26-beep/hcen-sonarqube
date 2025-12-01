import React from 'react';

const Contacto = () => {
  return (
    <div style={{minHeight: '100vh', backgroundColor: '#f8fafc'}}>
      {/* Hero Section */}
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
                  Contacto
                </h3>
                <p style={{
                  color: 'var(--text-light)',
                  fontSize: '18px',
                  marginBottom: '0',
                  fontWeight: '400'
                }}>
                  Estamos aquí para ayudarte
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

      {/* Información de Contacto Principal */}
      <div style={{padding: '80px 0', backgroundColor: '#ffffff'}}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div style={{maxWidth: '1000px', margin: '0 auto'}}>
                <div style={{
                  textAlign: 'center',
                  marginBottom: '60px'
                }}>
                  <h2 style={{
                    fontSize: '36px',
                    fontWeight: '700',
                    color: '#1f2937',
                    marginBottom: '15px'
                  }}>
                    Agencia de Gobierno Electrónico y Sociedad de la Información y del Conocimiento
                  </h2>
                  <p style={{
                    fontSize: '18px',
                    color: '#6b7280',
                    lineHeight: '1.6'
                  }}>
                    Somos la entidad responsable de la Historia Clínica Electrónica Nacional (HCEN)
                  </p>
                </div>

                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                  gap: '40px',
                  marginTop: '50px'
                }}>
                  {/* Dirección */}
                  <div style={{
                    backgroundColor: '#f8fafc',
                    padding: '40px 30px',
                    borderRadius: '16px',
                    boxShadow: '0 4px 6px rgba(0,0,0,0.07)',
                    textAlign: 'center',
                    transition: 'transform 0.3s ease, box-shadow 0.3s ease',
                    border: '1px solid #e5e7eb'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-8px)';
                    e.currentTarget.style.boxShadow = '0 12px 24px rgba(0,0,0,0.12)';
                    e.currentTarget.style.borderColor = '#3b82f6';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 4px 6px rgba(0,0,0,0.07)';
                    e.currentTarget.style.borderColor = '#e5e7eb';
                  }}>
                    <div style={{
                      width: '80px',
                      height: '80px',
                      borderRadius: '50%',
                      backgroundColor: '#3b82f6',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      margin: '0 auto 25px',
                      boxShadow: '0 4px 12px rgba(59, 130, 246, 0.3)'
                    }}>
                      <i className="fa fa-map-marker" style={{
                        fontSize: '32px',
                        color: '#ffffff'
                      }}></i>
                    </div>
                    <h3 style={{
                      fontSize: '22px',
                      fontWeight: '600',
                      color: '#1f2937',
                      marginBottom: '20px'
                    }}>
                      Dirección
                    </h3>
                    <p style={{
                      fontSize: '16px',
                      color: '#4b5563',
                      lineHeight: '1.8',
                      margin: '0'
                    }}>
                      Liniers 1324 piso 4<br />
                      Montevideo, Uruguay
                    </p>
                  </div>

                  {/* Teléfono */}
                  <div style={{
                    backgroundColor: '#f8fafc',
                    padding: '40px 30px',
                    borderRadius: '16px',
                    boxShadow: '0 4px 6px rgba(0,0,0,0.07)',
                    textAlign: 'center',
                    transition: 'transform 0.3s ease, box-shadow 0.3s ease',
                    border: '1px solid #e5e7eb'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-8px)';
                    e.currentTarget.style.boxShadow = '0 12px 24px rgba(0,0,0,0.12)';
                    e.currentTarget.style.borderColor = '#3b82f6';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 4px 6px rgba(0,0,0,0.07)';
                    e.currentTarget.style.borderColor = '#e5e7eb';
                  }}>
                    <div style={{
                      width: '80px',
                      height: '80px',
                      borderRadius: '50%',
                      backgroundColor: '#10b981',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      margin: '0 auto 25px',
                      boxShadow: '0 4px 12px rgba(16, 185, 129, 0.3)'
                    }}>
                      <i className="fa fa-phone" style={{
                        fontSize: '32px',
                        color: '#ffffff'
                      }}></i>
                    </div>
                    <h3 style={{
                      fontSize: '22px',
                      fontWeight: '600',
                      color: '#1f2937',
                      marginBottom: '20px'
                    }}>
                      Teléfono
                    </h3>
                    <p style={{
                      fontSize: '18px',
                      color: '#4b5563',
                      lineHeight: '1.8',
                      margin: '0'
                    }}>
                      <a href="tel:+59829012929" style={{
                        color: '#10b981',
                        textDecoration: 'none',
                        fontWeight: '600',
                        fontSize: '20px',
                        transition: 'color 0.2s ease'
                      }}
                      onMouseEnter={(e) => e.target.style.color = '#059669'}
                      onMouseLeave={(e) => e.target.style.color = '#10b981'}>
                        (+598) 2901 2929
                      </a>
                    </p>
                  </div>

                  {/* Horario */}
                  <div style={{
                    backgroundColor: '#f8fafc',
                    padding: '40px 30px',
                    borderRadius: '16px',
                    boxShadow: '0 4px 6px rgba(0,0,0,0.07)',
                    textAlign: 'center',
                    transition: 'transform 0.3s ease, box-shadow 0.3s ease',
                    border: '1px solid #e5e7eb'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-8px)';
                    e.currentTarget.style.boxShadow = '0 12px 24px rgba(0,0,0,0.12)';
                    e.currentTarget.style.borderColor = '#3b82f6';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 4px 6px rgba(0,0,0,0.07)';
                    e.currentTarget.style.borderColor = '#e5e7eb';
                  }}>
                    <div style={{
                      width: '80px',
                      height: '80px',
                      borderRadius: '50%',
                      backgroundColor: '#f59e0b',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      margin: '0 auto 25px',
                      boxShadow: '0 4px 12px rgba(245, 158, 11, 0.3)'
                    }}>
                      <i className="fa fa-clock-o" style={{
                        fontSize: '32px',
                        color: '#ffffff'
                      }}></i>
                    </div>
                    <h3 style={{
                      fontSize: '22px',
                      fontWeight: '600',
                      color: '#1f2937',
                      marginBottom: '20px'
                    }}>
                      Horario de Atención
                    </h3>
                    <p style={{
                      fontSize: '16px',
                      color: '#4b5563',
                      lineHeight: '1.8',
                      margin: '0'
                    }}>
                      <strong style={{color: '#1f2937'}}>Lunes a viernes</strong><br />
                      9:00 a 17:00 h
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Información Adicional */}
      <div style={{padding: '80px 0', backgroundColor: '#f8fafc'}}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div style={{maxWidth: '900px', margin: '0 auto'}}>
                <div style={{
                  backgroundColor: '#ffffff',
                  padding: '50px 40px',
                  borderRadius: '16px',
                  boxShadow: '0 4px 6px rgba(0,0,0,0.07)'
                }}>
                  <h3 style={{
                    fontSize: '28px',
                    fontWeight: '700',
                    color: '#1f2937',
                    marginBottom: '25px',
                    textAlign: 'center'
                  }}>
                    ¿Necesitas ayuda con HCEN?
                  </h3>
                  <p style={{
                    fontSize: '17px',
                    color: '#4b5563',
                    lineHeight: '1.8',
                    marginBottom: '30px',
                    textAlign: 'center'
                  }}>
                    Si tienes consultas sobre la Historia Clínica Electrónica Nacional, problemas técnicos, o necesitas información adicional, no dudes en contactarnos durante nuestro horario de atención.
                  </p>
                  
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                    gap: '30px',
                    marginTop: '40px'
                  }}>
                    <div style={{
                      textAlign: 'center',
                      padding: '25px',
                      backgroundColor: '#f8fafc',
                      borderRadius: '12px'
                    }}>
                      <i className="fa fa-info-circle" style={{
                        fontSize: '32px',
                        color: '#3b82f6',
                        marginBottom: '15px'
                      }}></i>
                      <h4 style={{
                        fontSize: '18px',
                        fontWeight: '600',
                        color: '#1f2937',
                        marginBottom: '10px'
                      }}>
                        Información General
                      </h4>
                      <p style={{
                        fontSize: '15px',
                        color: '#6b7280',
                        margin: '0'
                      }}>
                        Consultas sobre el funcionamiento de HCEN
                      </p>
                    </div>

                    <div style={{
                      textAlign: 'center',
                      padding: '25px',
                      backgroundColor: '#f8fafc',
                      borderRadius: '12px'
                    }}>
                      <i className="fa fa-wrench" style={{
                        fontSize: '32px',
                        color: '#10b981',
                        marginBottom: '15px'
                      }}></i>
                      <h4 style={{
                        fontSize: '18px',
                        fontWeight: '600',
                        color: '#1f2937',
                        marginBottom: '10px'
                      }}>
                        Soporte Técnico
                      </h4>
                      <p style={{
                        fontSize: '15px',
                        color: '#6b7280',
                        margin: '0'
                      }}>
                        Asistencia con problemas técnicos
                      </p>
                    </div>

                    <div style={{
                      textAlign: 'center',
                      padding: '25px',
                      backgroundColor: '#f8fafc',
                      borderRadius: '12px'
                    }}>
                      <i className="fa fa-question-circle" style={{
                        fontSize: '32px',
                        color: '#f59e0b',
                        marginBottom: '15px'
                      }}></i>
                      <h4 style={{
                        fontSize: '18px',
                        fontWeight: '600',
                        color: '#1f2937',
                        marginBottom: '10px'
                      }}>
                        Consultas
                      </h4>
                      <p style={{
                        fontSize: '15px',
                        color: '#6b7280',
                        margin: '0'
                      }}>
                        Preguntas sobre acceso y uso
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Mapa o Información Adicional */}
      <div style={{padding: '60px 0', backgroundColor: '#ffffff'}}>
        <div className="container">
          <div className="row">
            <div className="col-xl-12">
              <div style={{
                maxWidth: '800px',
                margin: '0 auto',
                textAlign: 'center',
                padding: '40px',
                backgroundColor: '#f8fafc',
                borderRadius: '16px'
              }}>
                <h3 style={{
                  fontSize: '24px',
                  fontWeight: '600',
                  color: '#1f2937',
                  marginBottom: '20px'
                }}>
                  Ubicación
                </h3>
                <p style={{
                  fontSize: '16px',
                  color: '#4b5563',
                  lineHeight: '1.8',
                  marginBottom: '30px'
                }}>
                  Nuestras oficinas están ubicadas en el centro de Montevideo, en Liniers 1324 piso 4. 
                  Estamos disponibles para atenderte de lunes a viernes de 9:00 a 17:00 horas.
                </p>
                <div style={{
                  padding: '20px',
                  backgroundColor: '#ffffff',
                  borderRadius: '12px',
                  border: '2px solid #e5e7eb',
                  display: 'inline-block'
                }}>
                  <p style={{
                    fontSize: '18px',
                    fontWeight: '600',
                    color: '#1f2937',
                    margin: '0'
                  }}>
                    📍 Liniers 1324 piso 4, Montevideo, Uruguay
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Contacto;


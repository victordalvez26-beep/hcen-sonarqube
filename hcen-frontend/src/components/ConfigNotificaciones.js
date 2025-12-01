import React, { useState, useEffect } from 'react';
import config from '../config';
import GenericPopup from './GenericPopup';

const ConfigNotificaciones = ({ show, onClose }) => {
  const [preferences, setPreferences] = useState({
    notifyResults: true,
    notifyNewAccessRequest: true,
    notifyMedicalHistory: true,
    notifyNewAccessHistory: true,
    notifyMaintenance: true,
    notifyNewFeatures: true,
    allDisabled: false
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ show: false, text: '', type: 'info' });

  // Cargar preferencias al abrir el modal
  useEffect(() => {
    if (show) {
      loadPreferences();
    }
  }, [show]);

  const loadPreferences = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${config.BACKEND_URL}/api/notifications/preferences`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        setPreferences(data);
      } else {
        setMessage({ show: true, text: 'Error al cargar las preferencias de notificaciones', type: 'error' });
      }
    } catch (error) {
      console.error('Error cargando preferencias:', error);
      setMessage({ show: true, text: 'Error de conexión al cargar preferencias', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = (key) => {
    if (key === 'allDisabled') {
      // Si se activa "Desactivar todas", desactivar todas las demás
      const newValue = !preferences.allDisabled;
      setPreferences({
        ...preferences,
        allDisabled: newValue,
        notifyResults: !newValue,
        notifyNewAccessRequest: !newValue,
        notifyMedicalHistory: !newValue,
        notifyNewAccessHistory: !newValue,
        notifyMaintenance: !newValue,
        notifyNewFeatures: !newValue
      });
    } else {
      // Si se activa cualquier otra opción, desactivar "Desactivar todas"
      setPreferences({
        ...preferences,
        [key]: !preferences[key],
        allDisabled: false
      });
    }
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      const response = await fetch(`${config.BACKEND_URL}/api/notifications/preferences`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(preferences)
      });

      const data = await response.json();

      if (response.ok && data.success) {
        setMessage({ show: true, text: data.message || 'Preferencias actualizadas correctamente', type: 'success' });
        setTimeout(() => {
          onClose();
        }, 1500);
      } else {
        setMessage({ show: true, text: data.message || 'Error al actualizar las preferencias', type: 'error' });
      }
    } catch (error) {
      console.error('Error guardando preferencias:', error);
      setMessage({ show: true, text: 'Error de conexión al guardar preferencias', type: 'error' });
    } finally {
      setSaving(false);
    }
  };

  if (!show) return null;

  const notificationOptions = [
    {
      key: 'notifyResults',
      title: 'Notificación de resultados',
      description: 'Cuando los resultados de laboratorio o de imágenes (radiografías, ecografías, etc.) estén disponibles.'
    },
    {
      key: 'notifyNewAccessRequest',
      title: 'Nuevo pedido de acceso',
      description: 'Cuando un profesional o institución solicita permiso para acceder a tu información médica.'
    },
    {
      key: 'notifyMedicalHistory',
      title: 'Actualizaciones en el historial médico',
      description: 'Cuando se añaden nuevos documentos, diagnósticos o notas en tu historial médico.'
    },
    {
      key: 'notifyNewAccessHistory',
      title: 'Nuevo acceso a historia clínica',
      description: 'Cuando un profesional o institución obtiene acceso a tu historia clínica.'
    },
    {
      key: 'notifyMaintenance',
      title: 'Mantenimiento del sistema',
      description: 'Cuando la app o el sistema va a estar fuera de servicio temporalmente por mantenimiento.'
    },
    {
      key: 'notifyNewFeatures',
      title: 'Nuevas funciones',
      description: 'Avisos sobre mejoras, nuevas características o correcciones de errores en la app.'
    },
    {
      key: 'allDisabled',
      title: 'Desactivar todas',
      description: 'Desactiva todas las notificaciones de una vez.'
    }
  ];

  return (
    <div 
      className="generic-popup-overlay" 
      onClick={onClose}
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 10000
      }}
    >
      <div 
        className="generic-popup-content"
        onClick={(e) => e.stopPropagation()}
        style={{
          backgroundColor: '#ffffff',
          borderRadius: '12px',
          padding: '30px',
          maxWidth: '600px',
          width: '90%',
          maxHeight: '90vh',
          overflowY: 'auto',
          boxShadow: '0 10px 40px rgba(0, 0, 0, 0.2)'
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '25px' }}>
          <h2 style={{ margin: 0, color: '#1f2937', fontSize: '24px', fontWeight: '700' }}>
            Configuración de Notificaciones
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '24px',
              color: '#6b7280',
              cursor: 'pointer',
              padding: '0',
              width: '30px',
              height: '30px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <i className="fa fa-times"></i>
          </button>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <i className="fa fa-spinner fa-spin" style={{ fontSize: '32px', color: '#3b82f6' }}></i>
            <p style={{ marginTop: '15px', color: '#6b7280' }}>Cargando preferencias...</p>
          </div>
        ) : (
          <>
            <div style={{ marginBottom: '25px' }}>
              {notificationOptions.map((option) => (
                <div
                  key={option.key}
                  style={{
                    padding: '20px',
                    marginBottom: '15px',
                    backgroundColor: '#f8fafc',
                    borderRadius: '8px',
                    border: '1px solid #e5e7eb'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1, marginRight: '15px' }}>
                      <h3 style={{
                        margin: '0 0 8px 0',
                        color: '#1f2937',
                        fontSize: '16px',
                        fontWeight: '600'
                      }}>
                        {option.title}
                      </h3>
                      <p style={{
                        margin: 0,
                        color: '#6b7280',
                        fontSize: '14px',
                        lineHeight: '1.5'
                      }}>
                        {option.description}
                      </p>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                      <label
                        style={{
                          position: 'relative',
                          display: 'inline-block',
                          width: '50px',
                          height: '28px',
                          cursor: 'pointer'
                        }}
                      >
                        <input
                          type="checkbox"
                          checked={preferences[option.key]}
                          onChange={() => handleToggle(option.key)}
                          style={{ display: 'none' }}
                          disabled={saving}
                        />
                        <span
                          style={{
                            position: 'absolute',
                            top: 0,
                            left: 0,
                            right: 0,
                            bottom: 0,
                            backgroundColor: preferences[option.key] ? '#3b82f6' : '#d1d5db',
                            borderRadius: '14px',
                            transition: 'background-color 0.3s',
                            cursor: saving ? 'not-allowed' : 'pointer'
                          }}
                        >
                          <span
                            style={{
                              position: 'absolute',
                              top: '2px',
                              left: preferences[option.key] ? '24px' : '2px',
                              width: '24px',
                              height: '24px',
                              backgroundColor: '#ffffff',
                              borderRadius: '50%',
                              transition: 'left 0.3s',
                              boxShadow: '0 2px 4px rgba(0, 0, 0, 0.2)'
                            }}
                          />
                        </span>
                      </label>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '25px' }}>
              <button
                onClick={onClose}
                disabled={saving}
                style={{
                  padding: '10px 20px',
                  backgroundColor: '#f3f4f6',
                  color: '#374151',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: saving ? 'not-allowed' : 'pointer',
                  fontWeight: '600',
                  fontSize: '14px'
                }}
              >
                Cancelar
              </button>
              <button
                onClick={handleSave}
                disabled={saving}
                style={{
                  padding: '10px 20px',
                  backgroundColor: saving ? '#9ca3af' : '#3b82f6',
                  color: '#ffffff',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: saving ? 'not-allowed' : 'pointer',
                  fontWeight: '600',
                  fontSize: '14px'
                }}
              >
                {saving ? (
                  <>
                    <i className="fa fa-spinner fa-spin" style={{ marginRight: '8px' }}></i>
                    Guardando...
                  </>
                ) : (
                  'Guardar'
                )}
              </button>
            </div>
          </>
        )}

        {message.show && (
          <GenericPopup
            show={message.show}
            onClose={() => setMessage({ ...message, show: false })}
            message={message.text}
            type={message.type === 'success' ? 'success' : message.type === 'error' ? 'error' : 'info'}
          />
        )}
      </div>
    </div>
  );
};

export default ConfigNotificaciones;


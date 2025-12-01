import React from 'react';
import './GenericPopup.css';

const GenericPopup = ({ 
  show, 
  onClose, 
  title, 
  message, 
  type = 'info', // 'info', 'success', 'warning', 'error'
  showConfirm = false,
  onConfirm,
  confirmText = 'Confirmar',
  cancelText = 'Cancelar',
  confirmColor = '#3b82f6',
  showIcon = true
}) => {
  if (!show) return null;

  const getIcon = () => {
    if (!showIcon) return null;
    
    switch (type) {
      case 'success':
        return (
          <div className="generic-popup-icon generic-popup-icon-success">
            <i className="fa fa-check-circle"></i>
          </div>
        );
      case 'error':
        return (
          <div className="generic-popup-icon generic-popup-icon-error">
            <i className="fa fa-exclamation-circle"></i>
          </div>
        );
      case 'warning':
        return (
          <div className="generic-popup-icon generic-popup-icon-warning">
            <i className="fa fa-exclamation-triangle"></i>
          </div>
        );
      default:
        return (
          <div className="generic-popup-icon generic-popup-icon-info">
            <i className="fa fa-info-circle"></i>
          </div>
        );
    }
  };

  const handleConfirm = () => {
    if (onConfirm) {
      onConfirm();
    }
    onClose();
  };

  return (
    <div className="generic-popup-overlay" onClick={!showConfirm ? onClose : undefined}>
      <div 
        className={`generic-popup-content generic-popup-${type}`}
        onClick={(e) => e.stopPropagation()}
      >
        <button 
          className="generic-popup-close" 
          onClick={onClose}
          aria-label="Cerrar"
        >
          <i className="fa fa-times"></i>
        </button>
        
        {getIcon()}
        
        {title && (
          <h3 className="generic-popup-title">{title}</h3>
        )}
        
        <div className="generic-popup-message">
          {message}
        </div>
        
        <div className="generic-popup-actions">
          {showConfirm ? (
            <>
              <button 
                className="generic-popup-button generic-popup-button-cancel"
                onClick={onClose}
              >
                {cancelText}
              </button>
              <button 
                className="generic-popup-button generic-popup-button-confirm"
                onClick={handleConfirm}
                style={{ backgroundColor: confirmColor }}
              >
                {confirmText}
              </button>
            </>
          ) : (
            <button 
              className="generic-popup-button generic-popup-button-ok"
              onClick={onClose}
              style={{ backgroundColor: confirmColor }}
            >
              Entendido
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default GenericPopup;


package uy.edu.tse.hcen.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import uy.edu.tse.hcen.model.User;
import uy.edu.tse.hcen.model.Nacionalidad;
import uy.edu.tse.hcen.model.Rol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Stateless
public class UserDAO {
    
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    
    @PersistenceContext(unitName = "hcenPersistenceUnit")
    private EntityManager em;
    
    // Guarda o actualiza un usuario
    public User saveOrUpdate(User user) {
        User existingUser = findByUid(user.getUid());
        if (existingUser != null) {
            // CRÍTICO: Leer el rol más reciente de la BD ANTES de hacer cualquier actualización
            // Esto asegura que el rol no se pierda si fue actualizado recientemente
            Rol rolActualBD = getRolFromDatabase(user.getUid());
            if (rolActualBD != null) {
                existingUser.setRol(rolActualBD);
                LOGGER.info("Rol leído desde BD antes de actualizar: " + rolActualBD.getCodigo());
            }
            
            // Actualizar campos (pero NO el rol - el campo tiene updatable=false)
            existingUser.setEmail(user.getEmail());
            existingUser.setPrimerNombre(user.getPrimerNombre());
            existingUser.setSegundoNombre(user.getSegundoNombre());
            existingUser.setPrimerApellido(user.getPrimerApellido());
            existingUser.setSegundoApellido(user.getSegundoApellido());
            existingUser.setTipDocum(user.getTipDocum());
            existingUser.setCodDocum(user.getCodDocum());
            
            if (user.getNacionalidad() != null) {
                existingUser.setNacionalidad(user.getNacionalidad());
            }
            if (user.getFechaNacimiento() != null) {
                existingUser.setFechaNacimiento(user.getFechaNacimiento());
            }
            if (user.getDepartamento() != null) {
                existingUser.setDepartamento(user.getDepartamento());
            }
            if (user.getLocalidad() != null && !user.getLocalidad().isEmpty()) {
                existingUser.setLocalidad(user.getLocalidad());
            }
            if (user.getDireccion() != null && !user.getDireccion().isEmpty()) {
                existingUser.setDireccion(user.getDireccion());
            }
            if (user.getTelefono() != null && !user.getTelefono().isEmpty()) {
                existingUser.setTelefono(user.getTelefono());
            }
            if (user.getCodigoPostal() != null && !user.getCodigoPostal().isEmpty()) {
                existingUser.setCodigoPostal(user.getCodigoPostal());
            }
            if (user.isProfileCompleted()) {
                existingUser.setProfileCompleted(true);
            }
            
            // Hacer merge (el rol no se actualizará porque tiene updatable=false)
            User updatedUser = em.merge(existingUser);
            
            // Forzar flush
            em.flush();
            
            // Refrescar para leer el rol correcto de la BD
            em.refresh(updatedUser);
            
            LOGGER.info("Usuario actualizado: " + user.getUid() + " - Rol final (desde BD): " + (updatedUser.getRol() != null ? updatedUser.getRol().getCodigo() : "null"));
            return updatedUser;
        } else {
            em.persist(user);
            LOGGER.info("Usuario creado: " + user.getUid());
            return user;
        }
    }
    
    // Busca un usuario por UID
    public User findByUid(String uid) {
        try {
            // CRÍTICO: Limpiar completamente el cache de JPA antes de leer
            // Esto asegura que siempre leamos el valor más reciente de la BD
            em.clear(); // Limpia todo el cache del EntityManager
            
            // Leer el rol directamente de la BD usando query nativa
            String rolRaw = null;
            try {
                rolRaw = (String) em.createNativeQuery(
                        "SELECT rol FROM users WHERE uid = :uid")
                        .setParameter("uid", uid)
                        .getSingleResult();
                LOGGER.info("Usuario encontrado por UID: " + uid + " - Rol en BD (raw): '" + rolRaw + "'");
            } catch (Exception e) {
                LOGGER.warning("No se pudo obtener el rol crudo de la BD para UID: " + uid + " - " + e.getMessage());
            }
            
            // Buscar el usuario usando JPQL (después de limpiar el cache)
            User user = em.createQuery(
                    "SELECT u FROM User u WHERE u.uid = :uid", 
                    User.class)
                    .setParameter("uid", uid)
                    .getSingleResult();
            
            // CRÍTICO: Si el rol en la entidad no coincide con el raw de la BD, 
            // forzar un refresh y establecer el rol correcto manualmente
            if (rolRaw != null) {
                Rol rolCorrecto = Rol.fromCodigo(rolRaw);
                if (rolCorrecto != null && (user.getRol() == null || !user.getRol().getCodigo().equals(rolRaw))) {
                    LOGGER.warning("Inconsistencia detectada: rol en BD='" + rolRaw + "', rol en entidad='" + 
                            (user.getRol() != null ? user.getRol().getCodigo() : "null") + "'. Corrigiendo...");
                    // Forzar refresh desde la BD
                    em.refresh(user);
                    // Si después del refresh aún no coincide, establecerlo manualmente
                    if (user.getRol() == null || !user.getRol().getCodigo().equals(rolRaw)) {
                        user.setRol(rolCorrecto);
                        LOGGER.info("Rol corregido manualmente: " + rolCorrecto.getCodigo());
                    }
                }
            }
            
            LOGGER.info("Usuario encontrado por UID: " + uid + " - Rol final: " + (user.getRol() != null ? user.getRol().getCodigo() : "null"));
            return user;
        } catch (NoResultException e) {
            return null;
        }
    }
    
    // Busca un usuario por email
    public User findByEmail(String email) {
        try {
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", 
                    User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    // Busca un usuario por documento (CI)
    public User findByDocumento(String codDocum) {
        try {
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.codDocum = :codDocum", 
                    User.class)
                    .setParameter("codDocum", codDocum)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    // Persiste un nuevo usuario
    public void persist(User user) {
        em.persist(user);
        LOGGER.info("Usuario persistido con ID: " + user.getId());
    }
    
    // Actualiza un usuario existente
    public User merge(User user) {
        User updated = em.merge(user);
        LOGGER.info("Usuario actualizado con ID: " + updated.getId());
        return updated;
    }
    
    // Elimina un usuario por UID
    public boolean deleteByUid(String uid) {
        try {
            User user = findByUid(uid);
            if (user != null) {
                em.remove(user);
                LOGGER.info("Usuario eliminado: " + uid);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.severe("Error eliminando usuario: " + e.getMessage());
            return false;
        }
    }

    // Obtiene todos los usuarios
    public List<User> findAll() {
        try {
            return em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo todos los usuarios: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Actualiza el rol de un usuario
    public boolean updateUserRole(String uid, Rol newRole) {
        try {
            // CRÍTICO: Limpiar el cache antes de actualizar
            em.clear();
            
            // Primero hacer un update directo en la BD para asegurar que se persista
            int updated = em.createNativeQuery(
                    "UPDATE users SET rol = :rol WHERE uid = :uid")
                    .setParameter("rol", newRole.getCodigo())
                    .setParameter("uid", uid)
                    .executeUpdate();
            
            if (updated > 0) {
                // Hacer flush para asegurar que el cambio se persista
                em.flush();
                
                // Limpiar el cache nuevamente después de la actualización
                em.clear();
                
                // Verificar que el cambio se haya persistido correctamente
                String rolVerificado = (String) em.createNativeQuery(
                        "SELECT rol FROM users WHERE uid = :uid")
                        .setParameter("uid", uid)
                        .getSingleResult();
                
                LOGGER.info("Rol actualizado para usuario: " + uid + " -> " + newRole.getCodigo() + 
                        " (filas actualizadas: " + updated + ", verificado en BD: '" + rolVerificado + "')");
                
                if (!newRole.getCodigo().equals(rolVerificado)) {
                    LOGGER.severe("ERROR: El rol en la BD no coincide con el esperado. Esperado: " + 
                            newRole.getCodigo() + ", Encontrado: " + rolVerificado);
                }
                
                return true;
            }
            LOGGER.warning("No se actualizó ningún usuario con UID: " + uid);
            return false;
        } catch (Exception e) {
            LOGGER.severe("Error actualizando rol del usuario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Busca usuarios por rol
    public List<User> findByRol(String rolCodigo) {
        try {
            // Como rol es un enum convertido, necesitamos comparar directamente con el enum
            Rol rol = Rol.fromCodigo(rolCodigo);
            if (rol == null) {
                return new ArrayList<>();
            }
            return em.createQuery(
                    "SELECT u FROM User u WHERE u.rol = :rol ORDER BY u.primerNombre, u.primerApellido", 
                    User.class)
                    .setParameter("rol", rol)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("Error obteniendo usuarios por rol: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public long countUsuarios() {
        try {
            Long total = em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();
            return total != null ? total : 0L;
        } catch (Exception e) {
            LOGGER.severe("Error contando usuarios: " + e.getMessage());
            return 0L;
        }
    }

    public Map<String, Long> countUsuariosPorRol() {
        try {
            // Como rol es un enum convertido, necesitamos agrupar por el enum y luego extraer el código
            List<Object[]> resultados = em.createQuery(
                    "SELECT u.rol, COUNT(u) FROM User u GROUP BY u.rol", Object[].class)
                    .getResultList();

            Map<String, Long> conteo = new HashMap<>();
            for (Object[] row : resultados) {
                if (row[0] != null && row[0] instanceof Rol) {
                    Rol rol = (Rol) row[0];
                    conteo.put(rol.getCodigo(), row[1] != null ? ((Number) row[1]).longValue() : 0L);
                }
            }
            return conteo;
        } catch (Exception e) {
            LOGGER.severe("Error contando usuarios por rol: " + e.getMessage());
            return new java.util.HashMap<>();
        }
    }

    public long countUsuariosConPerfilCompleto(boolean completo) {
        try {
            Long total = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.profileCompleted = :completo",
                            Long.class)
                    .setParameter("completo", completo)
                    .getSingleResult();
            return total != null ? total : 0L;
        } catch (Exception e) {
            LOGGER.severe("Error contando usuarios por estado de perfil: " + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Obtiene el rol de un usuario directamente desde la base de datos usando una query nativa.
     * Útil para leer el rol sin pasar por el cache de JPA.
     */
    public Rol getRolFromDatabase(String uid) {
        try {
            // Forzar flush para asegurar que cualquier cambio pendiente se persista
            em.flush();
            
            // Leer directamente de la BD usando query nativa (no usa cache de JPA)
            String rolRaw = (String) em.createNativeQuery(
                    "SELECT rol FROM users WHERE uid = :uid")
                    .setParameter("uid", uid)
                    .getSingleResult();
            
            LOGGER.info("getRolFromDatabase - UID: " + uid + ", Rol raw: '" + rolRaw + "'");
            
            if (rolRaw != null && !rolRaw.isEmpty()) {
                Rol rol = Rol.fromCodigo(rolRaw);
                if (rol == null) {
                    LOGGER.warning("getRolFromDatabase - No se pudo convertir el código de rol: '" + rolRaw + "'");
                }
                return rol;
            }
            return null;
        } catch (Exception e) {
            LOGGER.warning("No se pudo obtener el rol de la BD para UID: " + uid + " - " + e.getMessage());
            return null;
        }
    }
}

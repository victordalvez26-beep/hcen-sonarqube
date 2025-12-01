-- Script SQL para actualizar el rol del usuario a Administrador
UPDATE users SET rol = 'AD' WHERE uid = 'uy-ci-52453518';

-- Verificar el cambio
SELECT id, uid, email, primer_nombre, primer_apellido, rol, nacionalidad, profile_completed 
FROM users 
WHERE uid = 'uy-ci-52453518';


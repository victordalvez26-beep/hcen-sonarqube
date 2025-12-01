package uy.edu.tse.hcen.model;

public enum Rol {
    USUARIO_SALUD("US", "Usuario de la Salud"),
    ADMIN_HCEN("AD", "Administrador HCEN");

    private final String codigo;
    private final String descripcion;

    Rol(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static Rol fromCodigo(String codigo) {
        if (codigo == null || codigo.isEmpty()) {
            return null;
        }
        for (Rol r : Rol.values()) {
            if (r.codigo.equalsIgnoreCase(codigo)) {
                return r;
            }
        }
        return null;
    }

    public static Rol getDefault() {
        return USUARIO_SALUD;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}

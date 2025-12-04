package uy.edu.tse.hcen.model;

public enum Department {
    ARTIGAS,
    CANELONES,
    CERRO_LARGO,
    COLONIA,
    DURAZNO,
    FLORES,
    FLORIDA,
    LAVALLEJA,
    MALDONADO,
    MONTEVIDEO,
    PAYSANDU,
    RIO_NEGRO,
    RIVERA,
    ROCHA,
    SALTO,
    SAN_JOSE,
    SORIANO,
    TACUAREMBO,
    TREINTA_Y_TRES;

    public String getDisplayName() {
        switch (this) {
            case CERRO_LARGO: return "Cerro Largo";
            case RIO_NEGRO: return "Río Negro";
            case SAN_JOSE: return "San José";
            case TREINTA_Y_TRES: return "Treinta y Tres";
            default:
                // Capitaliza y reemplaza guiones bajos por espacios
                String name = this.name().replace("_", " ");
                return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
    }

}
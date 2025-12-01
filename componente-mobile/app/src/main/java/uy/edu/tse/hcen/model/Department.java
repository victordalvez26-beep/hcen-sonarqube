package uy.edu.tse.hcen.model;

public enum Department {
    Artigas,
    Canelones,
    Cerro_Largo,
    Colonia,
    Durazno,
    Flores,
    Florida,
    Lavalleja,
    Maldonado,
    Montevideo,
    Paysandú,
    Río_Negro,
    Rivera,
    Rocha,
    Salto,
    San_José,
    Soriano,
    Tacuarembó,
    Treinta_y_Tres;

    public String getDisplayName() {
        switch (this) {
            case Cerro_Largo: return "Cerro Largo";
            case Río_Negro: return "Río Negro";
            case San_José: return "San José";
            case Treinta_y_Tres: return "Treinta y Tres";
            default:
                // Capitaliza y reemplaza guiones bajos por espacios
                String name = this.name().replace("_", " ");
                return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
    }

}
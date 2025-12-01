package uy.gub.agesic.inus.model;

public enum Departamento {
    ARTIGAS("Artigas"),
    CANELONES("Canelones"),
    CERRO_LARGO("Cerro Largo"),
    COLONIA("Colonia"),
    DURAZNO("Durazno"),
    FLORES("Flores"),
    FLORIDA("Florida"),
    LAVALLEJA("Lavalleja"),
    MALDONADO("Maldonado"),
    MONTEVIDEO("Montevideo"),
    PAYSANDU("Paysandú"),
    RIO_NEGRO("Río Negro"),
    RIVERA("Rivera"),
    ROCHA("Rocha"),
    SALTO("Salto"),
    SAN_JOSE("San José"),
    SORIANO("Soriano"),
    TACUAREMBO("Tacuarembó"),
    TREINTA_Y_TRES("Treinta y Tres");
    
    private final String nombre;
    
    Departamento(String nombre) {
        this.nombre = nombre;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}


package uy.edu.tse.hcen.model;

public enum Nacionalidad {
    UY("Uruguay"),
    AR("Argentina"),
    BR("Brasil"),
    CL("Chile"),
    PY("Paraguay"),
    BO("Bolivia"),
    PE("Perú"),
    EC("Ecuador"),
    CO("Colombia"),
    VE("Venezuela"),
    US("Estados Unidos"),
    CA("Canadá"),
    ES("España"),
    IT("Italia"),
    FR("Francia"),
    DE("Alemania"),
    PT("Portugal"),
    GB("Reino Unido"),
    CN("China"),
    JP("Japón"),
    IN("India"),
    ZA("Sudáfrica"),
    AU("Australia"),
    NZ("Nueva Zelanda"),
    MX("México"),
    CU("Cuba"),
    DO("República Dominicana"),
    SV("El Salvador"),
    GT("Guatemala"),
    HN("Honduras"),
    NI("Nicaragua"),
    CR("Costa Rica"),
    PA("Panamá"),
    PR("Puerto Rico"),
    AD("Andorra"),
    BE("Bélgica"),
    CH("Suiza"),
    DK("Dinamarca"),
    FI("Finlandia"),
    GR("Grecia"),
    IE("Irlanda"),
    IS("Islandia"),
    LU("Luxemburgo"),
    NL("Países Bajos"),
    NO("Noruega"),
    SE("Suecia"),
    AT("Austria"),
    PL("Polonia"),
    RU("Rusia"),
    TR("Turquía"),
    UA("Ucrania"),
    EG("Egipto"),
    MA("Marruecos"),
    NG("Nigeria"),
    KE("Kenia"),
    ET("Etiopía"),
    SA("Arabia Saudí"),
    AE("Emiratos Árabes Unidos"),
    QA("Catar"),
    KR("Corea del Sur"),
    TH("Tailandia"),
    VN("Vietnam"),
    ID("Indonesia"),
    MY("Malasia"),
    PH("Filipinas"),
    SG("Singapur"),
    OT("Otros");

    private final String nombre;

    Nacionalidad(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return this.name();
    }

    public static Nacionalidad fromCodigo(String codigo) {
        if (codigo == null || codigo.isEmpty()) {
            return null;
        }
        for (Nacionalidad n : Nacionalidad.values()) {
            if (n.name().equalsIgnoreCase(codigo)) {
                return n;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
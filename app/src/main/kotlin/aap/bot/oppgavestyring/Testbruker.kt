package aap.bot.oppgavestyring

enum class Testbruker(val epost: String, val ident: String) {
    VEILEDER_GAMLEOSLO_NAVKONTOR("f_Z994559.e_Z994559@trygdeetaten.no", "Z994559"), // Lokalkontor
    SAKSBEHANDLER("f_Z994554.e_Z994554@trygdeetaten.no", "Z994554"), // NAY
    SAKSBEHANDLER_STRENGT_FORTROLIG("f_Z994553.e_Z994553@trygdeetaten.no", "Z994553"),
    FATTER("f_Z994439.e_Z994439@trygdeetaten.no", "Z994439"), // Kvalitetssikrer lokalkontor
    BESLUTTER("f_Z994524.e_Z994524@trygdeetaten.no", "Z994524"), // Kvalitetssikrer NAY
    SAKSBEHANDLER_FORTROLIG("f_Z994169.e_Z994169@trygdeetaten.no", "Z994169"),
    SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR("f_Z990203.e_Z990203@trygdeetaten.no", "Z990203"),
    BESLUTTER_OG_FATTER_ALLE_NAVKONTOR("f_Z990252.e_Z990252@trygdeetaten.no", "Z990252"),
}

package bot.oppgavestyring

enum class Testbruker(val epost: String) {
    VEILEDER_GAMLEOSLO_NAVKONTOR("f_Z994559.e_Z994559@trygdeetaten.no"), // Lokalkontor
    SAKSBEHANDLER("f_Z994554.e_Z994554@trygdeetaten.no"), // NAY
    SAKSBEHANDLER_STRENGT_FORTROLIG("f_Z994553.e_Z994553@trygdeetaten.no"),
    FATTER("f_Z994439.e_Z994439@trygdeetaten.no"), // Kvalitetssikrer lokalkontor
    BESLUTTER("f_Z994524.e_Z994524@trygdeetaten.no"), // Kvalitetssikrer NAY
    SAKSBEHANDLER_FORTROLIG("f_Z994169.e_Z994169@trygdeetaten.no"), // fikk ikke skrudd av 2FA i #tech-azure
    SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR("f_Z990203.e_Z990203@trygdeetaten.no"),
    BESLUTTER_OG_FATTER_ALLE_NAVKONTOR("f_Z990252.e_Z990252@trygdeetaten.no"),
}

package aap.bot.oppgavestyring

import java.time.LocalDateTime
import java.util.*

data class Kvalitetssikring(
    val løsningId: UUID,
    val kvalitetssikretAv: String,
    val tidspunktForKvalitetssikring: LocalDateTime,
    val erGodkjent: Boolean,
    val begrunnelse: String
) {
    companion object {
        fun pathsForNAY() = listOf(
            "kvalitetssikre/paragraf_11_2",
            "kvalitetssikre/paragraf_11_3",
            "kvalitetssikre/paragraf_11_4_ledd2Og3",
            "kvalitetssikre/paragraf_11_6",
            "kvalitetssikre/paragraf_11_19",
            "kvalitetssikre/paragraf_22_13",
        )

        fun pathsForLokalkontor() = listOf(
            "kvalitetssikre/paragraf_11_5"
        )

        fun godkjent() = Kvalitetssikring(
            løsningId = UUID.randomUUID(),
            kvalitetssikretAv = "Z999999",
            tidspunktForKvalitetssikring = LocalDateTime.now(),
            erGodkjent = true,
            begrunnelse = "Godkjent"
        )
    }
}

package aap.bot.oppgavestyring

import java.util.*

data class Kvalitetssikringer(
    val path: String,
    val data: () -> Any,
) {
    companion object {
        fun lokalkontor() = listOf(
            Kvalitetssikringer("kvalitetssikre/paragraf_11_5") {
                Kvalitetssikring_11_5(
                    løsningId = UUID.randomUUID(),
                    kravOmNedsattArbeidsevneErGodkjent = true,
                    kravOmNedsattArbeidsevneErGodkjentBegrunnelse = "Godkjent",
                    nedsettelseSkyldesSykdomEllerSkadeErGodkjent = true,
                    nedsettelseSkyldesSykdomEllerSkadeErGodkjentBegrunnelse = "Godkjent"
                )
            }
        )

        fun nay() = listOf(
            Kvalitetssikringer("kvalitetssikre/paragraf_11_2") {
                Kvalitetssikring_11_2(
                    løsningId = UUID.randomUUID(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            },
            Kvalitetssikringer("kvalitetssikre/paragraf_11_3") {
                Kvalitetssikring_11_3(
                    løsningId = UUID.randomUUID(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            },
            Kvalitetssikringer("kvalitetssikre/paragraf_11_4_ledd2Og3") {
                Kvalitetssikring_11_4_ledd2og3(
                    løsningId = UUID.randomUUID(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            },
            Kvalitetssikringer("kvalitetssikre/paragraf_11_6") {
                Kvalitetssikring_11_6(
                    løsningId = UUID.randomUUID(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            },
            Kvalitetssikringer("kvalitetssikre/paragraf_11_19") {
                Kvalitetssikring_11_19(
                    løsningId = UUID.randomUUID(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            },
            Kvalitetssikringer("kvalitetssikre/paragraf_22_13") {
                Kvalitetssikring_22_13(
                    løsningId = UUID.randomUUID(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            }
        )
    }
}

internal data class Kvalitetssikring_11_5(
    val løsningId: UUID,
    val kravOmNedsattArbeidsevneErGodkjent: Boolean,
    val kravOmNedsattArbeidsevneErGodkjentBegrunnelse: String?,
    val nedsettelseSkyldesSykdomEllerSkadeErGodkjent: Boolean,
    val nedsettelseSkyldesSykdomEllerSkadeErGodkjentBegrunnelse: String?,
)

internal data class Kvalitetssikring_11_2(
    val løsningId: UUID,
    val erGodkjent: Boolean,
    val begrunnelse: String?,
)

internal data class Kvalitetssikring_11_3(
    val løsningId: UUID,
    val erGodkjent: Boolean,
    val begrunnelse: String?,
)

internal data class Kvalitetssikring_11_4_ledd2og3(
    val løsningId: UUID,
    val erGodkjent: Boolean,
    val begrunnelse: String?,
)

internal data class Kvalitetssikring_11_6(
    val løsningId: UUID,
    val erGodkjent: Boolean,
    val begrunnelse: String?,
)

internal data class Kvalitetssikring_11_19(
    val løsningId: UUID,
    val erGodkjent: Boolean,
    val begrunnelse: String?,
)

internal data class Kvalitetssikring_22_13(
    val løsningId: UUID,
    val erGodkjent: Boolean,
    val begrunnelse: String?,
)

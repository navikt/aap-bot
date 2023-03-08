package bot.oppgavestyring

import java.util.*

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

package bot.oppgavestyring

import java.time.LocalDate

internal data class Inngangsvilkår(
    val løsning_11_2: Løsning_11_2,
    val løsning_11_3: Løsning_11_3,
    val løsning_11_4: Løsning_11_4,
) {
    internal data class Løsning_11_2(val erMedlem: String)
    internal data class Løsning_11_3(val erOppfylt: Boolean)
    internal data class Løsning_11_4(val erOppfylt: Boolean)
}

internal data class Løsning_11_5(
    val kravOmNedsattArbeidsevneErOppfylt: Boolean,
    val kravOmNedsattArbeidsevneErOppfyltBegrunnelse: String,
    val nedsettelseSkyldesSykdomEllerSkade: Boolean,
    val nedsettelseSkyldesSykdomEllerSkadeBegrunnelse: String,
    val kilder: List<String>,
    val legeerklæringDato: LocalDate?,
    val sykmeldingDato: LocalDate?,
)

internal data class Innstilling_11_6(
    val harBehovForBehandling: Boolean,
    val harBehovForTiltak: Boolean,
    val harMulighetForÅKommeIArbeid: Boolean,
    val individuellBegrunnelse: String?,
)

internal data class Løsning_11_6(
    val harBehovForBehandling: Boolean,
    val harBehovForTiltak: Boolean,
    val harMulighetForÅKommeIArbeid: Boolean,
    val individuellBegrunnelse: String?,
)

internal data class Løsning_11_19(
    val beregningsdato: LocalDate,
    val grunnForDato: String,
)

internal data class Løsning_22_13(
    val bestemmesAv: String,
    val unntak: String?,
    val unntaksbegrunnelse: String?,
    val manueltSattVirkningsdato: LocalDate?,
    val begrunnelseForAnnet: String?,
)

package aap.bot.oppgavestyring

import java.time.LocalDate

data class Løsninger(
    val path: String,
    val data: () -> Any
) {
    companion object {
        // TODO: Skal vi bare svarer på 11_2 dersom LovMe svarer UAVKLART, eller kan man ALLTID svare JA
        fun inngangsvilkår() = listOf(
            Løsninger("losning/inngangsvilkar") {
                Inngangsvilkår(
                    Inngangsvilkår.Løsning_11_2("JA"),
                    Inngangsvilkår.Løsning_11_3(true),
                    Inngangsvilkår.Løsning_11_4(true)
                )
            }
        )

        fun fraLokalkontor() = listOf(
            Løsninger("losning/paragraf_11_5") {
                Løsning_11_5(
                    kravOmNedsattArbeidsevneErOppfylt = true,
                    nedsettelseSkyldesSykdomEllerSkade = true,
                    kilder = listOf(),
                    legeerklæringDato = null,
                    sykmeldingDato = null,
                    kravOmNedsattArbeidsevneErOppfyltBegrunnelse = "fritekst",
                    nedsettelseSkyldesSykdomEllerSkadeBegrunnelse = "fritekst"
                )
            },
            Løsninger("innstilling/paragraf_11_6") {
                Innstilling_11_6(
                    harBehovForBehandling = true,
                    harBehovForTiltak = true,
                    harMulighetForÅKommeIArbeid = true,
                    individuellBegrunnelse = null,
                )
            }
        )

        fun resten() = listOf(
            Løsninger("losning/paragraf_11_6") {

                Løsning_11_6(
                    harBehovForBehandling = true,
                    harBehovForTiltak = true,
                    harMulighetForÅKommeIArbeid = true,
                    individuellBegrunnelse = null,
                )
            },
            Løsninger("losning/paragraf_11_19") {
                Løsning_11_19(
                    beregningsdato = LocalDate.now(),
                    grunnForDato = "tiden den er nå"
                )
            },
            Løsninger("losning/paragraf_22_13") {
                Løsning_22_13(
                    bestemmesAv = "soknadstidspunkt",
                    unntak = "unntak",
                    unntaksbegrunnelse = "NAV har gitt mangelfulle eller misvisende opplysninger",
                    manueltSattVirkningsdato = LocalDate.now(),
                    begrunnelseForAnnet = null
                )
            }
        )
    }
}

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

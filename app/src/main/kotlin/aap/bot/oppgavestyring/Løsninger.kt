package aap.bot.oppgavestyring

import no.nav.aap.dto.kafka.Løsning_11_19_manuell
import no.nav.aap.dto.kafka.Løsning_11_2_manuell
import no.nav.aap.dto.kafka.Løsning_11_3_manuell
import no.nav.aap.dto.kafka.Løsning_11_4_ledd2_ledd3_manuell
import no.nav.aap.dto.kafka.Løsning_11_5_manuell
import no.nav.aap.dto.kafka.Løsning_11_6_manuell
import no.nav.aap.dto.kafka.Løsning_22_13_manuell
import java.time.LocalDate
import java.time.LocalDateTime

data class Inngangsvilkår(
    val løsning_11_2: Løsning_11_2_manuell,
    val løsning_11_3: Løsning_11_3_manuell,
    val løsning_11_4: Løsning_11_4_ledd2_ledd3_manuell,
)

data class Løsninger(
    val path: String,
    val data: Any
) {
    companion object {
        // TODO: Skal vi bare svarer på 11_2 dersom LovMe svarer UAVKLART, eller kan man ALLTID svare JA
        fun inngangsvilkår(vurdertAv: String) = listOf(
            Løsninger(
                path = "losning/inngangsvilkar",
                data = Inngangsvilkår(
                    løsning_11_2 = Løsning_11_2_manuell(
                        vurdertAv = vurdertAv,
                        tidspunktForVurdering = LocalDateTime.now(),
                        erMedlem = "JA",
                    ),
                    løsning_11_3 = Løsning_11_3_manuell(
                        vurdertAv = vurdertAv,
                        tidspunktForVurdering = LocalDateTime.now(),
                        erOppfylt = true,
                    ),
                    løsning_11_4 = Løsning_11_4_ledd2_ledd3_manuell(
                        vurdertAv = vurdertAv,
                        tidspunktForVurdering = LocalDateTime.now(),
                        erOppfylt = true,
                    )
                )
            )
        )

        fun fraLokalkontor(vurdertAv: String) = listOf(
            Løsninger(
                path = "losning/paragraf_11_5",
                data = Løsning_11_5_manuell(
                    vurdertAv = vurdertAv,
                    tidspunktForVurdering = LocalDateTime.now(),
                    kravOmNedsattArbeidsevneErOppfylt = true,
                    nedsettelseSkyldesSykdomEllerSkade = true,
                )
            ),
            Løsninger(
                path = "innstilling/paragraf_11_6",
                data = Løsning_11_6_manuell(
                    vurdertAv = vurdertAv,
                    tidspunktForVurdering = LocalDateTime.now(),
                    harBehovForBehandling = true,
                    harBehovForTiltak = true,
                    harMulighetForÅKommeIArbeid = true,
                )
            )
        )

        fun resten(vurdertAv: String) = listOf(
            Løsninger(
                path = "losning/paragraf_11_6",
                data = Løsning_11_6_manuell(
                    vurdertAv = vurdertAv,
                    tidspunktForVurdering = LocalDateTime.now(),
                    harBehovForBehandling = true,
                    harBehovForTiltak = true,
                    harMulighetForÅKommeIArbeid = true,
                )
            ),
            Løsninger(
                path = "losning/paragraf_11_19",
                data = Løsning_11_19_manuell(
                    vurdertAv = vurdertAv,
                    tidspunktForVurdering = LocalDateTime.now(),
                    beregningsdato = LocalDate.now(),
                )
            ),
            Løsninger(
                path = "losning/paragraf_22_13",
                data = Løsning_22_13_manuell(
                    vurdertAv = vurdertAv,
                    tidspunktForVurdering = LocalDateTime.now(),
                    bestemmesAv = "soknadstidspunkt",
                    unntak = "unntak",
                    unntaksbegrunnelse = "NAV har gitt mangelfulle eller misvisende opplysninger",
                    manueltSattVirkningsdato = LocalDate.now(),
                )
            )
        )
    }
}

package aap.bot.oppgavestyring

import no.nav.aap.dto.kafka.Løsning_11_19_manuellKafkaDto
import no.nav.aap.dto.kafka.Løsning_11_2_manuellKafkaDto
import no.nav.aap.dto.kafka.Løsning_11_3_manuellKafkaDto
import no.nav.aap.dto.kafka.Løsning_11_4_ledd2_ledd3_manuellKafkaDto
import no.nav.aap.dto.kafka.Løsning_11_5_manuellKafkaDto
import no.nav.aap.dto.kafka.Løsning_11_6_manuellKafkaDto
import no.nav.aap.dto.kafka.Løsning_22_13_manuellKafkaDto
import java.time.LocalDate
import java.time.LocalDateTime

data class Inngangsvilkår(
    val løsning_11_2: Løsning_11_2_manuellKafkaDto,
    val løsning_11_3: Løsning_11_3_manuellKafkaDto,
    val løsning_11_4: Løsning_11_4_ledd2_ledd3_manuellKafkaDto,
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
                    løsning_11_2 = Løsning_11_2_manuellKafkaDto(
                        vurdertAv = vurdertAv,
                        tidspunktForVurdering = LocalDateTime.now(),
                        erMedlem = "JA",
                    ),
                    løsning_11_3 = Løsning_11_3_manuellKafkaDto(
                        vurdertAv = vurdertAv,
                        tidspunktForVurdering = LocalDateTime.now(),
                        erOppfylt = true,
                    ),
                    løsning_11_4 = Løsning_11_4_ledd2_ledd3_manuellKafkaDto(
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
                data = Løsning_11_5_manuellKafkaDto(
                    vurdertAv = vurdertAv,
                    tidspunktForVurdering = LocalDateTime.now(),
                    kravOmNedsattArbeidsevneErOppfylt = true,
                    nedsettelseSkyldesSykdomEllerSkade = true,
                    kilder = listOf(),
                    legeerklæringDato = null,
                    sykmeldingDato = null,
                    kravOmNedsattArbeidsevneErOppfyltBegrunnelse = "fritekst",
                    nedsettelseSkyldesSykdomEllerSkadeBegrunnelse = "fritekst"
                )
            ),
            Løsninger(
                path = "innstilling/paragraf_11_6",
                data = Løsning_11_6_manuellKafkaDto(
                    vurdertAv = vurdertAv,
                    tidspunktForVurdering = LocalDateTime.now(),
                    harBehovForBehandling = true,
                    harBehovForTiltak = true,
                    harMulighetForÅKommeIArbeid = true,
                    individuellBegrunnelse = null,
                )
            )
        )

        fun resten(vurdertAv: String) = listOf(
            Løsninger(
                path = "losning/paragraf_11_6",
                data = Løsning_11_6_manuellKafkaDto(
                    vurdertAv = vurdertAv,
                    tidspunktForVurdering = LocalDateTime.now(),
                    harBehovForBehandling = true,
                    harBehovForTiltak = true,
                    harMulighetForÅKommeIArbeid = true,
                    individuellBegrunnelse = null,
                )
            ),
            Løsninger(
                path = "losning/paragraf_11_19",
                data = Løsning_11_19_manuellKafkaDto(
                    vurdertAv = vurdertAv,
                    tidspunktForVurdering = LocalDateTime.now(),
                    beregningsdato = LocalDate.now(),
                )
            ),
            Løsninger(
                path = "losning/paragraf_22_13",
                data = Løsning_22_13_manuellKafkaDto(
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

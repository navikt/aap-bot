package bot.streams

import bot.devtools.DevtoolsClient
import bot.oppgavestyring.*
import bot.produceSøknad
import bot.søknad.Søknader
import kotlinx.coroutines.runBlocking
import no.nav.aap.dto.kafka.SøkereKafkaDto
import no.nav.aap.dto.kafka.SøkereKafkaDtoHistorikk
import no.nav.aap.kafka.streams.v2.KStreams
import no.nav.aap.kafka.streams.v2.Topology
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.kafka.streams.v2.topology
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.random.Random

internal fun topology(
    oppgavestyring: OppgavestyringClient,
    devtools: DevtoolsClient,
    kafka: KStreams,
    config: StreamsConfig,
    testSøkere: List<String>,
): Topology = topology {
    consume(Topics.søkere)
        .filterKey { personident -> personident in testSøkere }
        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_3?.tilstand == AVVENTER_MANUELL_VURDERING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "losning/inngangsvilkar",
                    bruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR, // SAKSBEHANDLER
                    body = Inngangsvilkår(
                        løsning_11_2 = null,
                        løsning_11_3 = Inngangsvilkår.Løsning_11_3(true),
                        løsning_11_4 = null
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_2?.tilstand == AVVENTER_MANUELL_VURDERING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "losning/inngangsvilkar",
                    bruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR, // SAKSBEHANDLER
                    body = Inngangsvilkår(
                        løsning_11_2 = Inngangsvilkår.Løsning_11_2("JA"),
                        løsning_11_3 = Inngangsvilkår.Løsning_11_3(true),
                        løsning_11_4 = null,
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_4FørsteLedd?.tilstand == AVVENTER_MANUELL_VURDERING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "losning/inngangsvilkar",
                    bruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR, // SAKSBEHANDLER
                    body = Inngangsvilkår(
                        løsning_11_2 = null,
                        løsning_11_3 = Inngangsvilkår.Løsning_11_3(true),
                        løsning_11_4 = Inngangsvilkår.Løsning_11_4(true),
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_5?.tilstand == AVVENTER_MANUELL_VURDERING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "losning/paragraf_11_5",
                    bruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR, // VEILEDER_GAMLEOSLO_NAVKONTOR
                    body = Løsning_11_5(
                        kravOmNedsattArbeidsevneErOppfylt = true,
                        nedsettelseSkyldesSykdomEllerSkade = true,
                        kilder = listOf(),
                        legeerklæringDato = null,
                        sykmeldingDato = null,
                        kravOmNedsattArbeidsevneErOppfyltBegrunnelse = "fritekst",
                        nedsettelseSkyldesSykdomEllerSkadeBegrunnelse = "fritekst"
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_6?.tilstand == AVVENTER_INNSTILLING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "innstilling/paragraf_11_6",
                    bruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR, // VEILEDER_GAMLEOSLO_NAVKONTOR
                    body = Innstilling_11_6(
                        harBehovForBehandling = true,
                        harBehovForTiltak = true,
                        harMulighetForÅKommeIArbeid = true,
                        individuellBegrunnelse = null,
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_6?.tilstand == AVVENTER_MANUELL_VURDERING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "losning/paragraf_11_6",
                    bruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR, // SAKSBEHANDLER
                    body = Løsning_11_6(
                        harBehovForBehandling = true,
                        harBehovForTiltak = true,
                        harMulighetForÅKommeIArbeid = true,
                        individuellBegrunnelse = null,
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_19?.tilstand == AVVENTER_MANUELL_VURDERING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "losning/paragraf_11_19",
                    bruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR, // SAKSBEHANDLER
                    body = Løsning_11_19(
                        beregningsdato = LocalDate.now(),
                        grunnForDato = "tiden den er nå"
                    ),
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_22_13?.tilstand == AVVENTER_MANUELL_VURDERING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "losning/paragraf_22_13",
                    bruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR, // SAKSBEHANDLER
                    body = Løsning_22_13(
                        bestemmesAv = "soknadstidspunkt",
                        unntak = "unntak",
                        unntaksbegrunnelse = "NAV har gitt mangelfulle eller misvisende opplysninger",
                        manueltSattVirkningsdato = LocalDate.now(),
                        begrunnelseForAnnet = null
                    ),
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_2?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, dto ->
                val løsningId = dto.søkereKafkaDto.saker.single().sakstyper
                    .single { sakstype -> sakstype.paragraf_11_2 != null }
                    .paragraf_11_2!!
                    .totrinnskontroller
                    .first { totrinnskontroll -> totrinnskontroll.kvalitetssikring == null }
                    .løsning.løsningId

                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_2",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_11_2(
                        løsningId = løsningId,
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_3?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, dto ->
                val løsningId = dto.søkereKafkaDto.saker.single().sakstyper
                    .single { sakstype -> sakstype.paragraf_11_3 != null }
                    .paragraf_11_3!!
                    .totrinnskontroller
                    .first { totrinnskontroll -> totrinnskontroll.kvalitetssikring == null }
                    .løsning.løsningId

                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_3",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_11_3(
                        løsningId = løsningId,
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_4AndreOgTredjeLedd?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, dto ->
                val løsningId = dto.søkereKafkaDto.saker.single().sakstyper
                    .single { sakstype -> sakstype.paragraf_11_4AndreOgTredjeLedd != null }
                    .paragraf_11_4AndreOgTredjeLedd!!
                    .totrinnskontroller
                    .first { totrinnskontroll -> totrinnskontroll.kvalitetssikring == null }
                    .løsning.løsningId

                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_4_ledd2Og3",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_11_4_ledd2og3(
                        løsningId = løsningId,
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_5?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, dto ->
                val løsningId = dto.søkereKafkaDto.saker.single().sakstyper
                    .single { sakstype -> sakstype.paragraf_11_5 != null }
                    .paragraf_11_5!!
                    .totrinnskontroller
                    .first { totrinnskontroll -> totrinnskontroll.kvalitetssikring == null }
                    .løsning.løsningId

                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_5",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // FATTER
                    body = Kvalitetssikring_11_5(
                        løsningId = løsningId,
                        kravOmNedsattArbeidsevneErGodkjent = true,
                        kravOmNedsattArbeidsevneErGodkjentBegrunnelse = "Godkjent",
                        nedsettelseSkyldesSykdomEllerSkadeErGodkjent = true,
                        nedsettelseSkyldesSykdomEllerSkadeErGodkjentBegrunnelse = "Godkjent"
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_6?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, dto ->
                val løsningId = dto.søkereKafkaDto.saker.single().sakstyper
                    .single { sakstype -> sakstype.paragraf_11_6 != null }
                    .paragraf_11_6!!
                    .totrinnskontroller
                    .first { totrinnskontroll -> totrinnskontroll.kvalitetssikring == null }
                    .løsning.løsningId

                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_6",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_11_6(
                        løsningId = løsningId,
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_19?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, dto ->
                val løsningId = dto.søkereKafkaDto.saker.single().sakstyper
                    .single { sakstype -> sakstype.paragraf_11_19 != null }
                    .paragraf_11_19!!
                    .totrinnskontroller
                    .first { totrinnskontroll -> totrinnskontroll.kvalitetssikring == null }
                    .løsning.løsningId

                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_19",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_11_19(
                        løsningId = løsningId,
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }
        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_22_13?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, dto ->
                val løsningId = dto.søkereKafkaDto.saker.single().sakstyper
                    .single { sakstype -> sakstype.paragraf_22_13 != null }
                    .paragraf_22_13!!
                    .totrinnskontroller
                    .first { totrinnskontroll -> totrinnskontroll.kvalitetssikring == null }
                    .løsning.løsningId

                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_22_13",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_22_13(
                        løsningId = løsningId,
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }
        // Midlertidig fix for å hente sykepengedager på nytt

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_8_48?.tilstand == AVVENTER_MASKINELL_VURDERING }
        }) {
            it
                .map { _ -> "".toByteArray() }
                .produce(Topics.subscribeSykepengedager)
        }

        .branch({ dto ->
            dto.søkereKafkaDto.saker.any { sak -> sak.tilstand == VEDTAK_FATTET }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.iverksett(personident)
            }
        }

    consume(Topics.vedtak) { personident, vedtak, _ ->
        if (personident in testSøkere && vedtak != null) {
            secureLog.debug("Sletter $personident igjen pga vedtak")
            runBlocking {
                if (devtools.delete(personident)) {
                    Thread.sleep(5_000)
                    kafka.createProducer(config, Topics.søknad).use { producer ->
                        producer.produceSøknad(personident) {
                            Søknader.generell(LocalDate.now().minusYears(Random.nextLong(18, 62)))
                        }
                    }
                }
            }
        }
    }
}

private val secureLog = LoggerFactory.getLogger("secureLog")

private fun sjekkTilstand(
    dto: SøkereKafkaDtoHistorikk,
    sakstype: (SøkereKafkaDto.SakstypeKafkaDto) -> Boolean
): Boolean {
    return dto.søkereKafkaDto
        .saker.firstOrNull { it.tilstand == AVVENTER_VURDERING || it.tilstand == AVVENTER_KVALITETSSIKRING }
        ?.sakstyper?.filter { it.aktiv }
        ?.any(sakstype) ?: false
}

// tilstand på vilkår
const val IKKE_VURDERT = "IKKE_VURDERT"
const val AVVENTER_MASKINELL_VURDERING = "AVVENTER_MASKINELL_VURDERING"
const val AVVENTER_INNSTILLING = "AVVENTER_INNSTILLING"
const val AVVENTER_MANUELL_VURDERING = "AVVENTER_MANUELL_VURDERING"
const val OPPFYLT_MASKINELT_KVALITETSSIKRET = "OPPFYLT_MASKINELT_KVALITETSSIKRET"
const val IKKE_OPPFYLT_MASKINELT_KVALITETSSIKRET = "IKKE_OPPFYLT_MASKINELT_KVALITETSSIKRET"
const val OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING = "OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING"
const val OPPFYLT_MANUELT_KVALITETSSIKRET = "OPPFYLT_MANUELT_KVALITETSSIKRET"
const val IKKE_OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING = "IKKE_OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING"
const val IKKE_OPPFYLT_MANUELT_KVALITETSSIKRET = "IKKE_OPPFYLT_MANUELT_KVALITETSSIKRET"
const val IKKE_RELEVANT = "IKKE_RELEVANT"

// tilstand på sak
const val START = "START"
const val AVVENTER_VURDERING = "AVVENTER_VURDERING"
const val BEREGN_INNTEKT = "BEREGN_INNTEKT"
const val AVVENTER_KVALITETSSIKRING = "AVVENTER_KVALITETSSIKRING"
const val VEDTAK_FATTET = "VEDTAK_FATTET"
const val VENTER_SYKEPENGER = "VENTER_SYKEPENGER"
const val VEDTAK_IVERKSATT = "VEDTAK_IVERKSATT"
const val IKKE_OPPFYLT = "IKKE_OPPFYLT"

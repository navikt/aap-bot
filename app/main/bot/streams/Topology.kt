package bot.streams

import bot.devtools.DevtoolsClient
import bot.oppgavestyring.*
import no.nav.aap.dto.kafka.SøkereKafkaDto
import no.nav.aap.dto.kafka.SøkereKafkaDtoHistorikk
import no.nav.aap.kafka.streams.v2.KStreams
import no.nav.aap.kafka.streams.v2.Topology
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.kafka.streams.v2.topology
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*
import kotlin.time.Duration.Companion.seconds

private val secureLog = LoggerFactory.getLogger("secureLog")

internal fun topology(
    oppgavestyring: OppgavestyringClient,
    devtools: DevtoolsClient,
    kafka: KStreams,
    config: StreamsConfig,
    testSøkere: List<String>,
): Topology = topology {
    consume(Topics.søkere)
        .filterKey { personident -> personident in testSøkere }
        .secureLogWithKey { personident, value -> debug("Automatisk behandling av $personident, søker: $value") }

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
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_5?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_5",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // FATTER
                    body = Kvalitetssikring_11_5(
                        løsningId = UUID.randomUUID(),
                        kravOmNedsattArbeidsevneErGodkjent = true,
                        kravOmNedsattArbeidsevneErGodkjentBegrunnelse = "Godkjent",
                        nedsettelseSkyldesSykdomEllerSkadeErGodkjent = true,
                        nedsettelseSkyldesSykdomEllerSkadeErGodkjentBegrunnelse = "Godkjent"
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
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_6?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_6",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_11_6(
                        løsningId = UUID.randomUUID(),
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_19?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_19",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_11_19(
                        løsningId = UUID.randomUUID(),
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }
        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_22_13?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_22_13",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_22_13(
                        løsningId = UUID.randomUUID(),
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_2?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_2",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_11_2(
                        løsningId = UUID.randomUUID(),
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_3?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_3",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_11_3(
                        løsningId = UUID.randomUUID(),
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }

        .branch({ dto ->
            sjekkTilstand(dto) { sakstype -> sakstype.paragraf_11_4AndreOgTredjeLedd?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING }
        }) {
            it.forEach { personident, _ ->
                oppgavestyring.send(
                    personident = personident,
                    path = "kvalitetssikre/paragraf_11_4_ledd2Og3",
                    bruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR, // BESLUTTER
                    body = Kvalitetssikring_11_4_ledd2og3(
                        løsningId = UUID.randomUUID(),
                        erGodkjent = true,
                        begrunnelse = "Godkjent"
                    )
                )
            }
        }

        .branch(SKAL_IVERKSETTES) {
            it.forEach { personident, _ ->
                secureLog.debug("Sender inn iverksettelse for $personident")
                oppgavestyring.iverksett(personident)
            }
        }

    val vedtakTable = consume(Tables.vedtak)

    vedtakTable.schedule(
        SøkPåNyttScheduler(
            ktable = vedtakTable,
            interval = 10.seconds,
            devtools = devtools,
            kafka = kafka,
            config = config,
        )
    )
}

private fun sjekkTilstand(
    dto: SøkereKafkaDtoHistorikk,
    sakstype: (SøkereKafkaDto.SakstypeKafkaDto) -> Boolean
): Boolean {
    return dto.søkereKafkaDto
        .saker.first { it.tilstand == AVVENTER_VURDERING }
        .sakstyper.filter { it.aktiv }
        .any(sakstype)
}


private val SKAL_IVERKSETTES = { dto: SøkereKafkaDtoHistorikk ->
    secureLog.debug("Sjekk iverksetting for: ${dto.søkereKafkaDto.personident}")
    dto.søkereKafkaDto.saker.any { sak -> sak.tilstand == VEDTAK_FATTET }
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

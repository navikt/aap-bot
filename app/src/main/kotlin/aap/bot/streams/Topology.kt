package aap.bot.streams

import aap.bot.devtools.DevtoolsClient
import aap.bot.dolly.DollyClient
import aap.bot.oppgavestyring.OppgavestyringClient
import kotlinx.coroutines.runBlocking
import no.nav.aap.dto.kafka.SøkereKafkaDto
import no.nav.aap.dto.kafka.SøknadKafkaDto
import no.nav.aap.kafka.streams.v2.Topology
import no.nav.aap.kafka.streams.v2.topology
import org.apache.kafka.clients.producer.Producer
import kotlin.time.Duration.Companion.seconds

internal fun topology(
    oppgavestyring: OppgavestyringClient,
    devtools: DevtoolsClient,
    dolly: DollyClient,
    søknadProducer: Producer<String, SøknadKafkaDto>,
): Topology = topology {
    consume(Topics.søkere)
        .branch(TRENGER_INNGANGSVILKÅR) {
            it.forEach { personident, _ ->
                runBlocking {
                    oppgavestyring.løsningInngangsvilkår(personident)
                }
            }
        }
        .branch(TRENGER_LØSNING_LOKALKONTOR) {
            it.forEach { personident, _ ->
                runBlocking {
                    oppgavestyring.løsningLokalkontor(personident)
                }
            }
        }
        .branch(TRENGER_KVALITETSSIKRING_LOKALKONTOR) {
            it.forEach { personident, _ ->
                runBlocking {
                    oppgavestyring.kvalitetssikreLokalkontor(personident)
                }
            }
        }
        .branch(TRENGER_LØSNING_NAY) {
            it.forEach { personident, _ ->
                runBlocking {
                    oppgavestyring.løsningNAY(personident)
                }
            }
        }
        .branch(TRENGER_KVALITETSSIKRING_NAY) {
            it.forEach { personident, _ ->
                runBlocking {
                    oppgavestyring.kvalitetssikreNAY(personident)
                }
            }

        }
        .branch(SKAL_IVERKSETTES) {
            it.forEach { personident, _ ->
                runBlocking {
                    oppgavestyring.iverksett(personident)
                }
            }
        }

    val vedtakTable = consume(Topics.vedtak).produce(Tables.vedtak)

    vedtakTable.schedule(
        VedtakStateStoreCleaner(
            ktable = vedtakTable,
            interval = 10.seconds,
            devtools = devtools,
            dolly = dolly,
            søknadProducer = søknadProducer,
        )
    )
}

/**
 * 11-2 og 11-4 kan være løst automatisk, trenger bare sjekke 11-3
 */
private val TRENGER_INNGANGSVILKÅR = { dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        sak.sakstyper.first { it.aktiv }.paragraf_11_3?.tilstand == AVVENTER_MANUELL_VURDERING
    }
}

private val TRENGER_LØSNING_LOKALKONTOR = { dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        sak.sakstyper.first { it.aktiv }.let { aktiv ->
            aktiv.paragraf_11_5?.tilstand == AVVENTER_MANUELL_VURDERING ||
                    aktiv.paragraf_11_6?.tilstand == AVVENTER_INNSTILLING
        }
    }
}

private val TRENGER_KVALITETSSIKRING_LOKALKONTOR = { dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        sak.sakstyper.first { it.aktiv }
            .paragraf_11_5
            ?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING
    }
}

private val TRENGER_LØSNING_NAY = { dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        val sakstype = sak.sakstyper.first { it.aktiv }
        sakstype.paragraf_11_6?.tilstand == AVVENTER_MANUELL_VURDERING ||
                sakstype.paragraf_11_19?.tilstand == AVVENTER_MANUELL_VURDERING ||
                sakstype.paragraf_22_13?.tilstand == AVVENTER_MANUELL_VURDERING
    }
}

private val TRENGER_KVALITETSSIKRING_NAY = { dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        val sakstype = sak.sakstyper.first { it.aktiv }
        sakstype.paragraf_11_6?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING ||
                sakstype.paragraf_11_19?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING ||
                sakstype.paragraf_22_13?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING
    }
}

private val SKAL_IVERKSETTES = { dto: SøkereKafkaDto ->
    dto.saker.any { sak -> sak.tilstand == VEDTAK_FATTET }
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

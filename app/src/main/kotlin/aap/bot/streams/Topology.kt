package aap.bot.streams

import aap.bot.devtools.DevtoolsClient
import aap.bot.dolly.DollyClient
import aap.bot.oppgavestyring.OppgavestyringClient
import aap.bot.streams.søknad.SøknadDto
import kotlinx.coroutines.runBlocking
import no.nav.aap.dto.kafka.SøkereKafkaDto
import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import no.nav.aap.kafka.streams.extension.produce
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Branched
import kotlin.time.Duration.Companion.seconds

internal fun topology(
    oppgavestyring: OppgavestyringClient,
    devtools: DevtoolsClient,
    dolly: DollyClient,
    søknadProducer: Producer<String, SøknadDto>,
): Topology {

    val streams = StreamsBuilder()

    streams.consume(Topics.søkere)
        .filterNotNull("filter-sokere-tombstone")
        .split()
        .branch(TRENGER_INNGANGSVILKÅR, sendInngangsvilkår(oppgavestyring))
        .branch(TRENGER_LØSNING_LOKALKONTOR, sendLøsningLokalkontor(oppgavestyring))
        .branch(TRENGER_KVALITETSSIKRING_LOKALKONTOR, kvalitetssikreLokalkontor(oppgavestyring))
        .branch(TRENGER_LØSNING_NAY, sendLøsningNAY(oppgavestyring))
        .branch(TRENGER_KVALITETSSIKRING_NAY, kvalitetssikreNAY(oppgavestyring))
        .branch(SKAL_IVERKSETTES, iverksett(oppgavestyring))

    val vedtakTable = streams.consume(Topics.vedtak).produce(Tables.vedtak)

    vedtakTable.scheduleResøkAAP(
        table = Tables.vedtak,
        interval = 10.seconds,
        devtools = devtools,
        dolly = dolly,
        søknadProducer = søknadProducer
    ) { record, now ->
        // evict 10s or older records
        record.timestamp() + 10_000 > now
    }

    return streams.build()
}

/**
 * 11-2 og 11-4 kan være løst automatisk, trenger bare sjekke 11-3
 */
private val TRENGER_INNGANGSVILKÅR = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        sak.sakstyper.first { it.aktiv }.paragraf_11_3?.tilstand == AVVENTER_MANUELL_VURDERING
    }
}

private fun sendInngangsvilkår(client: OppgavestyringClient) =
    Branched.withConsumer<String, SøkereKafkaDto> { branch ->
        branch.foreach { personident, _ ->
            runBlocking {
                client.løsningInngangsvilkår(personident)
            }
        }
    }

private val TRENGER_LØSNING_LOKALKONTOR = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        sak.sakstyper.first { it.aktiv }.let { aktiv ->
            aktiv.paragraf_11_5?.tilstand == AVVENTER_MANUELL_VURDERING ||
                    aktiv.paragraf_11_6?.tilstand == AVVENTER_INNSTILLING
        }
    }
}

private fun sendLøsningLokalkontor(client: OppgavestyringClient) =
    Branched.withConsumer<String, SøkereKafkaDto> { branch ->
        branch.foreach { personident, _ ->
            runBlocking {
                client.løsningLokalkontor(personident)
            }
        }
    }

private val TRENGER_KVALITETSSIKRING_LOKALKONTOR = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        sak.sakstyper.first { it.aktiv }
            .paragraf_11_5
            ?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING
    }
}

private fun kvalitetssikreLokalkontor(client: OppgavestyringClient) =
    Branched.withConsumer<String, SøkereKafkaDto> { branch ->
        branch.foreach { personident, _ ->
            runBlocking {
                client.kvalitetssikreLokalkontor(personident)
            }
        }
    }

private val TRENGER_LØSNING_NAY = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        val sakstype = sak.sakstyper.first { it.aktiv }
        sakstype.paragraf_11_6?.tilstand == AVVENTER_MANUELL_VURDERING ||
                sakstype.paragraf_11_19?.tilstand == AVVENTER_MANUELL_VURDERING ||
                sakstype.paragraf_22_13?.tilstand == AVVENTER_MANUELL_VURDERING
    }
}

private fun sendLøsningNAY(client: OppgavestyringClient) =
    Branched.withConsumer<String, SøkereKafkaDto> { branch ->
        branch.foreach { personident, _ ->
            runBlocking {
                client.løsningNAY(personident)
            }
        }
    }

private val TRENGER_KVALITETSSIKRING_NAY = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        val sakstype = sak.sakstyper.first { it.aktiv }
        sakstype.paragraf_11_6?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING ||
                sakstype.paragraf_11_19?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING ||
                sakstype.paragraf_22_13?.tilstand == OPPFYLT_MANUELT_AVVENTER_KVALITETSSIKRING
    }
}

private fun kvalitetssikreNAY(client: OppgavestyringClient) =
    Branched.withConsumer<String, SøkereKafkaDto> { branch ->
        branch.foreach { personident, _ ->
            runBlocking {
                client.kvalitetssikreNAY(personident)
            }
        }
    }

private val SKAL_IVERKSETTES = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak -> sak.tilstand == VEDTAK_FATTET }
}

private fun iverksett(client: OppgavestyringClient) = Branched.withConsumer<String, SøkereKafkaDto> { branch ->
    branch.foreach { personident, _ ->
        runBlocking {
            client.iverksett(personident)
        }
    }
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

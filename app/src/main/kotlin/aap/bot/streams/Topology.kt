package aap.bot.streams

import aap.bot.oppgavestyring.OppgavestyringClient
import kotlinx.coroutines.runBlocking
import no.nav.aap.dto.kafka.SøkereKafkaDto
import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Branched

internal fun topology(oppgavestyring: OppgavestyringClient): Topology {

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

    return streams.build()
}

/**
 * 11-2 og 11-4 kan være løst automatisk, trenger bare sjekke 11-3
 */
val TRENGER_INNGANGSVILKÅR = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        sak.sakstyper.first { it.aktiv }.paragraf_11_3?.tilstand == SØKNAD_MOTTATT
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

val TRENGER_LØSNING_LOKALKONTOR = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        sak.sakstyper.first { it.aktiv }.let { aktiv ->
            aktiv.paragraf_11_5?.tilstand == SØKNAD_MOTTATT ||
                    aktiv.paragraf_11_6?.tilstand == SØKNAD_MOTTATT
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

val TRENGER_KVALITETSSIKRING_LOKALKONTOR = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        sak.sakstyper.first { it.aktiv }
            .paragraf_11_5
            ?.tilstand == OPPFYLT_MANUELT
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

val TRENGER_LØSNING_NAY = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        val sakstype = sak.sakstyper.first { it.aktiv }
        sakstype.paragraf_11_6?.tilstand == MANUELL_VURDERING_TRENGS ||
                sakstype.paragraf_11_19?.tilstand == SØKNAD_MOTTATT ||
                sakstype.paragraf_22_13?.tilstand == SØKNAD_MOTTATT
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

val TRENGER_KVALITETSSIKRING_NAY = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        val sakstype = sak.sakstyper.first { it.aktiv }
        sakstype.paragraf_11_6?.tilstand == OPPFYLT_MANUELT ||
                sakstype.paragraf_11_19?.tilstand == OPPFYLT_MANUELT ||
                sakstype.paragraf_22_13?.tilstand == OPPFYLT_MANUELT
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

val SKAL_IVERKSETTES = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak -> sak.tilstand == VEDTAK_FATTET }
}

private fun iverksett(client: OppgavestyringClient) = Branched.withConsumer<String, SøkereKafkaDto> { branch ->
    branch.foreach { personident, _ ->
        runBlocking {
            client.iverksett(personident)
        }
    }
}

const val IKKE_VURDERT = "IKKE_VURDERT"
const val SØKNAD_MOTTATT = "SØKNAD_MOTTATT"
const val MANUELL_VURDERING_TRENGS = "MANUELL_VURDERING_TRENGS"
const val OPPFYLT_MASKINELT = "OPPFYLT_MASKINELT"
const val OPPFYLT_MASKINELT_KVALITETSSIKRET = "OPPFYLT_MASKINELT_KVALITETSSIKRET"
const val IKKE_OPPFYLT_MASKINELT = "IKKE_OPPFYLT_MASKINELT"
const val IKKE_OPPFYLT_MASKINELT_KVALITETSSIKRET = "IKKE_OPPFYLT_MASKINELT_KVALITETSSIKRET"
const val OPPFYLT_MANUELT = "OPPFYLT_MANUELT"
const val OPPFYLT_MANUELT_KVALITETSSIKRET = "OPPFYLT_MANUELT_KVALITETSSIKRET"
const val IKKE_OPPFYLT_MANUELT = "IKKE_OPPFYLT_MANUELT"
const val IKKE_OPPFYLT_MANUELT_KVALITETSSIKRET = "IKKE_OPPFYLT_MANUELT_KVALITETSSIKRET"
const val IKKE_RELEVAN = "IKKE_RELEVANT"

const val START = "START"
const val BEREGN_INNTEKT = "BEREGN_INNTEKT"
const val AVVENTER_KVALITETSSIKRING = "AVVENTER_KVALITETSSIKRING"
const val VEDTAK_FATTET = "VEDTAK_FATTET"
const val VENTER_SYKEPENGER = "VENTER_SYKEPENGER"
const val VEDTAK_IVERKSATT = "VEDTAK_IVERKSATT"
const val IKKE_OPPFYLT = "IKKE_OPPFYLT"

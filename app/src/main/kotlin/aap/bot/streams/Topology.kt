package aap.bot.streams

import no.nav.aap.dto.kafka.SøkereKafkaDto
import no.nav.aap.kafka.streams.extension.consume
import no.nav.aap.kafka.streams.extension.filterNotNull
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Branched

internal fun topology(): Topology {

    val streams = StreamsBuilder()

    streams.consume(Topics.søkere)
        .filterNotNull("filter-sokere-tombstone")
        .split()
        .branch(TRENGER_INNGANGSVILKÅR, sendInngangsvilkår())

    return streams.build()
}


fun sendInngangsvilkår() = Branched.withConsumer<String, SøkereKafkaDto> { chain ->
    chain
}

/**
 * 11-2 og 11-4 kan være løst automatisk, trenger bare sjekke 11-3
 */
val TRENGER_INNGANGSVILKÅR = { _: String, dto: SøkereKafkaDto ->
    dto.saker.any { sak ->
        sak.sakstyper.first { it.aktiv }.paragraf_11_3?.tilstand == "SØKNAD_MOTTATT"
    }
}

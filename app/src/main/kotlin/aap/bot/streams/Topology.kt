package aap.bot.streams

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology

internal fun topology(): Topology {

    val streams = StreamsBuilder()

    return streams.build()
}

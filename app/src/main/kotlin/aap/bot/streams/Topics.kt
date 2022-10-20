package aap.bot.streams

import aap.bot.streams.søknad.SøknadDto
import no.nav.aap.dto.kafka.SøkereKafkaDto
import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.Topic
import org.apache.kafka.common.serialization.Serdes

object Topics {
    val søknad = Topic("aap.soknad-sendt.v1", JsonSerde.jackson<SøknadDto>())
    val søkere = Topic("aap.sokere.v1", JsonSerde.jackson<SøkereKafkaDto>())
    val vedtak = Topic("aap.vedtak.v1", Serdes.ByteArraySerde())
}

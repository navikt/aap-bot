package aap.bot.streams

import aap.bot.streams.søknad.SøknadDto
import no.nav.aap.dto.kafka.SøkereKafkaDto
import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.Topic

object Topics {

    val søknad = Topic("aap.soknad-sendt.v1", JsonSerde.jackson<SøknadDto>())
    val søkere = Topic("aap.sokere.v1", JsonSerde.jackson<SøkereKafkaDto>())


}

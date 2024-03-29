package bot.streams

import no.nav.aap.dto.kafka.SøkereKafkaDtoHistorikk
import no.nav.aap.dto.kafka.SøknadKafkaDto
import no.nav.aap.kafka.streams.v2.Topic
import no.nav.aap.kafka.streams.v2.serde.ByteArraySerde
import no.nav.aap.kafka.streams.v2.serde.JsonSerde

object Topics {
    val søknad = Topic("aap.soknad-sendt.v1", JsonSerde.jackson<SøknadKafkaDto>())
    val søkere = Topic("aap.sokere.v1", JsonSerde.jackson<SøkereKafkaDtoHistorikk>())
    val vedtak = Topic("aap.vedtak.v1", ByteArraySerde)
    val subscribeSykepengedager = Topic("aap.subscribe-sykepengedager.v1", ByteArraySerde)
}

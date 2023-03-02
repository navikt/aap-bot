package aap.bot.streams

import aap.bot.devtools.DevtoolsClient
import aap.bot.dolly.DollyClient
import aap.bot.dolly.Gruppe
import aap.bot.produce
import aap.bot.søknad.Søknader
import kotlinx.coroutines.runBlocking
import no.nav.aap.dto.kafka.SøknadKafkaDto
import no.nav.aap.kafka.streams.v2.KTable
import no.nav.aap.kafka.streams.v2.StateStore
import no.nav.aap.kafka.streams.v2.processor.state.StateScheduleProcessor
import org.apache.kafka.clients.producer.Producer
import kotlin.time.Duration

internal class SøkPåNyttScheduler<V: Any>(
    ktable: KTable<V>,
    interval: Duration,
    private val devtools: DevtoolsClient,
    private val dolly: DollyClient,
    private val søknadProducer: Producer<String, SøknadKafkaDto>,
) : StateScheduleProcessor<V>(
    named = "${ktable.table.stateStoreName}-cleaner",
    table = ktable,
    interval = interval
) {
    override fun schedule(wallClockTime: Long, store: StateStore<V>) {
        store.forEachTimestamped { key, _, timestamp ->
            // if record is more than 10_000 ms old
            if (timestamp + 10_000 < wallClockTime) {
                runBlocking {
                    devtools.delete(key)
                }

                runBlocking {
                    dolly.hentBrukere(Gruppe.AAP_HAPPY_BOT)
                        .firstOrNull { it.fødselsnummer == key }
                        ?.let { person ->
                            søknadProducer.produce(
                                topic = Topics.søknad,
                                key = person.fødselsnummer,
                                value = Søknader.generell(person.fødselsdato)
                            )
                        }
                }
            }
        }
    }
}

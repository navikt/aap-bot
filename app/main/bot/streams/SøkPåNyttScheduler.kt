package bot.streams

import bot.devtools.DevtoolsClient
import bot.produceSøknad
import bot.søknad.Søknader
import kotlinx.coroutines.runBlocking
import no.nav.aap.dto.kafka.SøknadKafkaDto
import no.nav.aap.kafka.streams.v2.KTable
import no.nav.aap.kafka.streams.v2.StateStore
import no.nav.aap.kafka.streams.v2.processor.state.StateScheduleProcessor
import org.apache.kafka.clients.producer.Producer
import java.time.LocalDate
import kotlin.random.Random
import kotlin.time.Duration

internal class SøkPåNyttScheduler<V : Any>(
    ktable: KTable<V>,
    interval: Duration,
    private val devtools: DevtoolsClient,
    private val søknadProducer: Producer<String, SøknadKafkaDto>,
) : StateScheduleProcessor<V>(
    named = "${ktable.table.stateStoreName}-cleaner",
    table = ktable,
    interval = interval
) {
    override fun schedule(wallClockTime: Long, store: StateStore<V>) {
        store.forEachTimestamped { personident, _, timestamp ->
            // if record is more than 10_000 ms old
            if (timestamp + 10_000 < wallClockTime) {
                runBlocking {
                    if (devtools.delete(personident)) {
                        søknadProducer.produceSøknad(personident) {
                            Søknader.generell(LocalDate.now().minusYears(Random.nextLong(18, 67)))
                        }
                    }
                }
            }
        }
    }
}

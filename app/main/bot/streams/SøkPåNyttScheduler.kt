package bot.streams

import bot.devtools.DevtoolsClient
import bot.produceSøknad
import bot.søknad.Søknader
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.streams.v2.KStreams
import no.nav.aap.kafka.streams.v2.KTable
import no.nav.aap.kafka.streams.v2.StateStore
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.kafka.streams.v2.processor.state.StateScheduleProcessor
import java.time.LocalDate
import kotlin.random.Random
import kotlin.time.Duration

internal class SøkPåNyttScheduler<V : Any>(
    ktable: KTable<V>,
    interval: Duration,
    private val devtools: DevtoolsClient,
    private val kafka: KStreams,
    private val config: StreamsConfig,
    private val testSøkere: List<String>,
) : StateScheduleProcessor<V>(
    named = "${ktable.table.stateStoreName}-cleaner",
    table = ktable,
    interval = interval
) {
    override fun schedule(wallClockTime: Long, store: StateStore<V>) {
        store.forEachTimestamped { personident, _, timestamp ->
            if (personident in testSøkere) {
                // if record is more than 10_000 ms old
                if (timestamp + 10_000 < wallClockTime) {
                    runBlocking {
                        if (devtools.delete(personident)) {
                            delay(10_000)

                            kafka.createProducer(config, Topics.søknad).use { producer ->
                                producer.produceSøknad(personident) {
                                    Søknader.generell(LocalDate.now().minusYears(Random.nextLong(18, 62)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

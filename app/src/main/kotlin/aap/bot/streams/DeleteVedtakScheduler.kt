package aap.bot.streams

import aap.bot.devtools.DevtoolsClient
import aap.bot.dolly.DollyClient
import aap.bot.dolly.Gruppe
import aap.bot.produce
import kotlinx.coroutines.runBlocking
import no.nav.aap.dto.kafka.Medlemskap
import no.nav.aap.dto.kafka.Studier
import no.nav.aap.dto.kafka.SøknadKafkaDto
import no.nav.aap.kafka.streams.Table
import no.nav.aap.kafka.streams.named
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.processor.api.Processor
import org.apache.kafka.streams.processor.api.ProcessorContext
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.apache.kafka.streams.processor.api.Record
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.ValueAndTimestamp
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.toJavaDuration

private class VedtakCleaner<V>(
    private val table: Table<V>,
    private val interval: Duration,
    private val devtools: DevtoolsClient,
    private val dolly: DollyClient,
    private val søknadProducer: Producer<String, SøknadKafkaDto>,
    private val predicate: (value: ValueAndTimestamp<V>, now: Long) -> Boolean,
) : Processor<String, V, Void, Void> {

    override fun process(record: Record<String, V>) {}

    override fun init(context: ProcessorContext<Void, Void>) {
        val store = context.getStateStore<KeyValueStore<String, ValueAndTimestamp<V>>>(table.stateStoreName)

        context.schedule(interval.toJavaDuration(), PunctuationType.WALL_CLOCK_TIME) { wallClockTime ->
            store.all().use { iterator ->
                iterator.forEach { record ->
                    if (predicate(record.value, wallClockTime)) {

                        runBlocking {
                            devtools.delete(record.key)
                        }

                        runBlocking {
                            dolly.hentBrukere(Gruppe.AAP_HAPPY_BOT)
                                .firstOrNull { it.fødselsnummer == record.key }
                                ?.let { person ->
                                    søknadProducer.produce(
                                        Topics.søknad,
                                        person.fødselsnummer,
                                        SøknadKafkaDto(
                                            sykepenger = true,
                                            ferie = null,
                                            studier = Studier(
                                                erStudent = null,
                                                kommeTilbake = null,
                                                vedlegg = emptyList(),
                                            ),
                                            medlemsskap = Medlemskap(
                                                boddINorgeSammenhengendeSiste5 = true,
                                                jobbetUtenforNorgeFørSyk = null,
                                                jobbetSammenhengendeINorgeSiste5 = null,
                                                iTilleggArbeidUtenforNorge = null,
                                                utenlandsopphold = emptyList(),
                                            ),
                                            registrerteBehandlere = emptyList(),
                                            andreBehandlere = emptyList(),
                                            yrkesskadeType = SøknadKafkaDto.Yrkesskade.NEI,
                                            utbetalinger = null,
                                            tilleggsopplysninger = null,
                                            registrerteBarn = emptyList(),
                                            andreBarn = emptyList(),
                                            vedlegg = emptyList(),
                                            fødselsdato = person.fødselsdato,
                                            innsendingTidspunkt = LocalDateTime.now(),
                                        )
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
}

internal fun <V> KTable<String, V>.scheduleResøkAAP(
    table: Table<V>,
    interval: Duration,
    devtools: DevtoolsClient,
    dolly: DollyClient,
    søknadProducer: Producer<String, SøknadKafkaDto>,
    predicate: (value: ValueAndTimestamp<V>, now: Long) -> Boolean,
) = toStream().process(
    ProcessorSupplier { VedtakCleaner(table, interval, devtools, dolly, søknadProducer, predicate) },
    named("vedtak-cleaner-${table.stateStoreName}"),
    table.stateStoreName
)

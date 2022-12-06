package aap.bot

import aap.bot.devtools.DevtoolsClient
import aap.bot.dolly.DollyClient
import aap.bot.dolly.Gruppe
import aap.bot.oppgavestyring.OppgavestyringClient
import aap.bot.streams.Topics
import aap.bot.streams.topology
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.logstash.logback.argument.StructuredArguments
import no.nav.aap.dto.kafka.Medlemskap
import no.nav.aap.dto.kafka.Studier
import no.nav.aap.dto.kafka.SøknadKafkaDto
import no.nav.aap.kafka.streams.KStreams
import no.nav.aap.kafka.streams.KafkaStreams
import no.nav.aap.kafka.streams.Topic
import no.nav.aap.kafka.vanilla.KafkaConfig
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::bot).start(wait = true)
}

val secureLog = LoggerFactory.getLogger("secureLog")

fun Application.bot(kafka: KStreams = KafkaStreams) {
    Thread.currentThread().setUncaughtExceptionHandler { _, e -> log.error("Uhåndtert feil", e) }

    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) { registry = prometheus }

    val config = loadConfig<Config>()
    val søknadProducer = kafka.createProducer(KafkaConfig.copyFrom(config.kafka), Topics.søknad)
    val oppgavestyring = OppgavestyringClient(config.oppgavestyring, config.azure)
    val dolly = DollyClient(config.dolly, config.azure)
    val devtools = DevtoolsClient(config.devtools)

    environment.monitor.subscribe(ApplicationStopping) {
        kafka.close()
        søknadProducer.close()
    }

    kafka.connect(config.kafka, prometheus, topology(oppgavestyring, devtools, dolly, søknadProducer))

    produceAsync(dolly, søknadProducer)

    routing {
        route("/actuator") {
            get("/metrics") { call.respondText(prometheus.scrape()) }
            get("/live") { call.respondText("bot") }
            get("/ready") { call.respondText("bot") }
        }
    }
}

private fun Application.produceAsync(
    dolly: DollyClient,
    søknadProducer: Producer<String, SøknadKafkaDto>
) {
    launch {
        dolly.hentBrukere(Gruppe.AAP_HAPPY_BOT).forEach { person ->
            val personident = person.fødselsnummer
            val søknad = SøknadKafkaDto(
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
            søknadProducer.produce(Topics.søknad, personident, søknad)
            delay(10_000)
        }
    }
}

inline fun <reified V : Any> Producer<String, V>.produce(topic: Topic<V>, key: String, value: V) {
    val record = ProducerRecord(topic.name, key, value)
    send(record).get().also {
        secureLog.trace(
            "Sender inn søknad",
            StructuredArguments.kv("key", record.key()),
            StructuredArguments.kv("topic", topic.name),
            StructuredArguments.kv("partition", it.partition()),
            StructuredArguments.kv("value", record.value()),
        )
    }
}

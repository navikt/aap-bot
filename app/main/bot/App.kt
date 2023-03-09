package bot

import bot.devtools.DevtoolsClient
import bot.devtools.TestPerson
import bot.oppgavestyring.OppgavestyringClient
import bot.streams.Topics
import bot.streams.topology
import bot.søknad.Søknader
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.aap.dto.kafka.SøknadKafkaDto
import no.nav.aap.kafka.streams.v2.KStreams
import no.nav.aap.kafka.streams.v2.KafkaStreams
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::bot).start(wait = true)
}

private val secureLog = LoggerFactory.getLogger("secureLog")

fun Application.bot(kafka: KStreams = KafkaStreams()) {
    Thread.currentThread().setUncaughtExceptionHandler { thread, exception ->
        secureLog.error("Uhåndtert feil. Thread:${thread.name}", exception)
    }

    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) { registry = prometheus }

    install(CallLogging) {
        logger = secureLog
        filter { call -> call.request.path().startsWith("/actuator").not() }
    }

    val config = loadConfig<Config>()
    val oppgavestyring = OppgavestyringClient(config.oppgavestyring, config.azure)
    val devtools = DevtoolsClient(config.devtools)

    environment.monitor.subscribe(ApplicationStopping) {
        kafka.close()
    }

    val testPersoner = runBlocking {
        devtools.getTestpersoner()
    }

    resetSøkere(testPersoner, devtools, kafka, config.kafka)

    kafka.connect(
        config = config.kafka,
        registry = prometheus,
        topology = topology(
            oppgavestyring = oppgavestyring,
            devtools = devtools,
            kafka = kafka,
            config = config.kafka,
            testSøkere = testPersoner.map(TestPerson::fødselsnummer)
        )
    )

    routing {
        get("/reset") {
            this@bot.resetSøkere(testPersoner, devtools, kafka, config.kafka)
            call.respondText("Resetter søkere.")
        }

        route("/actuator") {
            get("/metrics") { call.respondText(prometheus.scrape()) }
            get("/live") { call.respondText("bot") }
            get("/ready") { call.respondText("bot") }
        }
    }
}

private fun Application.resetSøkere(
    testPersoner: List<TestPerson>,
    devtools: DevtoolsClient,
    kafka: KStreams,
    kafkaConfig: StreamsConfig
) = launch {
    testPersoner.forEach { søker ->
        if (devtools.delete(søker.fødselsnummer)) {
            delay(10_000) // forsikre at ktables har blitt slettet
            kafka.createProducer(kafkaConfig, Topics.søknad).use { producer ->
                producer.produceSøknad(søker.fødselsnummer) {
                    Søknader.generell(søker.fødselsdato)
                }
            }
        }
    }
}

internal fun Producer<String, SøknadKafkaDto>.produceSøknad(personident: String, søknad: () -> SøknadKafkaDto) {
    val metadata = send(ProducerRecord(Topics.søknad.name, personident, søknad())).get()
    secureLog.trace(
        "Søknad sendt for $personident",
        kv("key", personident),
        kv("value", søknad()),
        kv("partition", metadata.partition()),
        kv("topic", metadata.topic())
    )
}

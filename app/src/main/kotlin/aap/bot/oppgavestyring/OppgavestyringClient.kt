package aap.bot.oppgavestyring

import aap.bot.Config
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import no.nav.aap.ktor.client.HttpClientAzureAdTokenProvider
import org.slf4j.LoggerFactory

private val secureLog = LoggerFactory.getLogger("secureLog")

internal class OppgavestyringClient(private val config: Config) {
    //  løsningNAY
    //  løsningLokalkontor
    //  kvalitetssikringNAY
    //  kvalitetssikringLokalkontor
    private val tokenProvider = HttpClientAzureAdTokenProvider(config.azure, config.oppgavestyring.scope)

    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout)
        install(HttpRequestRetry)
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) = secureLog.info(message)
            }
        }
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }
    }

    suspend fun løsningInngangsvilkår(personident: String) {
        val tokenNAY = tokenProvider.getToken()
        val (path, inngangsvilkår) = Løsning.inngangsvilkår()
        val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenNAY)
            setBody(inngangsvilkår)
        }
        require(response.status == HttpStatusCode.OK)
    }

    suspend fun løsningLokalkontor(personident: String) {
        val tokenLokal = tokenProvider.getToken()
        Løsning.lokalkontor().forEach { (path, løsning) ->
            val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
                contentType(ContentType.Application.Json)
                bearerAuth(tokenLokal)
                setBody(løsning)
            }
            require(response.status == HttpStatusCode.OK)
        }
    }
    suspend fun løsningNAY(personident: String) {
        val tokenNAY = tokenProvider.getToken()
        Løsning.saksbehandler().forEach { (path, løsning) ->
            val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
                contentType(ContentType.Application.Json)
                bearerAuth(tokenNAY)
                setBody(løsning)
            }
            require(response.status == HttpStatusCode.OK)
        }
    }

    suspend fun kvalitetssikreNAY(personident: String) {
        val tokenNAY = tokenProvider.getToken()
        Kvalitetssikring.pathsForNAY().forEach { path ->
            val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
                contentType(ContentType.Application.Json)
                bearerAuth(tokenNAY)
                setBody(Kvalitetssikring.godkjent())
            }
            require(response.status == HttpStatusCode.OK)
        }
    }
    suspend fun kvalitetssikreLokalkontor(personident: String) {
        val tokenLokal = tokenProvider.getToken()
        Kvalitetssikring.pathsForLokalkontor().forEach { path ->
            val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
                contentType(ContentType.Application.Json)
                bearerAuth(tokenLokal)
                setBody(Kvalitetssikring.godkjent())
            }
            require(response.status == HttpStatusCode.OK)
        }
    }

    suspend fun iverksett(personident: String) {
        val tokenLokal = tokenProvider.getToken()

        val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/iverksett") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenLokal)
        }
        require(response.status == HttpStatusCode.OK)
    }
}

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
import no.nav.aap.ktor.client.HttpClientUserLoginTokenProvider
import org.slf4j.LoggerFactory

private val secureLog = LoggerFactory.getLogger("secureLog")

internal class OppgavestyringClient(
    private val config: Config,
    private val tokenProvider: TokenProvider = TokenProvider(config)
) {
    suspend fun løsningInngangsvilkår(personident: String) {
        val (path, inngangsvilkår) = Løsning.inngangsvilkår()
        val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenProvider.get(Testbruker.SAKSBEHANDLER_OG_VEILEDER))
            setBody(inngangsvilkår)
        }
        require(response.status == HttpStatusCode.OK)
    }

    suspend fun løsningLokalkontor(personident: String) {
        Løsning.lokalkontor().forEach { (path, løsning) ->
            val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
                contentType(ContentType.Application.Json)
                bearerAuth(tokenProvider.get(Testbruker.SAKSBEHANDLER_OG_VEILEDER))
                setBody(løsning)
            }
            require(response.status == HttpStatusCode.OK)
        }
    }

    suspend fun løsningNAY(personident: String) {
        Løsning.saksbehandler().forEach { (path, løsning) ->
            val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
                contentType(ContentType.Application.Json)
                bearerAuth(tokenProvider.get(Testbruker.SAKSBEHANDLER_OG_VEILEDER))
                setBody(løsning)
            }
            require(response.status == HttpStatusCode.OK)
        }
    }

    suspend fun kvalitetssikreNAY(personident: String) {
        Kvalitetssikring.pathsForNAY().forEach { path ->
            val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
                contentType(ContentType.Application.Json)
                bearerAuth(tokenProvider.get(Testbruker.BESLUTTER_OG_FATTER))
                setBody(Kvalitetssikring.godkjent())
            }
            require(response.status == HttpStatusCode.OK)
        }
    }

    suspend fun kvalitetssikreLokalkontor(personident: String) {
        Kvalitetssikring.pathsForLokalkontor().forEach { path ->
            val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
                contentType(ContentType.Application.Json)
                bearerAuth(tokenProvider.get(Testbruker.BESLUTTER_OG_FATTER))
                setBody(Kvalitetssikring.godkjent())
            }
            require(response.status == HttpStatusCode.OK)
        }
    }

    suspend fun iverksett(personident: String) {
        val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/iverksett") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenProvider.get(Testbruker.SAKSBEHANDLER_OG_VEILEDER))
        }
        require(response.status == HttpStatusCode.OK)
    }

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
}

internal class TokenProvider(private val config: Config) {
    private val tokenProvider: HttpClientUserLoginTokenProvider =
        HttpClientUserLoginTokenProvider(config.azure, config.oppgavestyring.scope)

    internal suspend fun get(bruker: Testbruker): String =
        tokenProvider.getToken(bruker.epost, config.oppgavestyring.testbrukerPassord)
}

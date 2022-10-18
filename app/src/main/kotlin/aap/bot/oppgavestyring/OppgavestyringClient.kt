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
    suspend fun løsningInngangsvilkår(
        personident: String,
        bruker: Testbruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER,
    ) {
        Løsning.inngangsvilkår().forEach { (path, løsning) ->
            send(personident, path, bruker, løsning)
        }
    }

    suspend fun løsningLokalkontor(
        personident: String,
        bruker: Testbruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER,
    ) {
        Løsning.lokalkontor().forEach { (path, løsning) ->
            send(personident, path, bruker, løsning)
        }
    }

    suspend fun løsningNAY(
        personident: String,
        bruker: Testbruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER,
    ) {
        Løsning.saksbehandler().forEach { (path, løsning) ->
            send(personident, path, bruker, løsning)
        }
    }

    suspend fun kvalitetssikreNAY(
        personident: String,
        bruker: Testbruker = Testbruker.BESLUTTER_OG_FATTER,
    ) {
        Kvalitetssikring.nay(bruker).forEach { (path, kvalitetssikring) ->
            send(personident, path, bruker, kvalitetssikring)
        }
    }

    suspend fun kvalitetssikreLokalkontor(
        personident: String,
        bruker: Testbruker = Testbruker.BESLUTTER_OG_FATTER,
    ) {
        Kvalitetssikring.lokalkontor(bruker).forEach { (path, kvalitetssikring) ->
            send(personident, path, bruker, kvalitetssikring)
        }
    }

    suspend fun iverksett(personident: String) {
        val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/iverksett") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenProvider.getAccessToken(Testbruker.SAKSBEHANDLER_OG_VEILEDER))
        }
        require(response.status == HttpStatusCode.OK)
    }

    private suspend fun send(personident: String, path: String, bruker: Testbruker, body: Any) {
        val response = httpClient.post("${config.oppgavestyring.host}/sak/$personident/$path") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenProvider.getAccessToken(bruker))
            setBody(body)
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

    internal suspend fun getAccessToken(bruker: Testbruker): String =
        tokenProvider.getToken(bruker.epost, config.oppgavestyring.testbrukerPassord)
}

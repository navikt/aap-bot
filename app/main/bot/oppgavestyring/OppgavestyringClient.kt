package bot.oppgavestyring

import bot.OppgavestyringConfig
import bot.http.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.ktor.client.AzureConfig
import org.slf4j.LoggerFactory

internal class OppgavestyringClient(
    private val oppgavestyringConfig: OppgavestyringConfig,
    azure: AzureConfig,
) {
    private val tokenProvider: TokenProvider = TokenProvider(oppgavestyringConfig, azure)
    private val oppgavestyringClient: HttpClient = HttpClientFactory.create(LogLevel.ALL)

    /** Sendes inn til slutt */
    fun iverksett(personident: String) = runBlocking {
        val response = oppgavestyringClient.post("${oppgavestyringConfig.host}/api/sak/$personident/iverksett") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenProvider.getAccessToken(Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR)) // BESLUTTER
        }

        if (!response.status.isSuccess()) {
            secureLog.warn(
                """
                Wrong HTTP Status from iverksettelse av oppgavestyring: ${response.status}
                Skipper melding og leser neste.
                Message: ${response.bodyAsText()}
            """
            )
        }
    }

    fun send(personident: String, path: String, bruker: Testbruker, body: Any) = runBlocking {
        val url = "${oppgavestyringConfig.host}/api/sak/$personident/$path"
        secureLog.info("Sender inn løsning/kvalitetssikring til oppgavestyring for $personident på $url")
        val response = oppgavestyringClient.post(url) {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenProvider.getAccessToken(bruker))
            setBody(body)
        }

        if (!response.status.isSuccess()) {
            secureLog.warn(
                """
                    Wrong HTTP Status from oppgavestyring: ${response.status}
                    Skipper melding og leser neste.
                    Message: ${response.bodyAsText()}
                """
            )
        }
    }
}

private val secureLog = LoggerFactory.getLogger("secureLog")

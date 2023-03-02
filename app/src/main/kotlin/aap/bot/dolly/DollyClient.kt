package aap.bot.dolly

import aap.bot.http.HttpClientFactory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import no.nav.aap.ktor.client.AzureAdTokenProvider
import no.nav.aap.ktor.client.AzureConfig
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*

data class DollyConfig(
    val url: URL,
    val scope: String,
)

private val log = LoggerFactory.getLogger(DollyClient::class.java)
private const val APP_NAME = "aap-bot"

enum class Gruppe(val id: String) {
    AAP_HAPPY_BOT("5865")
}

class DollyClient(private val dollyConfig: DollyConfig, azureConfig: AzureConfig) {
    private val tokenProvider = AzureAdTokenProvider(
        config = azureConfig,
        scope = dollyConfig.scope,
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                }
            }
            install(Logging) {
                level = LogLevel.BODY
                logger = object : Logger {
                    private var logBody = false
                    override fun log(message: String) {
                        when {
                            message == "BODY START" -> logBody = true
                            message == "BODY END" -> logBody = false
                            logBody -> log.info("respons fra azuread: $message")
                        }
                    }
                }
            }
        }
    )

    private val httpClient = HttpClientFactory.create(LogLevel.ALL)

    suspend fun hentBrukere(gruppe: Gruppe): List<DollyResponsePerson> {
        val token = tokenProvider.getClientCredentialToken()
        val callId = callId

        val brukereForGruppe = httpClient.get("${dollyConfig.url}/gruppe/${gruppe.id}") {
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", callId)
            header("Nav-Consumer-Id", APP_NAME)
            bearerAuth(token)
        }.body<DollyResponseGrupper>()

        val identer = brukereForGruppe.identer.joinToString(",") { it.ident }
        val url = "${dollyConfig.url}/pdlperson/identer?identer=${identer}"

        val brukerlisteJson = httpClient.get(url) {
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", callId)
            header("Nav-Consumer-Id", APP_NAME)
            bearerAuth(token)
        }.body<DollyResponsePdl>()

        return brukerlisteJson.data.hentPersonBolk.map {
            DollyResponsePerson(
                fødselsnummer = it.ident,
                navn = "${it.person.navn.first().fornavn} ${it.person.navn.first().etternavn}",
                fødselsdato = it.person.foedsel.first().foedselsdato
            )
        }
    }

    private val callId: String get() = UUID.randomUUID().toString().also { log.info("calling dolly with call-id $it") }
}

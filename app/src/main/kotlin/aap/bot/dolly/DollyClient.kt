package aap.bot.dolly

import aap.bot.http.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
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
    private val tokenProvider = AzureAdTokenProvider(azureConfig, dollyConfig.scope)

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

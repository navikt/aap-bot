package aap.bot.dolly

import aap.bot.http.HttpClientFactory
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import no.nav.aap.ktor.client.AzureConfig
import no.nav.aap.ktor.client.HttpClientAzureAdTokenProvider
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL
import java.time.LocalDate
import java.util.*

data class DollyConfig(
    val url: URL,
    val scope: String,
)

private val log = LoggerFactory.getLogger(DollyClient::class.java)
private const val APP_NAME = "aap-kelmira"

class DollyClient(private val dollyConfig: DollyConfig, azureConfig: AzureConfig) {
    private val tokenProvider = HttpClientAzureAdTokenProvider(azureConfig, dollyConfig.scope)

    private val httpClient = HttpClientFactory.create(LogLevel.ALL)

    suspend fun hentBrukere(gruppe: String): List<DollyResponsePerson> {
        val token = tokenProvider.getToken()
        val callId = callId

        val brukereForGruppe = httpClient.get("${dollyConfig.url}/gruppe/$gruppe") {
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

        return brukerlisteJson.data.hentPersonBolk.map { DollyResponsePerson(
            fødselsnummer = it.ident,
            navn = "${it.person.navn.first().fornavn} ${it.person.navn.first().etternavn}",
            fødselsdato = it.person.foedsel.first().foedselsdato
        ) }
    }

    private val callId: String get() = UUID.randomUUID().toString().also { log.info("calling dolly with call-id $it") }
}

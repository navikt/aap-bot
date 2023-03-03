package aap.bot.devtools

import aap.bot.http.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.net.URL
import java.time.LocalDate

data class DevtoolsConfig(
    val host: URL,
)

internal class DevtoolsClient(private val config: DevtoolsConfig) {
    private val httpClient: HttpClient = HttpClientFactory.create(LogLevel.ALL)

    suspend fun delete(personident: String): Boolean {
        val response = httpClient.delete("${config.host}/$personident") {
            contentType(ContentType.Application.Json)
        }
        val statuses = response.body<List<TombstoneStatus>>()

        require(response.status == HttpStatusCode.OK) { "Feil http status fra devtools: ${response.status}" }
        return statuses.all { it.deleted }
    }

    suspend fun getTestpersoner(): List<TestPerson> {
        val response = httpClient.get("${config.host}/dolly") {
            accept(ContentType.Application.Json)
            url { parameters.append("gruppe", "5865") }
        }
        require(response.status == HttpStatusCode.OK)
        return response.body()
    }
}

data class TombstoneStatus(
    val topic: String,
    val deleted: Boolean
)

data class TestPerson(
    val fødselsnummer: String,
    val navn: String,
    val fødselsdato: LocalDate,
)
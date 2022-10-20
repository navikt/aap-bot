package aap.bot.devtools

import aap.bot.http.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.net.URL

data class DevtoolsConfig(
    val host: URL,
)

internal class DevtoolsClient(private val config: DevtoolsConfig) {
    private val httpClient: HttpClient = HttpClientFactory.create(LogLevel.ALL)

    suspend fun delete(personident: String) {
        val response = httpClient.delete("${config.host}/$personident") {
            contentType(ContentType.Application.Json)
        }
        val statuses = response.body<List<TombstoneStatus>>()

        require(response.status == HttpStatusCode.OK) { "Feil http status fra devtools: ${response.status}" }
        require(statuses.all { it.deleted }) { "Ikke alle topics ble tombstona: $statuses" }
    }
}

data class TombstoneStatus(
    val topic: String,
    val deleted: Boolean
)

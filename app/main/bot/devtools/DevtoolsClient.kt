package bot.devtools

import bot.http.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalDate

data class DevtoolsConfig(
    val host: URL,
)

private val secureLog = LoggerFactory.getLogger("secureLog")

internal class DevtoolsClient(private val config: DevtoolsConfig) {
    private val httpClient: HttpClient = HttpClientFactory.create(LogLevel.ALL)

    suspend fun delete(personident: String): Boolean {
        val response = httpClient.delete("${config.host}/$personident") {
            contentType(ContentType.Application.Json)
        }

        require(response.status == HttpStatusCode.OK) {
            """
                Wrong HTTP Status from devtools: ${response.status}
                Message: ${response.bodyAsText()}
            """
        }

        return response.body<List<TombstoneStatus>>().all { it.deleted }.also {
            if (!it) secureLog.warn("Søker $personident ble ikke slettet")
        }
    }

    suspend fun getTestpersoner(): List<TestPerson> {
        val response = httpClient.get("${config.host}/dolly") {
            accept(ContentType.Application.Json)
            url { parameters.append("gruppe", "5865") }
        }

        require(response.status == HttpStatusCode.OK) {
            """
                Wrong HTTP Status from devtools: ${response.status}
                Message: ${response.bodyAsText()}
            """
        }

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
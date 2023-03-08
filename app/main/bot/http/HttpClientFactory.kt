package bot.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.jackson.*
import org.slf4j.LoggerFactory

object HttpClientFactory {

    fun create(loglevel: LogLevel = LogLevel.BODY): HttpClient = HttpClient(CIO) {
        install(HttpTimeout)
        install(HttpRequestRetry)

        install(Logging) {
            level = loglevel
            logger = SecureLogger
        }

        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }
    }
}

private object SecureLogger : Logger {
    private val secureLog = LoggerFactory.getLogger("secureLog")

    override fun log(message: String) = secureLog.info(message)
}

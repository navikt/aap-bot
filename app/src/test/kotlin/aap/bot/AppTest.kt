package aap.bot

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.kafka.streams.v2.test.KStreamsMock
import org.intellij.lang.annotations.Language

//class AppTest {
//
//    @Test
//    fun app() {
//        Mocks().use {
//            testApplication {
//                environment { config = it.environment }
//                application {
//                    bot(it.kafka)
//                }
//            }
//        }
//    }
//
//}

private class Mocks : AutoCloseable {
    private val oAuth2 = embeddedServer(Netty, port = 0, module = Application::azureAdMock).apply { start() }

    val kafka = KStreamsMock()

    companion object {
        val NettyApplicationEngine.port get() = runBlocking { resolvedConnectors() }.first { it.type == ConnectorType.HTTP }.port
    }

    override fun close() {
        oAuth2.stop(0, 0)
//        kafka.close()
    }

    val environment = MapApplicationConfig(
        "TESTBRUKERE_PASSORD" to "passord",
        "AZURE_OPENID_CONFIG_TOKEN_ENDPOINT" to "http://localhost:${oAuth2.port}/token",
        "AZURE_APP_CLIENT_ID" to "id",
        "AZURE_APP_CLIENT_SECRET" to "secret",
        "KAFKA_STREAMS_APPLICATION_ID" to "kelmira",
        "KAFKA_BROKERS" to "mock://kafka",
        "KAFKA_TRUSTSTORE_PATH" to "",
        "KAFKA_KEYSTORE_PATH" to "",
        "KAFKA_CREDSTORE_PASSWORD" to "",
    )
}

internal fun Application.azureAdMock() {
    install(ContentNegotiation) { jackson {} }
    routing {
        post("/token") {
            require(call.receiveText() == "client_id=test&client_secret=test&scope=test&grant_type=client_credentials")
            call.respondText(validToken, ContentType.Application.Json)
        }
    }
}

@Language("JSON")
private const val validToken = """
{
  "token_type": "Bearer",
  "expires_in": 3599,
  "access_token": "very.secure.token"
}
"""

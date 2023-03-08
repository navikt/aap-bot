package bot

import bot.devtools.DevtoolsConfig
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.ktor.client.AzureConfig
import java.net.URL

data class Config(
    val oppgavestyring: OppgavestyringConfig,
    val azure: AzureConfig,
    val kafka: StreamsConfig,
    val devtools: DevtoolsConfig,
)

data class OppgavestyringConfig(
    val host: URL,
    val audience: String,
    val scope: String,
    val testbrukerPassord: String,
)

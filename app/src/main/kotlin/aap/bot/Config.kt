package aap.bot

import aap.bot.devtools.DevtoolsConfig
import aap.bot.dolly.DollyConfig
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.ktor.client.AzureConfig
import java.net.URL

data class Config(
    val oppgavestyring: OppgavestyringConfig,
    val azure: AzureConfig,
    val kafka: StreamsConfig,
    val dolly: DollyConfig,
    val devtools: DevtoolsConfig,
)

data class OppgavestyringConfig(
    val host: URL,
    val audience: String,
    val scope: String,
    val testbrukerPassord: String,
)

package aap.bot

import aap.bot.devtools.DevtoolsConfig
import aap.bot.dolly.DollyConfig
import no.nav.aap.kafka.streams.KStreamsConfig
import no.nav.aap.ktor.client.AzureConfig
import java.net.URL

data class Config(
    val oppgavestyring: OppgavestyringConfig,
    val azure: AzureConfig,
    val kafka: KStreamsConfig,
    val dolly: DollyConfig,
    val devtools: DevtoolsConfig,
)

data class OppgavestyringConfig(
    val host: URL,
    val audience: String,
    val scope: String,
    val testbrukerPassord: String,
)

package aap.bot.oppgavestyring

import aap.bot.OppgavestyringConfig
import aap.bot.http.HttpClientFactory
import io.ktor.client.plugins.logging.*
import no.nav.aap.ktor.client.AzureAdTokenProvider
import no.nav.aap.ktor.client.AzureConfig

internal class TokenProvider(
    private val oppgavestyring: OppgavestyringConfig,
    azure: AzureConfig,
) {

    private val tokenProvider = AzureAdTokenProvider(
        config = azure,
        scope = oppgavestyring.scope,
        client = HttpClientFactory.create(LogLevel.ALL),
    )

    internal suspend fun getAccessToken(bruker: Testbruker): String =
        tokenProvider.getUsernamePasswordToken(bruker.epost, oppgavestyring.testbrukerPassord)
}
package aap.bot.oppgavestyring

import aap.bot.OppgavestyringConfig
import aap.bot.http.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.aap.ktor.client.AzureAdTokenProvider
import no.nav.aap.ktor.client.AzureConfig

internal class OppgavestyringClient(
    private val oppgavestyring: OppgavestyringConfig,
    azure: AzureConfig,
) {
    private val tokenProvider: TokenProvider = TokenProvider(oppgavestyring, azure)
    private val httpClient: HttpClient = HttpClientFactory.create(LogLevel.ALL)

    /**
     * Sendes inn først
     */
    suspend fun løsningInngangsvilkår(
        personident: String,
        bruker: Testbruker = Testbruker.SAKSBEHANDLER,
    ) {
        val inngangsvilkår = Løsninger.inngangsvilkår(bruker.ident).single()
        send(personident, inngangsvilkår.path, bruker, inngangsvilkår.data)
    }

    /**
     * Etter inngangsvilkår
     */
    suspend fun løsningLokalkontor(
        personident: String,
        bruker: Testbruker = Testbruker.VEILEDER_GAMLEOSLO_NAVKONTOR,
    ) {
        Løsninger.fraLokalkontor(bruker.ident).forEach { (path, løsning) ->
            send(personident, path, bruker, løsning)
        }
    }

    /**
     * Etter løsning lokalkontor
     */
    suspend fun kvalitetssikreLokalkontor(
        personident: String,
        bruker: Testbruker = Testbruker.FATTER,
    ) {
        Kvalitetssikringer.lokalkontor(bruker).forEach { (path, kvalitetssikring) ->
            send(personident, path, bruker, kvalitetssikring)
        }
    }

    /**
     * Etter kvalitetssikring lokalkontor
     */
    suspend fun løsningNAY(
        personident: String,
        bruker: Testbruker = Testbruker.SAKSBEHANDLER,
    ) {
        Løsninger.resten(bruker.ident).forEach { (path, løsning) ->
            send(personident, path, bruker, løsning)
        }
    }

    /**
     * Etter løsning NAY
     */
    suspend fun kvalitetssikreNAY(
        personident: String,
        bruker: Testbruker = Testbruker.BESLUTTER,
    ) {
        Kvalitetssikringer.nay(bruker).forEach { (path, kvalitetssikring) ->
            send(personident, path, bruker, kvalitetssikring)
        }
    }

    /**
     * Til slutt
     */
    suspend fun iverksett(personident: String) {
        val response = httpClient.post("${oppgavestyring.host}/sak/$personident/iverksett") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenProvider.getAccessToken(Testbruker.BESLUTTER))
        }
        require(response.status == HttpStatusCode.OK)
    }

    private suspend fun send(personident: String, path: String, bruker: Testbruker, body: Any) {
        val response = httpClient.post("${oppgavestyring.host}/sak/$personident/$path") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenProvider.getAccessToken(bruker))
            setBody(body)
        }
        require(response.status == HttpStatusCode.OK)
    }

    // todo: sende inn meldeplikt
}

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

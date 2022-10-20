package aap.bot.oppgavestyring

import aap.bot.OppgavestyringConfig
import aap.bot.http.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.aap.ktor.client.AzureConfig
import no.nav.aap.ktor.client.HttpClientUserLoginTokenProvider

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
        bruker: Testbruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR,
    ) {
        Løsninger.inngangsvilkår(bruker.ident).forEach { (path, løsning) ->
            send(personident, path, bruker, løsning)
        }
    }

    /**
     * Etter inngangsvilkår
     */
    suspend fun løsningLokalkontor(
        personident: String,
        bruker: Testbruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR,
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
        bruker: Testbruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR,
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
        bruker: Testbruker = Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR,
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
        bruker: Testbruker = Testbruker.BESLUTTER_OG_FATTER_ALLE_NAVKONTOR,
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
            bearerAuth(tokenProvider.getAccessToken(Testbruker.SAKSBEHANDLER_OG_VEILEDER_ALLE_NAVKONTOR))
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
}

internal class TokenProvider(
    private val oppgavestyring: OppgavestyringConfig,
    azure: AzureConfig,
) {
    private val tokenProvider = HttpClientUserLoginTokenProvider(azure, oppgavestyring.scope)

    internal suspend fun getAccessToken(bruker: Testbruker): String =
        tokenProvider.getToken(bruker.epost, oppgavestyring.testbrukerPassord)
}

package aap.bot.oppgavestyring

import aap.bot.OppgavestyringConfig
import aap.bot.http.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.aap.ktor.client.AzureConfig
import java.time.LocalDate
import java.util.*

internal class OppgavestyringClient(
    private val oppgavestyring: OppgavestyringConfig,
    azure: AzureConfig,
) {
    private val tokenProvider: TokenProvider = TokenProvider(oppgavestyring, azure)
    private val httpClient: HttpClient = HttpClientFactory.create(LogLevel.ALL)

    /** Sendes inn til slutt */
    suspend fun iverksett(personident: String) {
        val response = httpClient.post("${oppgavestyring.host}/api/sak/$personident/iverksett") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenProvider.getAccessToken(Testbruker.BESLUTTER))
        }
        require(response.status == HttpStatusCode.OK)
    }

    // todo: sende inn meldeplikt

    /** Sendes inn først */
    suspend fun løsningInngangsvilkår(personident: String) {
        // TODO: Skal vi bare svarer på 11_2 dersom LovMe svarer UAVKLART, eller kan man ALLTID svare JA
        send(
            personident = personident,
            path = "losning/inngangsvilkar",
            bruker = Testbruker.SAKSBEHANDLER,
            body = Inngangsvilkår(
                Inngangsvilkår.Løsning_11_2("JA"),
                Inngangsvilkår.Løsning_11_3(true),
                Inngangsvilkår.Løsning_11_4(true)
            )
        )
    }

    /** Etter inngangsvilkår */
    suspend fun løsningLokalkontor(personident: String) {
        send(
            personident = personident,
            path = "losning/paragraf_11_5",
            bruker = Testbruker.VEILEDER_GAMLEOSLO_NAVKONTOR,
            body = Løsning_11_5(
                kravOmNedsattArbeidsevneErOppfylt = true,
                nedsettelseSkyldesSykdomEllerSkade = true,
                kilder = listOf(),
                legeerklæringDato = null,
                sykmeldingDato = null,
                kravOmNedsattArbeidsevneErOppfyltBegrunnelse = "fritekst",
                nedsettelseSkyldesSykdomEllerSkadeBegrunnelse = "fritekst"
            )
        )

        send(
            personident = personident,
            path = "innstilling/paragraf_11_6",
            bruker = Testbruker.VEILEDER_GAMLEOSLO_NAVKONTOR,
            body = Innstilling_11_6(
                harBehovForBehandling = true,
                harBehovForTiltak = true,
                harMulighetForÅKommeIArbeid = true,
                individuellBegrunnelse = null,
            )
        )
    }

    /** Etter løsning lokalkontor */
    suspend fun kvalitetssikreLokalkontor(personident: String) {
        send(
            personident = personident,
            path = "kvalitetssikre/paragraf_11_5",
            bruker = Testbruker.FATTER,
            body = Kvalitetssikring_11_5(
                løsningId = UUID.randomUUID(),
                kravOmNedsattArbeidsevneErGodkjent = true,
                kravOmNedsattArbeidsevneErGodkjentBegrunnelse = "Godkjent",
                nedsettelseSkyldesSykdomEllerSkadeErGodkjent = true,
                nedsettelseSkyldesSykdomEllerSkadeErGodkjentBegrunnelse = "Godkjent"
            )
        )
    }

    /** Etter kvalitetssikring lokalkontor */
    suspend fun løsningNAY(personident: String) {
        send(
            personident = personident,
            path = "losning/paragraf_11_6",
            bruker = Testbruker.SAKSBEHANDLER,
            body = Løsning_11_6(
                harBehovForBehandling = true,
                harBehovForTiltak = true,
                harMulighetForÅKommeIArbeid = true,
                individuellBegrunnelse = null,
            )
        )

        send(
            personident = personident,
            path = "losning/paragraf_11_19",
            bruker = Testbruker.SAKSBEHANDLER,
            body = Løsning_11_19(
                beregningsdato = LocalDate.now(),
                grunnForDato = "tiden den er nå"
            ),
        )

        send(
            personident = personident,
            path = "losning/paragraf_22_13",
            bruker = Testbruker.SAKSBEHANDLER,
            body = Løsning_22_13(
                bestemmesAv = "soknadstidspunkt",
                unntak = "unntak",
                unntaksbegrunnelse = "NAV har gitt mangelfulle eller misvisende opplysninger",
                manueltSattVirkningsdato = LocalDate.now(),
                begrunnelseForAnnet = null
            ),
        )
    }

    /** Etter løsning NAY */
    suspend fun kvalitetssikreNAY(personident: String) {
        send(
            personident = personident,
            path = "kvalitetssikre/paragraf_11_2",
            bruker = Testbruker.BESLUTTER,
            body = Kvalitetssikring_11_2(
                løsningId = UUID.randomUUID(),
                erGodkjent = true,
                begrunnelse = "Godkjent"
            )
        )

        send(
            personident = personident,
            path = "kvalitetssikre/paragraf_11_3",
            bruker = Testbruker.BESLUTTER,
            body = Kvalitetssikring_11_3(
                løsningId = UUID.randomUUID(),
                erGodkjent = true,
                begrunnelse = "Godkjent"
            )
        )

        send(
            personident = personident,
            path = "kvalitetssikre/paragraf_11_4_ledd2Og3",
            bruker = Testbruker.BESLUTTER,
            body = Kvalitetssikring_11_4_ledd2og3(
                løsningId = UUID.randomUUID(),
                erGodkjent = true,
                begrunnelse = "Godkjent"
            )
        )

        send(
            personident = personident,
            path = "kvalitetssikre/paragraf_11_6",
            bruker = Testbruker.BESLUTTER,
            body = Kvalitetssikring_11_6(
                løsningId = UUID.randomUUID(),
                erGodkjent = true,
                begrunnelse = "Godkjent"
            )
        )

        send(
            personident = personident,
            path = "kvalitetssikre/paragraf_11_19",
            bruker = Testbruker.BESLUTTER,
            body = Kvalitetssikring_11_19(
                løsningId = UUID.randomUUID(),
                erGodkjent = true,
                begrunnelse = "Godkjent"
            )
        )

        send(
            personident = personident,
            path = "kvalitetssikre/paragraf_22_13",
            bruker = Testbruker.BESLUTTER,
            body = Kvalitetssikring_22_13(
                løsningId = UUID.randomUUID(),
                erGodkjent = true,
                begrunnelse = "Godkjent"
            )
        )
    }

    private suspend fun send(personident: String, path: String, bruker: Testbruker, body: Any) {
        val response = httpClient.post("${oppgavestyring.host}/api/sak/$personident/$path") {
            contentType(ContentType.Application.Json)
            bearerAuth(tokenProvider.getAccessToken(bruker))
            setBody(body)
        }
        require(response.status == HttpStatusCode.OK)
    }
}

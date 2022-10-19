package aap.bot.dolly

import java.time.LocalDate

data class DollyResponsePerson(
    val fødselsnummer: String,
    val navn: String,
    val fødselsdato: LocalDate,
)

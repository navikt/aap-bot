package bot.søknad

import no.nav.aap.dto.kafka.Medlemskap
import no.nav.aap.dto.kafka.Studier
import no.nav.aap.dto.kafka.SøknadKafkaDto
import java.time.LocalDate
import java.time.LocalDateTime

object Søknader {

    fun generell(fødselsdato: LocalDate): SøknadKafkaDto =
        SøknadKafkaDto(
            sykepenger = true,
            ferie = null,
            studier = Studier(
                erStudent = null,
                kommeTilbake = null,
                vedlegg = emptyList(),
            ),
            medlemsskap = Medlemskap(
                boddINorgeSammenhengendeSiste5 = true,
                jobbetUtenforNorgeFørSyk = null,
                jobbetSammenhengendeINorgeSiste5 = null,
                iTilleggArbeidUtenforNorge = null,
                utenlandsopphold = emptyList(),
            ),
            registrerteBehandlere = emptyList(),
            andreBehandlere = emptyList(),
            yrkesskadeType = SøknadKafkaDto.Yrkesskade.NEI,
            utbetalinger = null,
            tilleggsopplysninger = null,
            registrerteBarn = emptyList(),
            andreBarn = emptyList(),
            vedlegg = emptyList(),
            fødselsdato = fødselsdato,
            innsendingTidspunkt = LocalDateTime.now(),
        )
}
package aap.bot.oppgavestyring

import no.nav.aap.dto.kafka.Kvalitetssikring_11_19KafkaDto
import no.nav.aap.dto.kafka.Kvalitetssikring_11_2KafkaDto
import no.nav.aap.dto.kafka.Kvalitetssikring_11_3KafkaDto
import no.nav.aap.dto.kafka.Kvalitetssikring_11_4_ledd2_ledd3KafkaDto
import no.nav.aap.dto.kafka.Kvalitetssikring_11_6KafkaDto
import no.nav.aap.dto.kafka.Kvalitetssikring_22_13KafkaDto
import java.time.LocalDateTime
import java.util.*

data class Kvalitetssikringer(
    val path: String,
    val data: Any,
) {
    companion object {
        fun lokalkontor(kvalitetssikrer: Testbruker) = listOf(
            Kvalitetssikringer(
                path = "kvalitetssikre/paragraf_11_5",
                data = Kvalitetssikring_11_2KafkaDto(
                    løsningId = UUID.randomUUID(),
                    kvalitetssikretAv = kvalitetssikrer.ident,
                    tidspunktForKvalitetssikring = LocalDateTime.now(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            )
        )

        fun nay(kvalitetssikrer: Testbruker) = listOf(
            Kvalitetssikringer(
                path = "kvalitetssikre/paragraf_11_2",
                data = Kvalitetssikring_11_2KafkaDto(
                    løsningId = UUID.randomUUID(),
                    kvalitetssikretAv = kvalitetssikrer.ident,
                    tidspunktForKvalitetssikring = LocalDateTime.now(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            ),
            Kvalitetssikringer(
                path = "kvalitetssikre/paragraf_11_3",
                data = Kvalitetssikring_11_3KafkaDto(
                    løsningId = UUID.randomUUID(),
                    kvalitetssikretAv = kvalitetssikrer.ident,
                    tidspunktForKvalitetssikring = LocalDateTime.now(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            ),
            Kvalitetssikringer(
                path = "kvalitetssikre/paragraf_11_4_ledd2Og3",
                data = Kvalitetssikring_11_4_ledd2_ledd3KafkaDto(
                    løsningId = UUID.randomUUID(),
                    kvalitetssikretAv = kvalitetssikrer.ident,
                    tidspunktForKvalitetssikring = LocalDateTime.now(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            ),
            Kvalitetssikringer(
                path = "kvalitetssikre/paragraf_11_6",
                data = Kvalitetssikring_11_6KafkaDto(
                    løsningId = UUID.randomUUID(),
                    kvalitetssikretAv = kvalitetssikrer.ident,
                    tidspunktForKvalitetssikring = LocalDateTime.now(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            ),
            Kvalitetssikringer(
                path = "kvalitetssikre/paragraf_11_19",
                data = Kvalitetssikring_11_19KafkaDto(
                    løsningId = UUID.randomUUID(),
                    kvalitetssikretAv = kvalitetssikrer.ident,
                    tidspunktForKvalitetssikring = LocalDateTime.now(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            ),
            Kvalitetssikringer(
                path = "kvalitetssikre/paragraf_22_13",
                data = Kvalitetssikring_22_13KafkaDto(
                    løsningId = UUID.randomUUID(),
                    kvalitetssikretAv = kvalitetssikrer.ident,
                    tidspunktForKvalitetssikring = LocalDateTime.now(),
                    erGodkjent = true,
                    begrunnelse = "Godkjent"
                )
            )
        )
    }
}

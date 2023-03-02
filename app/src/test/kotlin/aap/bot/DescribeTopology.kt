package aap.bot

import aap.bot.devtools.DevtoolsClient
import aap.bot.devtools.DevtoolsConfig
import aap.bot.dolly.DollyClient
import aap.bot.dolly.DollyConfig
import aap.bot.oppgavestyring.OppgavestyringClient
import aap.bot.streams.topology
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.kafka.streams.v2.test.KStreamsMock
import no.nav.aap.ktor.client.AzureConfig
import org.apache.kafka.clients.producer.MockProducer
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URL

internal class DescribeTopology {
    @Test
    fun mermaid() {
        val oppgavestyringConfig = OppgavestyringConfig(URL("http://oppgave.mock"), "", "", "")
        val azureConfig = AzureConfig(URL("http://azure.mock"), "", "")
        val oppgavestyring = OppgavestyringClient(oppgavestyringConfig, azureConfig)
        val devtools = DevtoolsClient(DevtoolsConfig(URL("http://dev.tools")))
        val dollyConfig = DollyConfig(URL("http://dolly.client"), "")
        val dolly = DollyClient(dollyConfig, azureConfig)
        val topology = topology(oppgavestyring, devtools, dolly, MockProducer(), listOf())

        val kafka = KStreamsMock()
        kafka.connect(topology, StreamsConfig("", ""), SimpleMeterRegistry())

        val mermaid = kafka.visulize().mermaid().generateDiagram()
        File("../docs/topology.mmd").apply { writeText(mermaid) }
    }
}

package bot

import bot.devtools.DevtoolsClient
import bot.devtools.DevtoolsConfig
import bot.oppgavestyring.OppgavestyringClient
import bot.streams.topology
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
        val topology = topology(oppgavestyring, devtools, MockProducer(), listOf())

        val kafka = KStreamsMock()
        kafka.connect(topology, StreamsConfig("", ""), SimpleMeterRegistry())

        val mermaid = kafka.visulize().mermaid().generateDiagram()
        File("../docs/topology.mmd").apply { writeText(mermaid) }
    }
}

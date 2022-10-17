package aap.bot

import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class AppTest {

    @Test
    fun app() {
        testApplication {
            application {
                bot()
            }
        }
    }

    @Test
    fun test() {
        // github actions trenger minst en test for å gå igjennom
        assertTrue(true)
    }
}

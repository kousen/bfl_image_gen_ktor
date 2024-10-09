package edu.trincoll

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.time.Duration
import kotlin.test.Test

class BFLImageGenerationServiceTest {
    private val service = BFLImageGenerationService()

    @Test
    fun `test generate image`() = runBlocking {
        val request = ImageRequest(
            prompt = """
                Show the annual event called
                the Running of the Chickens,
                where the chickens are released
                into the streets of the town
                of Hartford, Connecticut,
                and the townspeople chase them.
            """.trimIndent(),
            width = 1024,
            height = 768
        )

        val requestId = service.generateImageId(request)
        withTimeout(Duration.ofSeconds(30).toMillis()) {
            service.pollForResult(requestId)
                .collect(::println)
        }
    }
}

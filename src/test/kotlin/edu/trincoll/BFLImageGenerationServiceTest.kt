package edu.trincoll

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.time.Duration
import kotlin.test.Test

class BFLImageGenerationServiceTest {
    private val service = BFLImageGenerationService()

    @Test
    fun `test generate image`() = runBlocking {
        val request = FluxPro11Inputs(
            prompt = """
                A bull in a china shop
                being very careful not
                to break anything
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

package edu.trincoll

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertTimeout
import java.time.Duration
import kotlin.test.Test

class BFLImageGenerationServiceTest {
    private val service = BFLImageGenerationService()

    @Test
    fun `test generate image`() {
        val request = ImageRequest(
            prompt = """
                A dog pirate captain walks the starboard 
                side of his ship, preparing to board a
                merchant vessel.
            """.trimIndent(),
            width = 1024,
            height = 1024
        )

        runBlocking {
            val requestId = service.generateImageId(request)
            assertTimeout(Duration.ofSeconds(60)) {
                runBlocking {
                    service.pollForResult(requestId).collect { status ->
                        println(status)
                    }
                }
            }
        }
    }
}
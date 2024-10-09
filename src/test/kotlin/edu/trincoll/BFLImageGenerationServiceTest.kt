package edu.trincoll

import kotlinx.coroutines.flow.takeWhile
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
                Sabretooth tigers playing happily in the snow
                with baby penguins. The tigers are wearing
                furry hats and the penguins are wearing
                sunglasses. The background is a snowy
                mountain landscape.
            """.trimIndent(),
            width = 1024,
            height = 768
        )

        val requestId = service.generateImageId(request)
        withTimeout(Duration.ofSeconds(30).toMillis()) {
            service.pollForResult(requestId)
                .takeWhile { status ->
                    status.contains("waiting")
                }.collect { status ->
                    println(status)
                }
        }
    }
}
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
                At the Dinosaur Races, animal jockeys
                urge their mounts to victory
            """.trimIndent(),
            width = 1024,
            height = 1024
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
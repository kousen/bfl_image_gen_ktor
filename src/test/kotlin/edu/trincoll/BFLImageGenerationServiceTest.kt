package edu.trincoll

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.time.Duration
import kotlin.test.Test

class BFLImageGenerationServiceTest {
    private val service = BFLImageGenerationService()

    @Test
    fun `test generate image with prompt upsampling`() = runBlocking {
        val request = FluxPro11Inputs(
            prompt = """
                A warrior cat rides a dragon into battle
            """.trimIndent(),
            width = 1024,
            height = 768,
            promptUpsampling = true,
        )

        val requestId = service.generateImageId(request)
        withTimeout(Duration.ofSeconds(30).toMillis()) {
            service.pollForResult(requestId)
                .collect(::println)
        }
    }

    @Test
    fun `test generate and save image`() = runBlocking {
        service.generateAndSaveImage("""
            An ancient tortoise
            with a party hat
            celebrating its 100th birthday
        """.trimIndent())
            .collect { status ->
                println(status)
            }
    }
}

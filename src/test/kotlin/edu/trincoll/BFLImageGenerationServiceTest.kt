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
                A Black Forest Lab.
            """.trimIndent(),
            width = 1280,
            height = 720,
            promptUpsampling = true,
        )

        val requestId = service.generateImageId(request)
        withTimeout(Duration.ofSeconds(30).toMillis()) {
            service.pollForResult(requestId)
                .collect(::println)
        }
    }

    @Test
    fun `test generate 16x9 image`() = runBlocking {
        val request = FluxPro11Inputs(
            prompt = """
            A bunny dressed as a Napoleonic general
            aboard an armored llama, leading a charge into battle,
            as both scream "BANZAI!"
            """.trimIndent(),
            width = 1024,
            height = 576,
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
            A warrior cat riding a dragon
            into battle
        """.trimIndent())
            .collect { status ->
                println(status)
            }
    }
}

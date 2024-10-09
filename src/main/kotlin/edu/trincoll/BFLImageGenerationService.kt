package edu.trincoll

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class ImageRequest(
    val prompt: String,
    val width: Int,
    val height: Int,
)

@Serializable
data class AsyncResponse(val id: String)

@Serializable
data class ImageResponse(
    val id: String,
    val status: Status,
    val result: Result? = null
) {
    @Serializable
    data class Result(
        val sample: String
    )
}

@Serializable
enum class Status {
    Ready, InProgress, TaskNotFound, Failed, Pending, Unknown
}

class BFLImageGenerationService {
    companion object {
        private val API_KEY = System.getenv("BFL_API_KEY")
        private const val BASE_URL = "https://api.bfl.ml/v1"
        private val logger = LoggerFactory.getLogger(BFLImageGenerationService::class.java)
    }

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }
    }

    suspend fun generateImageId(request: ImageRequest) =
        client.post("$BASE_URL/flux-pro-1.1") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            header("x-key", API_KEY)
            setBody(request)
        }.body<AsyncResponse>().id

    fun pollForResult(requestId: String) = flow {
        withTimeoutOrNull(30_000) {
            while (true) {
                val response = client.get("$BASE_URL/get_result?id=$requestId") {
                    accept(ContentType.Application.Json)
                    header("x-key", API_KEY)
                }.body<ImageResponse>()

                val result = when (response.status) {
                    Status.Ready -> {
                        val sampleUrl = response.result?.sample ?: throw IOException("No sample available")
                        val savedFile = downloadAndSaveImage(sampleUrl)
                        "Sample image saved to: ${savedFile.absolutePath}"
                    }

                    Status.TaskNotFound -> "Task not found"
                    Status.Failed -> "Task failed"
                    Status.Unknown -> "Unknown status"
                    Status.Pending, Status.InProgress -> {
                        emit("Task is ${response.status}, waiting...")
                        delay(500)
                        null
                    }
                }

                if (result != null) {
                    emit(result)
                    break
                }
            }
        } ?: emit("Polling timed out")
    }

    suspend fun downloadAndSaveImage(imageUrl: String): File =
        withContext(Dispatchers.IO) {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            val fileName = "generated_image_$timestamp.jpg"
            val outputFile = File("src/main/resources", fileName)
            val response: HttpResponse = client.get(imageUrl)

            if (response.status.value in 200..299) {
                outputFile.writeBytes(response.body())
                logger.info("Image saved successfully to: ${outputFile.absolutePath}")
                outputFile
            } else {
                throw IOException("Failed to download the image. HTTP Status Code: ${response.status.value}")
            }
        }
}

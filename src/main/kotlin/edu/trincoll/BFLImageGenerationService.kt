package edu.trincoll

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.time.format.DateTimeFormatter

@Serializable
data class FluxPro11Inputs(
    val prompt: String,
    val width: Int = 1024,
    val height: Int = 768,
    @SerialName("prompt_upsampling")
    val promptUpsampling: Boolean = false,
    val seed: Int? = null,
    val safetyTolerance: Int? = 2,
)

@Serializable
data class AsyncResponse(val id: String)

@Serializable
data class ResultResponse(
    val id: String,
    val status: Status,
    val result: Result? = null
) {
    @Serializable
    data class Result(
        val sample: String, // URL to the generated image
        val prompt: String
    )
}

@Serializable
enum class Status {
    TaskNotFound,
    Pending,
    RequestModerated,
    ContentModerated,
    Ready,
    Error
}

class BFLImageGenerationService {
    companion object {
        private val API_KEY = System.getenv("BFL_API_KEY")
        private const val BASE_URL = "https://api.bfl.ml/v1"
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.NONE
        }
    }

    suspend fun generateImageId(request: FluxPro11Inputs) =
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
                }.body<ResultResponse>()

                val result = when (response.status) {
                    Status.Ready -> {
                        val sampleUrl = response.result?.sample ?: throw IOException("No sample available")
                        val savedFile = downloadAndSaveImage(sampleUrl)
                        "Image saved to: ${savedFile.path}"
                    }

                    Status.TaskNotFound -> "Task not found"
                    Status.RequestModerated -> "Request moderated"
                    Status.ContentModerated -> "Content moderated"
                    Status.Error -> "Error occurred"
                    Status.Pending -> {
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

    suspend fun downloadAndSaveImage(imageUrl: String): File = withContext(Dispatchers.IO) {
        val timestamp = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toJavaLocalDateTime()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val fileName = "generated_image_$timestamp.jpg"
        val outputFile = File("src/main/resources", fileName).apply {
            val response: HttpResponse = client.get(imageUrl)
            if (response.status.isSuccess()) {
                writeBytes(response.body())
            } else {
                throw IOException("Failed to download the image. HTTP Status Code: ${response.status.value}")
            }
        }
        outputFile
    }
}

package com.sharebite.app.data.remote

import android.util.Base64
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ListingSuggestion(
    val foodName: String = "",
    val description: String = "",
    val category: String = "Other",
    val dietaryType: String = "Regular"
)

@Serializable
private data class GenerateListingRequest(val imageBase64: String, val mimeType: String)

@Serializable
private data class GenerateListingError(val error: String? = null)

class GeminiListingService {
    private val client = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 45_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 45_000
        }
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun generate(imageBytes: ByteArray, mimeType: String = "image/jpeg"): ListingSuggestion {
        val response = client.post("${SupabaseClient.url}/functions/v1/generate-listing") {
            headers {
                append("apikey", SupabaseClient.publishableKey)
                append(HttpHeaders.Authorization, "Bearer ${SupabaseClient.publishableKey}")
            }
            headers { append(HttpHeaders.ContentType, ContentType.Application.Json.toString()) }
            setBody(GenerateListingRequest(Base64.encodeToString(imageBytes, Base64.NO_WRAP), mimeType))
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            val error = runCatching { Json.decodeFromString<GenerateListingError>(body).error }.getOrNull()
            throw IllegalStateException(error ?: "Unable to generate listing details")
        }
        return Json.decodeFromString(body)
    }
}

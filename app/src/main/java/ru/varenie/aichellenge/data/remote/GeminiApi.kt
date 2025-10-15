package ru.varenie.aichellenge.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.varenie.aichellenge.data.dto.GeminiRequest
import ru.varenie.aichellenge.data.dto.GeminiResponse

interface GeminiApi {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") modelName: String,      // имя модели, например "gemini-1.5"
        @Query("key") apiKey: String,          // ключ API
        @Body body: GeminiRequest
    ): GeminiResponse
}
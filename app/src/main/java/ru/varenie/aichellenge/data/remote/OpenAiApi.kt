package ru.varenie.aichellenge.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import ru.varenie.aichellenge.data.dto.ChatGptRequest
import ru.varenie.aichellenge.data.dto.ChatGptResponse

interface OpenAiApi {
    @POST("chat/completions")
    suspend fun generateCompletion(
        @Header("Authorization") authHeader: String,
        @Body body: ChatGptRequest
    ): ChatGptResponse
}

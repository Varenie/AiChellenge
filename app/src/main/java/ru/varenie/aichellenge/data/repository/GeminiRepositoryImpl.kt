package ru.varenie.aichellenge.data.repository

import ru.varenie.aichellenge.BuildConfig
import ru.varenie.aichellenge.data.dto.GeminiContent
import ru.varenie.aichellenge.data.dto.GeminiPart
import ru.varenie.aichellenge.data.dto.GeminiRequest
import ru.varenie.aichellenge.data.dto.GeminiResponse
import ru.varenie.aichellenge.data.remote.GeminiApi
import ru.varenie.aichellenge.domain.repository.GeminiRepository
import javax.inject.Inject

class GeminiRepositoryImpl @Inject constructor(
    private val api: GeminiApi
) : GeminiRepository {
    override suspend fun sendMessage(message: String): GeminiResponse {
        return api.generateContent(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            body = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(message))
                    )
                )
            )
        )
    }
}

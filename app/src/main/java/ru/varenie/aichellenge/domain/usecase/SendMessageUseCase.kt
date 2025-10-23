package ru.varenie.aichellenge.domain.usecase

import com.google.gson.Gson
import ru.varenie.aichellenge.domain.models.Agent
import ru.varenie.aichellenge.domain.models.ChatUiMessage
import ru.varenie.aichellenge.domain.models.Meal
import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: HuggingFaceRepository
) {
    suspend operator fun invoke(userMessage: String, modelId: String): ChatUiMessage {
        val generationResult = repository.sendMessage(userMessage, modelId)

        val meal = try {
            Gson().fromJson(generationResult.text, Meal::class.java)
        } catch (e: Exception) {
            null
        }

        return ChatUiMessage(
            text = generationResult.text,
            agent = Agent.DEVELOPER, // Or some other default
            mealResponse = meal,
            showRaw = false,
            model = generationResult.model,
            responseTime = generationResult.responseTime,
            promptTokens = generationResult.promptTokens,
            completionTokens = generationResult.completionTokens,
            finishReason = generationResult.finishReason
        )
    }
}




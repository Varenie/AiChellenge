package ru.varenie.aichellenge.domain.usecase

import com.google.gson.Gson
import ru.varenie.aichellenge.domain.models.ChatUiMessage
import ru.varenie.aichellenge.domain.models.MealResponse
import ru.varenie.aichellenge.domain.repository.OpenAiRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: OpenAiRepository
) {
    suspend operator fun invoke(userMessage: String): ChatUiMessage {
        val assistantText = repository.sendMessage(userMessage)

        val mealResponse = try {
            Gson().fromJson(assistantText, MealResponse::class.java)
        } catch (e: Exception) {
            null
        }

        return ChatUiMessage(
            text = assistantText,
            isUser = false,
            mealResponse = mealResponse,
            showRaw = false
        )
    }
}




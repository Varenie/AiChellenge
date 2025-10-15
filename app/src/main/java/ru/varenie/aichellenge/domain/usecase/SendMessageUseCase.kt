package ru.varenie.aichellenge.domain.usecase

import ru.varenie.aichellenge.domain.models.ChatUiMessage
import ru.varenie.aichellenge.domain.models.GeminiMessage
import ru.varenie.aichellenge.domain.repository.GeminiRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    suspend operator fun invoke(userMessage: String): String {
        val response = repository.sendMessage(userMessage)

        // Преобразуем DTO в готовый текст
        return response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString("\n") { it.text.orEmpty() }
            ?: "No response"
    }
}



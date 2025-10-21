package ru.varenie.aichellenge.domain.usecase

import jakarta.inject.Inject
import ru.varenie.aichellenge.domain.repository.OpenAiRepository

class SendCustomMessageUseCase @Inject constructor(
    private val repository: OpenAiRepository
) {
    suspend operator fun invoke(
        userMessage: String,
        systemPrompt: String,
        temperature: Float
    ): String {
        val chatUiMessageFromRepo =
            repository.sendCustomMessage(userMessage, systemPrompt, temperature)
        return chatUiMessageFromRepo
    }
}
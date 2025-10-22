package ru.varenie.aichellenge.domain.usecase

import ru.varenie.aichellenge.domain.models.ChatSettings
import ru.varenie.aichellenge.domain.models.GenerationResult
import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository
import javax.inject.Inject

class SendCustomMessageUseCase @Inject constructor(
    private val repository: HuggingFaceRepository
) {
    suspend operator fun invoke(
        userMessage: String,
        chatSettings: ChatSettings,
        modelId: String
    ): GenerationResult {
        return repository.sendCustomMessage(userMessage, chatSettings, modelId)
    }
}
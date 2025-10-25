package ru.varenie.aichellenge.domain.usecase

import ru.varenie.aichellenge.data.TokenCounter
import ru.varenie.aichellenge.domain.models.ChatSettings
import ru.varenie.aichellenge.domain.models.MessageProcessingResult
import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository
import ru.varenie.aichellenge.utils.MAX_REQUEST_TOKENS
import javax.inject.Inject

class SendCustomMessageUseCase @Inject constructor(
    private val repository: HuggingFaceRepository,
    private val tokenCounter: TokenCounter,
    private val summarizeUseCase: SummarizeUseCase
) {
    suspend operator fun invoke(
        userMessage: String,
        chatSettings: ChatSettings,
        modelId: String
    ): MessageProcessingResult {
        val fullMessage = "${chatSettings.systemPrompt}\n\n$userMessage"
        val originalInputTokens = tokenCounter.countTokens(fullMessage)
        var messageToSend = fullMessage
        var isSummarized = false
        var summarizedTokens: Int? = null

        if (originalInputTokens > MAX_REQUEST_TOKENS) {
            messageToSend = summarizeUseCase(fullMessage, modelId, MAX_REQUEST_TOKENS)
            summarizedTokens = tokenCounter.countTokens(messageToSend)
            isSummarized = true
        }

        val generationResult = repository.sendCustomMessage(messageToSend, chatSettings, modelId)

        return MessageProcessingResult(
            generationResult = generationResult,
            isSummarized = isSummarized,
            originalInputTokens = originalInputTokens,
            summarizedTokens = summarizedTokens
        )
    }
}
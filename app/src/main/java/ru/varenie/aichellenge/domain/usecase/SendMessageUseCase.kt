package ru.varenie.aichellenge.domain.usecase

import ru.varenie.aichellenge.data.TokenCounter
import ru.varenie.aichellenge.domain.models.MessageProcessingResult
import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository
import ru.varenie.aichellenge.utils.MAX_REQUEST_TOKENS
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: HuggingFaceRepository,
    private val tokenCounter: TokenCounter,
    private val summarizeUseCase: SummarizeUseCase
) {
    suspend operator fun invoke(userMessage: String, modelId: String): MessageProcessingResult {
        val originalInputTokens = tokenCounter.countTokens(userMessage)
        var messageToSend = userMessage
        var isSummarized = false
        var summarizedTokens: Int? = null
        var originalText: String? = null

        if (originalInputTokens > MAX_REQUEST_TOKENS) {
            originalText = userMessage
            isSummarized = true

            var currentMessage = userMessage
            var currentTokens = originalInputTokens
            var attempts = 0

            // Loop to summarize until the message is within limits or max attempts reached
            while (currentTokens > MAX_REQUEST_TOKENS && attempts < 3) { // Limit attempts to prevent infinite loops
                currentMessage = summarizeUseCase(currentMessage, modelId, MAX_REQUEST_TOKENS)
                currentTokens = tokenCounter.countTokens(currentMessage)
                attempts++
            }
            messageToSend = currentMessage
            summarizedTokens = currentTokens

            // Hard truncation fallback: ensure messageToSend never exceeds MAX_REQUEST_TOKENS
            if (summarizedTokens > MAX_REQUEST_TOKENS) {
                // This is a simple character-based truncation. A more robust solution
                // would involve re-tokenizing and truncating by tokens, but that's more complex.
                val ratio = MAX_REQUEST_TOKENS.toDouble() / summarizedTokens.toDouble()
                val newLength = (messageToSend.length * ratio).toInt()
                messageToSend = messageToSend.substring(0, newLength)
                summarizedTokens =
                    tokenCounter.countTokens(messageToSend) // Recount tokens after truncation
            }
        }

        messageToSend = "$messageToSend\n\nRespond in the language of the query."

        val generationResult = repository.sendMessage(messageToSend, modelId)

        return MessageProcessingResult(
            generationResult = generationResult,
            isSummarized = isSummarized,
            originalInputTokens = originalInputTokens,
            summarizedTokens = summarizedTokens,
            originalText = originalText
        )
    }
}





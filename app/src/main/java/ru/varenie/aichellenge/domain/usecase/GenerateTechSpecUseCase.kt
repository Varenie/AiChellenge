package ru.varenie.aichellenge.domain.usecase

import ru.varenie.aichellenge.domain.models.ChatUiMessage
import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository
import java.util.regex.Pattern
import javax.inject.Inject

class GenerateTechSpecUseCase @Inject constructor(
    private val repository: HuggingFaceRepository
) {
    suspend operator fun invoke(
        userMessage: String,
        memory: String,
        modelId: String
    ): ChatUiMessage {
        val generationResult = repository.generateTechSpec(userMessage, memory, modelId)

        val pattern = Pattern.compile("\\{step:\\s*(\\w+)\\}")
        val matcher = pattern.matcher(generationResult.text)

        val step = if (matcher.find()) matcher.group(1) else null
        val cleanText = matcher.replaceAll("").trim()

        return ChatUiMessage(
            text = cleanText,
            isUser = false,
            techSpecStep = step,
            model = generationResult.model,
            responseTime = generationResult.responseTime,
            promptTokens = generationResult.promptTokens,
            completionTokens = generationResult.completionTokens,
            finishReason = generationResult.finishReason
        )
    }
}

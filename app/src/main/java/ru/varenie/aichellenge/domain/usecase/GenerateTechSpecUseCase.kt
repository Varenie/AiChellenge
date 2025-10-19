package ru.varenie.aichellenge.domain.usecase

import ru.varenie.aichellenge.domain.models.ChatUiMessage
import ru.varenie.aichellenge.domain.repository.OpenAiRepository
import java.util.regex.Pattern
import javax.inject.Inject

class GenerateTechSpecUseCase @Inject constructor(
    private val repository: OpenAiRepository
) {
    suspend operator fun invoke(userMessage: String, memory: String): ChatUiMessage {
        val assistantText = repository.generateTechSpec(userMessage, memory)

        val pattern = Pattern.compile("\\{step:\\s*(\\w+)\\}")
        val matcher = pattern.matcher(assistantText)

        val step = if (matcher.find()) matcher.group(1) else null
        val cleanText = matcher.replaceAll("").trim()

        return ChatUiMessage(
            text = cleanText,
            isUser = false,
            techSpecStep = step
        )
    }
}

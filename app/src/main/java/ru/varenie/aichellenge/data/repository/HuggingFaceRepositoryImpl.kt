package ru.varenie.aichellenge.data.repository

import ru.varenie.aichellenge.data.remote.HuggingFaceClient
import ru.varenie.aichellenge.domain.models.GenerationResult
import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository
import javax.inject.Inject

class HuggingFaceRepositoryImpl @Inject constructor(
    private val client: HuggingFaceClient
) : HuggingFaceRepository {
    override suspend fun sendMessage(message: String, modelId: String): GenerationResult {
        return client.generateText(modelId, message)
    }

    override suspend fun generateTechSpec(
        message: String,
        memory: String,
        modelId: String
    ): GenerationResult {
        val fullMessage = """
Вот история нашего диалога:
---
$memory
---

Мое новое сообщение: $message
""".trimIndent()

        return client.generateText(modelId, fullMessage)
    }

    override suspend fun sendCustomMessage(
        message: String,
        systemPrompt: String,
        temperature: Float,
        modelId: String
    ): GenerationResult {
        val fullMessage = "$systemPrompt\n\n$message"
        return client.generateText(modelId, fullMessage)
    }

    override suspend fun generateText(modelName: String, prompt: String): GenerationResult {
        return client.generateText(modelName, prompt)
    }
}


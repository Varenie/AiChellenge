package ru.varenie.aichellenge.domain.repository

import ru.varenie.aichellenge.domain.models.ChatSettings
import ru.varenie.aichellenge.domain.models.GenerationResult

interface HuggingFaceRepository {
    suspend fun sendMessage(message: String, modelId: String): GenerationResult
    suspend fun generateTechSpec(message: String, memory: String, modelId: String): GenerationResult
    suspend fun sendCustomMessage(
        message: String,
        chatSettings: ChatSettings,
        modelId: String
    ): GenerationResult

    suspend fun generateText(modelName: String, prompt: String): GenerationResult
}
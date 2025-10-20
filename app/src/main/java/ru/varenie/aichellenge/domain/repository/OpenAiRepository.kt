package ru.varenie.aichellenge.domain.repository

interface OpenAiRepository {
    suspend fun sendMessage(message: String): String
    suspend fun generateTechSpec(message: String, memory: String): String
    suspend fun sendCustomMessage(message: String, systemPrompt: String, temperature: Float): String
}

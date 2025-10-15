package ru.varenie.aichellenge.domain.repository

import ru.varenie.aichellenge.data.dto.GeminiResponse

interface GeminiRepository {
    suspend fun sendMessage(message: String): GeminiResponse
}
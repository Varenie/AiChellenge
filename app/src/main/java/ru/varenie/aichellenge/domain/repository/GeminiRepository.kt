package ru.varenie.aichellenge.domain.repository

interface GeminiRepository {
    suspend fun sendMessage(message: String): String
}
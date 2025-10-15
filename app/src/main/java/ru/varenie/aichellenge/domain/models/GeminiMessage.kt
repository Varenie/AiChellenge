package ru.varenie.aichellenge.domain.models

data class GeminiMessage(
    val role: String,   // "user" или "assistant"
    val text: String
)
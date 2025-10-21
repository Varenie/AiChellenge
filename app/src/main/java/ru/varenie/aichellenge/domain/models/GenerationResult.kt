package ru.varenie.aichellenge.domain.models

data class GenerationResult(
    val text: String,
    val model: String,
    val responseTime: Long,
    val promptTokens: Int,
    val completionTokens: Int,
    val finishReason: String
)

package ru.varenie.aichellenge.domain.models

data class MessageProcessingResult(
    val generationResult: GenerationResult,
    val isSummarized: Boolean,
    val originalInputTokens: Int,
    val summarizedTokens: Int? = null,
    val originalText: String? = null
)
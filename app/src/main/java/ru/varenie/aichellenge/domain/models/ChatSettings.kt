package ru.varenie.aichellenge.domain.models

data class ChatSettings(
    val temperature: Float = 0.7f,
    val systemPrompt: String = ""
)
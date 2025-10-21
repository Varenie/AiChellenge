package ru.varenie.aichellenge.data.dto

data class ChatCompletionsRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = false
)

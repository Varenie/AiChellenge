package ru.varenie.aichellenge.data.dto

data class ChatCompletionsResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

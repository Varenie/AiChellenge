package ru.varenie.aichellenge.data.dto

import com.google.gson.annotations.SerializedName

data class ChatGptRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<ChatMessage>
)

data class ChatMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class ChatGptResponse(
    @SerializedName("choices") val choices: List<ChatChoice>
)

data class ChatChoice(
    @SerializedName("message") val message: ChatMessage
)
package ru.varenie.aichellenge.data.dto

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<GeminiContent>
)

data class GeminiContent(
    @SerializedName("role")
    val role: String,

    @SerializedName("parts")
    val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text")
    val text: String
)
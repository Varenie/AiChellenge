package ru.varenie.aichellenge.data.dto

import com.google.gson.annotations.SerializedName

data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    @SerializedName("content")
    val content: GeminiContentResponse?
)

data class GeminiContentResponse(
    @SerializedName("parts")
    val parts: List<GeminiPartResponse>?
)

data class GeminiPartResponse(
    @SerializedName("text")
    val text: String?
)
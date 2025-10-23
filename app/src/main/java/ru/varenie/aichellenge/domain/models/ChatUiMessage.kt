package ru.varenie.aichellenge.domain.models

import java.util.UUID

data class ChatUiMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,            // исходный текст/JSON
    val agent: Agent,
    val mealResponse: Meal? = null, // распарсенный объект (если есть)
    val techSpecStep: String? = null,      // шаг в сценарии ТЗ
    val showRaw: Boolean = false,           // флаг для показа сырого текста
    val model: String? = null,
    val responseTime: Long = 0,
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val finishReason: String? = null
)
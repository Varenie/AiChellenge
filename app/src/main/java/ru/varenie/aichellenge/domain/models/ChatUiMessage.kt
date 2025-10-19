package ru.varenie.aichellenge.domain.models

import java.util.UUID

data class ChatUiMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,            // исходный текст/JSON
    val isUser: Boolean,
    val mealResponse: MealResponse? = null, // распарсенный объект (если есть)
    val techSpecStep: String? = null,      // шаг в сценарии ТЗ
    val showRaw: Boolean = false           // флаг для показа сырого текста
)
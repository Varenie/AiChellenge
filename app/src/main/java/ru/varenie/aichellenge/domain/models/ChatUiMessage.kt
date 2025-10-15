package ru.varenie.aichellenge.domain.models

import java.util.UUID

data class ChatUiMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean
)
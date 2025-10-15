package ru.varenie.aichellenge.domain.models

data class Message(
    val role: Role,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
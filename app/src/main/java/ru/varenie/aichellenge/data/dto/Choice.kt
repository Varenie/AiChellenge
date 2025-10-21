package ru.varenie.aichellenge.data.dto

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)

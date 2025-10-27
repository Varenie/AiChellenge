package ru.varenie.aichellenge.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class McpResponse(
    val id: String,
    val response: String,
    val timestamp: Long
)
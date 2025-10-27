package ru.varenie.aichellenge.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class McpRequest(
    val id: String,
    val message: String,
    val timestamp: Long
)
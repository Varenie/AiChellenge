package ru.varenie.aichellenge.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Tool(
    val name: String,
    val description: String
)

@Serializable
data class ToolsListResponse(
    val type: String = "tools_list_response",
    val tools: List<Tool>
)
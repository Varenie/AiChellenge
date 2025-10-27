package ru.varenie.aichellenge.data.remote.mcp

import kotlinx.coroutines.flow.Flow
import ru.varenie.aichellenge.domain.models.McpRequest
import ru.varenie.aichellenge.domain.models.McpResponse

interface McpClient {
    val connectionState: Flow<ConnectionState>
    val incomingMessages: Flow<McpResponse>

    suspend fun connect(url: String)
    suspend fun disconnect()
    suspend fun sendMessage(request: McpRequest)
    suspend fun getTools() // Added function

    sealed class ConnectionState {
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        data class Error(val throwable: Throwable) : ConnectionState()
    }
}
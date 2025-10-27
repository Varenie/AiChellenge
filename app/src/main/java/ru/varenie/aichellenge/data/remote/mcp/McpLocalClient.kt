package ru.varenie.aichellenge.data.remote.mcp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.varenie.aichellenge.domain.models.McpRequest
import ru.varenie.aichellenge.domain.models.McpResponse

class McpLocalClient : McpClient {

    private val _connectionState = MutableStateFlow<McpClient.ConnectionState>(McpClient.ConnectionState.Disconnected)
    override val connectionState: StateFlow<McpClient.ConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<McpResponse>()
    override val incomingMessages: SharedFlow<McpResponse> = _incomingMessages.asSharedFlow()

    override suspend fun connect(url: String) {
        delay(500) // Simulate network delay
        _connectionState.value = McpClient.ConnectionState.Connected
    }

    override suspend fun disconnect() {
        delay(200) // Simulate network delay
        _connectionState.value = McpClient.ConnectionState.Disconnected
    }

    override suspend fun sendMessage(request: McpRequest) {
        delay(100) // Simulate network delay
        // Simulate a response
        _incomingMessages.emit(McpResponse(
            id = request.id,
            response = "Local client received: ${request.message}",
            timestamp = System.currentTimeMillis()
        ))
    }

    override suspend fun getTools() {
        // No-op for local client, or simulate a response if needed for testing
        _incomingMessages.emit(
            McpResponse(
                id = java.util.UUID.randomUUID().toString(),
                response = "{\"type\": \"tools_list\", \"tools\": []}", // Simulate empty tools list
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
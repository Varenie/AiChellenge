package ru.varenie.aichellenge.data.remote.mcp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import ru.varenie.aichellenge.domain.models.McpRequest
import ru.varenie.aichellenge.domain.models.McpResponse
import ru.varenie.aichellenge.domain.models.ToolsListResponse

class McpWebSocketClient(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : McpClient {

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow<McpClient.ConnectionState>(McpClient.ConnectionState.Disconnected)
    override val connectionState: StateFlow<McpClient.ConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<McpResponse>()
    override val incomingMessages: SharedFlow<McpResponse> = _incomingMessages.asSharedFlow()

    override suspend fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = McpClient.ConnectionState.Connected
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    try {
                        // Try to parse as ToolsListResponse first
                        val toolsListResponse = json.decodeFromString<ToolsListResponse>(text)
                        if (toolsListResponse.type == "tools_list_response") {
                            _incomingMessages.emit(
                                McpResponse(
                                    id = java.util.UUID.randomUUID().toString(),
                                    response = text, // Store the original JSON for later parsing in ViewModel
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        } else {
                            // If it has a 'type' but not 'tools_list_response', treat as generic McpResponse
                            val mcpResponse = json.decodeFromString<McpResponse>(text)
                            _incomingMessages.emit(mcpResponse)
                        }
                    } catch (e: Exception) {
                        // If parsing as ToolsListResponse or McpResponse fails, treat as plain text
                        _incomingMessages.emit(
                            McpResponse(
                                id = java.util.UUID.randomUUID().toString(),
                                response = text,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        println("Error parsing WebSocket message, treating as plain text: ${e.message}")
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = McpClient.ConnectionState.Disconnected
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = McpClient.ConnectionState.Error(t)
            }
        })
    }

    override suspend fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = McpClient.ConnectionState.Disconnected
    }

    override suspend fun sendMessage(request: McpRequest) {
        webSocket?.send(json.encodeToString(request))
    }

    override suspend fun getTools() {
        val request = McpRequest(
            id = java.util.UUID.randomUUID().toString(),
            type = "get_tools",
            timestamp = System.currentTimeMillis()
        )
        webSocket?.send(json.encodeToString(request))
    }
}
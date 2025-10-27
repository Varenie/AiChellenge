# MCP Client Integration Plan

This plan outlines the steps to integrate an MCP (Multiplayer Chat Protocol) client into the Android application, including both backend connectivity and UI interaction.

## Backend Integration

- [x] **Create `mcp` package:** Create a new package `mcp` under `app/src/main/java/ru/varenie/aichellenge/data/remote/`.
- [x] **Define `McpClient` interface:** Create an interface `McpClient.kt` in the `mcp` package. This interface will define methods for sending/receiving messages and managing connection state.
- [x] **Create `McpWebSocketClient.kt`:** Create `McpWebSocketClient.kt` in the `mcp` package, implementing `McpClient` for WebSocket communication.
- [x] **Create `McpLocalClient.kt`:** Create `McpLocalClient.kt` in the `mcp` package, implementing `McpClient` as a mock for local testing.
- [x] **Update `NetworkModule.kt`:** Modify `NetworkModule.kt` to provide instances of `McpWebSocketClient` (or `McpLocalClient`) and inject it where needed.
- [x] **Add Dependencies:** Review and add necessary dependencies for WebSocket communication and `kotlinx.serialization` to `app/build.gradle.kts`.

## UI Integration

- [x] **Modify `ChatViewModel.kt`:**
    - [x] Inject `McpClient` into `ChatViewModel`.
    - [x] Add functions to `ChatViewModel` to send messages via `McpClient` and observe incoming messages.
    - [x] Manage connection state (connected/disconnected) and expose it to the UI.
- [x] **Modify `ChatScreen.kt`:**
    - [x] Add UI elements to `ChatScreen` for sending messages via the MCP client (e.g., separate input, toggle).
    - [x] Display messages received from the MCP client.
    - [x] Show the connection status of the MCP client.
    - [x] Consider adding a button to connect/disconnect from the MCP server.

## Verification

- [x] **Implement `McpWebSocketClient` and `McpLocalClient`:** Implement the logic for both clients, ensuring correct handling of `McpRequest` and `McpResponse` objects.
- [x] **Build and Test:** Ensure the project builds successfully and the new MCP chat functionality works as expected in the UI.
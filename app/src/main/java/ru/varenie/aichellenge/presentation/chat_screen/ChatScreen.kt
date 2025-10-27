package ru.varenie.aichellenge.presentation.chat_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width // Added import
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.varenie.aichellenge.domain.models.ChatUiMessage
import ru.varenie.aichellenge.presentation.util.copyToClipboard
import ru.varenie.aichellenge.presentation.util.saveAndShareText
import ru.varenie.aichellenge.utils.MAX_REQUEST_TOKENS


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ChatEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is ChatEffect.CopyToClipboard -> {
                    copyToClipboard(context, effect.content)
                    scope.launch {
                        snackbarHostState.showSnackbar("Chat history copied to clipboard")
                    }
                }

                is ChatEffect.SaveTechSpec -> {
                    saveAndShareText(
                        context = context,
                        fileName = "tech_spec.md",
                        content = effect.content,
                        mimeType = "text/markdown",
                        chooserTitle = "Share Tech Spec"
                    )
                }
            }
        }
    }

    val listState = rememberLazyListState()
    var showCustomSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            when (state.mode) {
                                ChatMode.DIET -> "Diet Chat"
                                ChatMode.TECH_SPEC -> "Tech Spec Assistant"
                                ChatMode.CUSTOM -> "Custom Chat"
                                ChatMode.MULTI_AGENT -> "Multi-Agent Chat"
                                ChatMode.MCP_CHAT -> "MCP Chat"
                            }
                        )
                    },
                    actions = {
                        if (state.mode == ChatMode.CUSTOM) {
                            IconButton(onClick = { showCustomSettings = !showCustomSettings }) {
                                Icon(
                                    imageVector = if (showCustomSettings) Icons.Filled.KeyboardArrowUp else Icons.Filled.Settings,
                                    contentDescription = "Toggle Custom Settings"
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(ChatEvent.ToggleModelSelector) }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Select Model"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                TabRow(
                    selectedTabIndex = state.mode.ordinal,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Tab(
                        selected = state.mode == ChatMode.DIET,
                        onClick = { viewModel.onEvent(ChatEvent.SwitchMode(ChatMode.DIET)) },
                        text = { Text("Diet") }
                    )
                    Tab(
                        selected = state.mode == ChatMode.TECH_SPEC,
                        onClick = { viewModel.onEvent(ChatEvent.SwitchMode(ChatMode.TECH_SPEC)) },
                        text = { Text("Tech Spec") }
                    )
                    Tab(
                        selected = state.mode == ChatMode.CUSTOM,
                        onClick = { viewModel.onEvent(ChatEvent.SwitchMode(ChatMode.CUSTOM)) },
                        text = { Text("Custom") }
                    )
                    Tab(
                        selected = state.mode == ChatMode.MULTI_AGENT,
                        onClick = { viewModel.onEvent(ChatEvent.SwitchMode(ChatMode.MULTI_AGENT)) },
                        text = { Text("Multi-Agent") }
                    )
                    Tab(
                        selected = state.mode == ChatMode.MCP_CHAT,
                        onClick = { viewModel.onEvent(ChatEvent.SwitchMode(ChatMode.MCP_CHAT)) },
                        text = { Text("MCP Chat") }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedVisibility(visible = state.isModelSelectorVisible) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.models) { model ->
                        ModelItem(model = model, isSelected = state.selectedModel == model) {
                            viewModel.onEvent(ChatEvent.SelectModel(model))
                        }
                    }
                }
            }
            AnimatedVisibility(visible = state.mode == ChatMode.CUSTOM && showCustomSettings) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("System Prompt:", style = MaterialTheme.typography.labelMedium)
                    TextField(
                        value = state.chatSettings.systemPrompt,
                        onValueChange = { viewModel.onEvent(ChatEvent.UpdateSystemPrompt(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter system prompt") },
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Temperature:", style = MaterialTheme.typography.labelMedium)
                    TextField(
                        value = state.chatSettings.temperature.toString(),
                        onValueChange = {
                            val newTemp = it.toFloatOrNull()
                            if (newTemp != null && newTemp >= 0f) {
                                viewModel.onEvent(ChatEvent.UpdateTemperature(newTemp))
                            } else if (it.isBlank()) {
                                viewModel.onEvent(ChatEvent.UpdateTemperature(0f)) // Or some default/error state
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter temperature (0.0 - N)") },
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            AnimatedVisibility(visible = state.mode == ChatMode.MCP_CHAT) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var mcpUrl by remember { mutableStateOf("ws://10.0.2.2:8080/mcp") } // Default URL

                    Text(
                        text = "MCP Connection: ${state.mcpConnectionState}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = mcpUrl,
                            onValueChange = { mcpUrl = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Enter MCP Server URL") },
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.onEvent(ChatEvent.ConnectMcp(mcpUrl)) },
                            enabled = state.mcpConnectionState is ru.varenie.aichellenge.data.remote.mcp.McpClient.ConnectionState.Disconnected
                        ) {
                            Text("Connect")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.onEvent(ChatEvent.DisconnectMcp) },
                            enabled = state.mcpConnectionState is ru.varenie.aichellenge.data.remote.mcp.McpClient.ConnectionState.Connected
                        ) {
                            Text("Disconnect")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.messages) { message ->
                    ChatBubble(message = message, onEvent = viewModel::onEvent)
                }
            }

            LaunchedEffect(state.messages.size) {
                if (state.messages.isNotEmpty()) {
                    listState.animateScrollToItem(state.messages.lastIndex)
                }
            }

            var text by remember { mutableStateOf("") }
            Text(
                text = "Max input tokens: $MAX_REQUEST_TOKENS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = { Text("Type a message") },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            if (state.mode == ChatMode.MCP_CHAT) {
                                viewModel.onEvent(ChatEvent.SendMcpMessage(text))
                            } else {
                                viewModel.onEvent(ChatEvent.SendMessage(text))
                            }
                            text = ""
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun ModelItem(
    model: ru.varenie.aichellenge.domain.models.Model,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = model.name, modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(imageVector = Icons.Filled.Check, contentDescription = "Selected")
        }
    }
}

@Composable
fun ChatBubble(message: ChatUiMessage, onEvent: (ChatEvent) -> Unit) {
    val isUser = message.agent == ru.varenie.aichellenge.domain.models.Agent.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val color =
        if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor =
        if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = alignment
    ) {
        if (!isUser) {
            Text(
                text = message.agent.name,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .background(color, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
                .clickable { onEvent(ChatEvent.ToggleRaw(message)) }
        ) {
            if (!isUser && message.mealResponse != null) {
                Column {
                    // ... (rest of the meal response code is the same)
                }
            } else {
                // Сообщение пользователя или ассистента
                Column {
                    Text(
                        text = if (message.isSummarized && message.showRaw) message.originalText
                            ?: message.text else message.text,
                        color = textColor
                    )
                    if (isUser && (message.inputTokens ?: 0) > 0) {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                text = "Tokens: ${message.inputTokens ?: 0}",
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor.copy(alpha = 0.6f)
                            )
                            if (message.isSummarized) {
                                Text(
                                    text = "Summarized (Original: ${message.originalInputTokens ?: 0})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    if (!isUser && (message.outputTokens ?: 0) > 0) {
                        Text(
                            text = "Tokens: ${message.outputTokens ?: 0}",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (!isUser && message.techSpecStep == "generate_tz") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onEvent(ChatEvent.RequestSaveTechSpec(message.text)) }) {
                            Text("Save to file")
                        }
                    }
                }
            }
        }
        if (!isUser) {
            // ... (rest of the metadata code is the same)
        }
    }
}

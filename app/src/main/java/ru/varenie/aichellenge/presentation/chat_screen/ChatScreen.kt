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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.varenie.aichellenge.R
import ru.varenie.aichellenge.domain.models.ChatUiMessage
import ru.varenie.aichellenge.presentation.util.copyToClipboard
import ru.varenie.aichellenge.presentation.util.saveAndShareText

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
                            viewModel.onEvent(ChatEvent.SendMessage(text))
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
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .background(color, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (!message.isUser && message.mealResponse != null) {
                Column {
                    // Заголовок с общими калориями и кнопкой
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (!message.showRaw) "Общие калории: ${message.mealResponse.totalCalories}" else "JSON:",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onEvent(ChatEvent.ToggleRaw(message)) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(
                                    if (message.showRaw) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                ),
                                contentDescription = "Toggle view",
                                tint = textColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!message.showRaw) {
                        // Заголовки колонок
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Блюдо",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(120.dp)
                            )
                            Text(
                                "Калории",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                "Белки/Жиры/Углеводы",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(140.dp)
                            )
                            Text(
                                "Вес, г",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Сами блюда
                        message.mealResponse.meals.forEach { meal ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    meal.name,
                                    color = textColor,
                                    modifier = Modifier.width(120.dp)
                                )
                                Text(
                                    "${meal.calories} ккал",
                                    color = textColor,
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(
                                    "Б:${meal.protein} Ж:${meal.fat} У:${meal.carbs}",
                                    color = textColor,
                                    modifier = Modifier.width(140.dp)
                                )
                                val weight = meal.weightGrams?.toString() ?: "-"
                                Text(
                                    weight,
                                    color = textColor,
                                    modifier = Modifier.width(60.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    } else {
                        // JSON view
                        Text(message.text, color = textColor)
                    }
                }
            } else {
                // Сообщение пользователя или ассистента в режиме ТЗ
                Column {
                    Text(text = message.text, color = textColor)
                    if (!message.isUser && message.techSpecStep == "generate_tz") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onEvent(ChatEvent.RequestSaveTechSpec(message.text)) }) {
                            Text("Save to file")
                        }
                    }
                }
            }
        }
        if (!message.isUser) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Model: ${message.model}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Time: ${message.responseTime?.let { "%.2f s".format(it / 1000f) } ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tokens: P:${message.promptTokens ?: "N/A"} C:${message.completionTokens ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Finish: ${message.finishReason ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

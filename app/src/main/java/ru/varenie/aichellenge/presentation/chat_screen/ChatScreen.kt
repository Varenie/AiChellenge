package ru.varenie.aichellenge.presentation.chat_screen

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.varenie.aichellenge.R
import ru.varenie.aichellenge.domain.models.ChatUiMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            if (effect is ChatEffect.ShowError) {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diet Chat") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.messages) { message ->
                    ChatBubble(message = message) {
                        viewModel.onEvent(ChatEvent.ToggleRaw(message))
                    }
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
fun ChatBubble(message: ChatUiMessage, onToggleRaw: () -> Unit) {
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
                            onClick = onToggleRaw,
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
                                Text(
                                    "${meal.weightGrams ?: "-"}",
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
                // Сообщение пользователя
                Text(text = message.text, color = textColor)
            }
        }
    }
}








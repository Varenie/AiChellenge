package ru.varenie.aichellenge.presentation.chat_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.varenie.aichellenge.data.TokenCounter
import ru.varenie.aichellenge.domain.models.Agent
import ru.varenie.aichellenge.domain.models.ChatUiMessage
import ru.varenie.aichellenge.domain.models.MessageProcessingResult
import ru.varenie.aichellenge.domain.models.Model
import ru.varenie.aichellenge.domain.usecase.AgentStreamEvent
import ru.varenie.aichellenge.domain.usecase.GenerateCodeAndTestsUseCase
import ru.varenie.aichellenge.domain.usecase.SendCustomMessageUseCase
import ru.varenie.aichellenge.domain.usecase.SendMessageUseCase
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendCustomMessageUseCase: SendCustomMessageUseCase,
    private val generateCodeAndTestsUseCase: GenerateCodeAndTestsUseCase,
    private val tokenCounter: TokenCounter
) : ViewModel() {

    private val models = listOf(
        Model("openai/gpt-oss-20b", "GPT- 20b"),
        Model(
            "deepseek-ai/DeepSeek-R1:hyperbolic",
            "DeepSeek"
        ),
        Model("baidu/ERNIE-4.5-0.3B-PT", "Ernie"),
    )

    private val _state =
        MutableStateFlow(ChatState(models = models, selectedModel = models.first()))
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ChatEffect>()
    val effect: SharedFlow<ChatEffect> = _effect.asSharedFlow()

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.SendMessage -> sendMessage(event.text)
            is ChatEvent.ToggleRaw -> toggleRaw(event.message)
            is ChatEvent.SwitchMode -> switchMode(event.mode)
            is ChatEvent.ExportChatToClipboard -> exportChatToClipboard()
            is ChatEvent.RequestSaveTechSpec -> {
                viewModelScope.launch {
                    _effect.emit(ChatEffect.SaveTechSpec(event.content))
                }
            }
            is ChatEvent.UpdateSystemPrompt -> updateSystemPrompt(event.systemPrompt)
            is ChatEvent.UpdateTemperature -> updateTemperature(event.temperature)
            is ChatEvent.SelectModel -> selectModel(event.model)
            is ChatEvent.ToggleModelSelector -> toggleModelSelector()
        }
    }

    private fun selectModel(model: Model) {
        _state.update { it.copy(selectedModel = model, isModelSelectorVisible = false) }
    }

    private fun toggleModelSelector() {
        _state.update { it.copy(isModelSelectorVisible = !it.isModelSelectorVisible) }
    }

    private fun updateTemperature(temperature: Float) {
        _state.update { it.copy(chatSettings = it.chatSettings.copy(temperature = temperature)) }
    }

    private fun updateSystemPrompt(systemPrompt: String) {
        _state.update { it.copy(chatSettings = it.chatSettings.copy(systemPrompt = systemPrompt)) }
    }

    private fun exportChatToClipboard() {
        viewModelScope.launch {
            val history = _state.value.messages.joinToString("") { msg ->
                if (msg.agent == Agent.USER) "User: ${msg.text}" else "Assistant (${msg.agent.name}): ${msg.text}"
            }
            _effect.emit(ChatEffect.CopyToClipboard(history))
        }
    }

    private fun switchMode(mode: ChatMode) {
        _state.update { it.copy(mode = mode, messages = emptyList(), memory = "") }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessageTokens = tokenCounter.countTokens(text)
        val userMessage = ChatUiMessage(
            text = text,
            agent = Agent.USER,
            inputTokens = userMessageTokens
        )
        _state.update { it.copy(messages = it.messages + userMessage, isLoading = true) }

        viewModelScope.launch {
            try {
                when (_state.value.mode) {
                    ChatMode.DIET -> handleDietMode(text, userMessage)
                    ChatMode.TECH_SPEC -> handleTechSpecMode(text, userMessage)
                    ChatMode.CUSTOM -> handleCustomMode(text, userMessage)
                    ChatMode.MULTI_AGENT -> sendMessageForMultiAgent(text)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(ChatEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    private suspend fun handleDietMode(text: String, userMessage: ChatUiMessage) {
        val result = sendMessageUseCase(text, _state.value.selectedModel!!.id)
        updateStateWithResult(userMessage, result)
    }

    private suspend fun handleTechSpecMode(text: String, userMessage: ChatUiMessage) {
        val result = sendMessageUseCase(text, _state.value.selectedModel!!.id)
        updateStateWithResult(userMessage, result)
    }

    private suspend fun handleCustomMode(text: String, userMessage: ChatUiMessage) {
        val chatSettings = _state.value.chatSettings
        val result = sendCustomMessageUseCase(
            text,
            chatSettings,
            _state.value.selectedModel!!.id
        )
        updateStateWithResult(userMessage, result)
    }

    private fun updateStateWithResult(
        userMessage: ChatUiMessage,
        result: MessageProcessingResult
    ) {
        val updatedUserMessage = userMessage.copy(
            originalInputTokens = result.originalInputTokens,
            isSummarized = result.isSummarized,
            inputTokens = result.summarizedTokens ?: userMessage.inputTokens,
            originalText = result.originalText
        )

        val assistantMessage = ChatUiMessage(
            text = result.generationResult.text,
            agent = Agent.ASSISTANT,
            model = result.generationResult.model,
            responseTime = result.generationResult.responseTime,
            outputTokens = result.generationResult.completionTokens
        )

        _state.update {
            val updatedMessages = it.messages.map { msg ->
                if (msg.id == userMessage.id) updatedUserMessage else msg
            }
            it.copy(
                messages = updatedMessages + assistantMessage,
                isLoading = false
            )
        }
    }

    private fun sendMessageForMultiAgent(text: String) {
        viewModelScope.launch {
            generateCodeAndTestsUseCase(text, _state.value.selectedModel!!.id)
                .collect { event ->
                    when (event) {
                        is AgentStreamEvent.Typing -> {
                            val typingMessage = ChatUiMessage(
                                id = "typing-${event.agent.name}",
                                text = "${event.agent.name} is typing...",
                                agent = event.agent
                            )
                            _state.update {
                                if (it.messages.none { msg -> msg.id == typingMessage.id }) {
                                    it.copy(messages = it.messages + typingMessage)
                                } else {
                                    it
                                }
                            }
                        }

                        is AgentStreamEvent.Message -> {
                            _state.update { state ->
                                val newMessages = state.messages.map {
                                    if (it.id == "typing-${event.message.agent.name}") {
                                        event.message
                                    } else {
                                        it
                                    }
                                }
                                state.copy(messages = newMessages)
                            }
                        }
                    }
                }
        }
    }

    fun toggleRaw(message: ChatUiMessage) {
        _state.update {
            val newMessages = it.messages.map { m ->
                if (m == message) {
                    m.copy(
                        showRaw = !m.showRaw,
                        text = if (m.showRaw) m.originalText
                            ?: m.text else m.text // Toggle between original and summarized text
                    )
                } else {
                    m
                }

            }
            it.copy(messages = newMessages)
        }
    }
}

enum class ChatMode {
    DIET,
    TECH_SPEC,
    CUSTOM,
    MULTI_AGENT
}

data class ChatState(
    val messages: List<ChatUiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val mode: ChatMode = ChatMode.DIET,
    val memory: String = "",
    val chatSettings: ru.varenie.aichellenge.domain.models.ChatSettings = ru.varenie.aichellenge.domain.models.ChatSettings(),
    val models: List<ru.varenie.aichellenge.domain.models.Model> = emptyList(),
    val selectedModel: ru.varenie.aichellenge.domain.models.Model? = null,
    val isModelSelectorVisible: Boolean = false,
    val typingAgent: ru.varenie.aichellenge.domain.models.Agent? = null
)

sealed class ChatEvent {
    data class SendMessage(val text: String) : ChatEvent()
    data class ToggleRaw(val message: ChatUiMessage) : ChatEvent()
    data class SwitchMode(val mode: ChatMode) : ChatEvent()
    object ExportChatToClipboard : ChatEvent()
    data class RequestSaveTechSpec(val content: String) : ChatEvent()
    data class UpdateSystemPrompt(val systemPrompt: String) : ChatEvent()
    data class UpdateTemperature(val temperature: Float) : ChatEvent()
    data class SelectModel(val model: ru.varenie.aichellenge.domain.models.Model) : ChatEvent()
    object ToggleModelSelector : ChatEvent()
}


sealed class ChatEffect {
    data class ShowError(val message: String) : ChatEffect()
    data class CopyToClipboard(val content: String) : ChatEffect()
    data class SaveTechSpec(val content: String) : ChatEffect()
}

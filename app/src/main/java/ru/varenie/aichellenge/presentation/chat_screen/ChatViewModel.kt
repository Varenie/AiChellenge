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
import ru.varenie.aichellenge.domain.models.ChatUiMessage
import ru.varenie.aichellenge.domain.usecase.GenerateTechSpecUseCase
import ru.varenie.aichellenge.domain.usecase.SendCustomMessageUseCase
import ru.varenie.aichellenge.domain.usecase.SendMessageUseCase
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val generateTechSpecUseCase: GenerateTechSpecUseCase,
    private val sendCustomMessageUseCase: SendCustomMessageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ChatEffect>()
    val effect: SharedFlow<ChatEffect> = _effect.asSharedFlow()

    fun onEvent(event: ChatEvent) {
        when(event) {
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
        }
    }

    private fun updateTemperature(temperature: Float) {
        _state.update { it.copy(temperature = temperature) }
    }

    private fun updateSystemPrompt(systemPrompt: String) {
        _state.update { it.copy(systemPrompt = systemPrompt) }
    }

    private fun exportChatToClipboard() {
        viewModelScope.launch {
            val history = _state.value.messages.joinToString("\n") { msg ->
                if (msg.isUser) "User: ${msg.text}" else "Assistant: ${msg.text}"
            }
            _effect.emit(ChatEffect.CopyToClipboard(history))
        }
    }

    private fun switchMode(mode: ChatMode) {
        _state.update { it.copy(mode = mode, messages = emptyList(), memory = "") }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatUiMessage(text = text, isUser = true)
        _state.update { it.copy(messages = it.messages + userMessage, isLoading = true) }

        viewModelScope.launch {
            try {
                when (_state.value.mode) {
                    ChatMode.DIET -> sendMessageForDiet(text)
                    ChatMode.TECH_SPEC -> sendMessageForTechSpec(text)
                    ChatMode.CUSTOM -> sendMessageForCustom(text)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(ChatEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    private suspend fun sendMessageForDiet(text: String) {
        val assistantMessage = sendMessageUseCase(text)
        _state.update {
            it.copy(
                messages = it.messages + assistantMessage,
                isLoading = false,
            )
        }
    }

    private suspend fun sendMessageForTechSpec(text: String) {
        val currentMemory = _state.value.memory
        val assistantMessage = generateTechSpecUseCase(text, currentMemory)
        val newMemory =
            _state.value.memory + "User: " + text + "\n" + "Assistant: " + assistantMessage.text + "\n"

        _state.update {
            it.copy(
                messages = it.messages + assistantMessage,
                isLoading = false,
                memory = newMemory
            )
        }
    }

    private suspend fun sendMessageForCustom(text: String) {
        val currentSystemPrompt = _state.value.systemPrompt
        val currentTemperature = _state.value.temperature
        val response = sendCustomMessageUseCase(text, currentSystemPrompt, currentTemperature)
        val assistantMessage = ChatUiMessage(text = response, isUser = false)
        val newMemory =
            _state.value.memory + "User: " + text + "\n" + "Assistant: " + assistantMessage.text + "\n"

        _state.update {
            it.copy(
                messages = it.messages + assistantMessage,
                isLoading = false,
                memory = newMemory
            )
        }
    }

    fun toggleRaw(message: ChatUiMessage) {
        _state.update {
            val newMessages = it.messages.map { m ->
                if (m == message) m.copy(showRaw = !m.showRaw) else m
            }
            it.copy(messages = newMessages)
        }
    }
}

enum class ChatMode {
    DIET,
    TECH_SPEC,
    CUSTOM
}

data class ChatState(
    val messages: List<ChatUiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val mode: ChatMode = ChatMode.DIET,
    val memory: String = "",
    val systemPrompt: String = "You are a helpful assistant.",
    val temperature: Float = 0.7f
)

sealed class ChatEvent {
    data class SendMessage(val text: String) : ChatEvent()
    data class ToggleRaw(val message: ChatUiMessage) : ChatEvent()
    data class SwitchMode(val mode: ChatMode) : ChatEvent()
    object ExportChatToClipboard : ChatEvent()
    data class RequestSaveTechSpec(val content: String) : ChatEvent()
    data class UpdateSystemPrompt(val systemPrompt: String) : ChatEvent()
    data class UpdateTemperature(val temperature: Float) : ChatEvent()
}


sealed class ChatEffect {
    data class ShowError(val message: String) : ChatEffect()
    data class CopyToClipboard(val content: String) : ChatEffect()
    data class SaveTechSpec(val content: String) : ChatEffect()
}
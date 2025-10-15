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
import ru.varenie.aichellenge.domain.models.GeminiMessage
import ru.varenie.aichellenge.domain.usecase.SendMessageUseCase
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ChatEffect>()
    val effect: SharedFlow<ChatEffect> = _effect.asSharedFlow()

    fun onEvent(event: ChatEvent) {
        when(event) {
            is ChatEvent.SendMessage -> sendMessage(event.text)
        }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatUiMessage(text = text, isUser = true)
        _state.update { it.copy(messages = it.messages + userMessage, isLoading = true) }

        viewModelScope.launch {
            try {
                val assistantText = sendMessageUseCase(text)

                val geminiMessage = ChatUiMessage(
                    text = assistantText,
                    isUser = false
                )

                _state.update {
                    it.copy(messages = it.messages + geminiMessage, isLoading = false)
                }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(ChatEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

}


data class ChatState(
    val messages: List<ChatUiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ChatEvent {
    data class SendMessage(val text: String) : ChatEvent()
}

sealed class ChatEffect {
    data class ShowError(val message: String) : ChatEffect()
}
package ru.varenie.aichellenge.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.varenie.aichellenge.domain.models.Agent
import ru.varenie.aichellenge.domain.models.ChatUiMessage
import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository

sealed class AgentStreamEvent {
    data class Typing(val agent: Agent) : AgentStreamEvent()
    data class Message(val message: ChatUiMessage) : AgentStreamEvent()
}

class GenerateCodeAndTestsUseCase(private val repository: HuggingFaceRepository) {

    operator fun invoke(userMessage: String, modelId: String): Flow<AgentStreamEvent> = flow {
        // 1. User message
        emit(AgentStreamEvent.Message(ChatUiMessage(text = userMessage, agent = Agent.USER)))

        // 2. Developer agent
        emit(AgentStreamEvent.Typing(Agent.DEVELOPER))
        val developerPrompt =
            "Ты — старший разработчик. Напиши код для следующего запроса: $userMessage. Предоставь только сам код, без каких-либо объяснений, комментариев или разметки. Ответь на том же языке, что и запрос."
        val developerResult = repository.sendMessage(developerPrompt, modelId)
        val developerMessage = ChatUiMessage(
            text = developerResult.text,
            agent = Agent.DEVELOPER,
            responseTime = developerResult.responseTime,
            model = developerResult.model,
            promptTokens = developerResult.promptTokens,
            outputTokens = developerResult.completionTokens,
            finishReason = developerResult.finishReason
        )
        emit(AgentStreamEvent.Message(developerMessage))

        // 3. Tester agent
        if (developerResult.text.isNotBlank()) {
            emit(AgentStreamEvent.Typing(Agent.TESTER))
            val testerPrompt =
                "Кратко опиши, что делает следующий код, в одном предложении. Затем предоставь список тестовых случаев без деталей реализации. Отформатируй тестовые случаи в виде нумерованного списка. Ответь на том же языке, что и запрос.\nКод:\n${developerResult.text}"
            val testerResult = repository.sendMessage(testerPrompt, modelId)
            val testerMessage = ChatUiMessage(
                text = testerResult.text,
                agent = Agent.TESTER,
                responseTime = testerResult.responseTime,
                model = testerResult.model,
                promptTokens = testerResult.promptTokens,
                outputTokens = testerResult.completionTokens,
                finishReason = testerResult.finishReason
            )
            emit(AgentStreamEvent.Message(testerMessage))
        }
    }
}
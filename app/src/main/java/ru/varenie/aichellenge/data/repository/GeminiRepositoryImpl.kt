package ru.varenie.aichellenge.data.repository

import ru.varenie.aichellenge.BuildConfig
import ru.varenie.aichellenge.data.dto.ChatGptRequest
import ru.varenie.aichellenge.data.dto.ChatMessage
import ru.varenie.aichellenge.data.remote.OpenAiApi
import ru.varenie.aichellenge.domain.repository.GeminiRepository
import javax.inject.Inject

class GeminiRepositoryImpl @Inject constructor(
    private val api: OpenAiApi
) : GeminiRepository {
    override suspend fun sendMessage(message: String): String {
        val response = api.generateCompletion(
            authHeader = "Bearer ${BuildConfig.OPENAI_API_KEY}",
            body = ChatGptRequest(
                model = "gpt-4o-mini",
                messages = listOf(
                    ChatMessage(role = "system", content = SYSTEM_PROMPT),
                    ChatMessage(role = "user", content = message)
                )
            )
        )

        // Берём текст из первого выбора
        return response.choices
            .firstOrNull()
            ?.message
            ?.content
            ?.trim()
            ?: "No response"
    }

}

private const val SYSTEM_PROMPT = """
Ты — диетический ассистент. Когда пользователь сообщает, что он съел какие-то блюда, возвращай **JSON с калориями, БЖУ и весом каждого блюда, а также суммарно**.  

Если калории или БЖУ не указаны, оцени их по средним значениям на 100 г, исходя из веса блюда (weightGrams). Если вес не указан, используй 150 г как стандарт.  

Формат JSON:
{
  "totalCalories": 1234,
  "meals": [
    {
      "name": "Лазанья",
      "weightGrams": 200,
      "calories": 300,
      "protein": 12,
      "fat": 20,
      "carbs": 40
    },
    {
      "name": "Бутерброд",
      "weightGrams": 100,
      "calories": 250,
      "protein": 10,
      "fat": 8,
      "carbs": 30
    }
  ]
}

Возвращай JSON **только**, без лишнего текста. 
"""



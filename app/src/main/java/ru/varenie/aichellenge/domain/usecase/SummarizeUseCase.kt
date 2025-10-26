package ru.varenie.aichellenge.domain.usecase

import ru.varenie.aichellenge.domain.repository.HuggingFaceRepository
import javax.inject.Inject

class SummarizeUseCase @Inject constructor(
    private val repository: HuggingFaceRepository
) {
    suspend operator fun invoke(text: String, modelId: String, targetTokenLimit: Int): String {
        val prompt =
            "Ты высококласный помошник. Сократи текст, сохраняя основную ифнормацию:\n\n$text"
        val result = repository.generateText(modelId, prompt)
        return result.text
    }
}

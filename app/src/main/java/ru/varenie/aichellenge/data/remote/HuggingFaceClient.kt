package ru.varenie.aichellenge.data.remote

import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import ru.varenie.aichellenge.BuildConfig
import ru.varenie.aichellenge.data.dto.ChatCompletionsRequest
import ru.varenie.aichellenge.data.dto.ChatCompletionsResponse
import ru.varenie.aichellenge.data.dto.Message
import ru.varenie.aichellenge.domain.models.GenerationResult
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HuggingFaceClient(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {

    suspend fun generateText(modelName: String, prompt: String): GenerationResult {
        val requestBody = ChatCompletionsRequest(
            model = modelName,
            messages = listOf(Message(role = "user", content = prompt))
        )
        val requestJson = gson.toJson(requestBody)

        val request = Request.Builder()
            .url("https://router.huggingface.co/v1/chat/completions")
            .header("Authorization", "Bearer ${BuildConfig.HUGGINGFACE_API_KEY}")
            .post(requestJson.toRequestBody("application/json".toMediaType()))
            .build()

        val startTime = System.currentTimeMillis()

        return suspendCancellableCoroutine { continuation ->
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseTime = System.currentTimeMillis() - startTime
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val huggingFaceResponse =
                                gson.fromJson(responseBody, ChatCompletionsResponse::class.java)
                            if (huggingFaceResponse.choices.isNotEmpty()) {
                                val choice = huggingFaceResponse.choices[0]
                                continuation.resume(
                                    GenerationResult(
                                        text = choice.message.content,
                                        model = huggingFaceResponse.model,
                                        responseTime = responseTime,
                                        promptTokens = huggingFaceResponse.usage.prompt_tokens,
                                        completionTokens = huggingFaceResponse.usage.completion_tokens,
                                        finishReason = choice.finish_reason
                                    )
                                )
                            } else {
                                continuation.resumeWithException(Exception("Empty choices from Hugging Face API"))
                            }
                        } else {
                            continuation.resumeWithException(Exception("Empty response body from Hugging Face API"))
                        }
                    } else {
                        continuation.resumeWithException(IOException("Unexpected code ${response.code} ${response.body?.string()}"))
                    }
                }
            })

            continuation.invokeOnCancellation {
                okHttpClient.newCall(request).cancel()
            }
        }
    }
}

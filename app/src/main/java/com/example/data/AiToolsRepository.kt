package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

sealed interface AiGenerationState {
    data object Idle : AiGenerationState
    data object Loading : AiGenerationState
    data class Streaming(val text: String) : AiGenerationState
    data class Success(val text: String) : AiGenerationState
    data class Error(val message: String) : AiGenerationState
}

class AiToolsRepository {
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    fun streamMvciAiTask(toolName: String, topicOrPrompt: String, languageCode: String): Flow<AiGenerationState> = flow {
        emit(AiGenerationState.Loading)
        val langInstruction = if (languageCode == "hi") {
            "Respond clearly in Hindi (Devanagari script) with English technical terms where helpful."
        } else {
            "Respond clearly in professional English."
        }
        val systemContext = """
            You are the AI Education Assistant for Maa Vaishno Computer Institute (MVCI, https://www.mvci.online).
            Selected AI Tool: $toolName.
            $langInstruction
            Provide accurate, structured, classroom-ready output for institute faculty, students, or administrators.
        """.trimIndent()
        val fullPrompt = "$systemContext\n\nRequest:\n$topicOrPrompt"

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                model.generateContentStream(fullPrompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(AiGenerationState.Streaming(accumulated.toString()))
                    }
                }
            }
            if (accumulated.isNotBlank()) {
                emit(AiGenerationState.Success(accumulated.toString()))
            } else {
                emit(AiGenerationState.Error("Gemini returned an empty response. Please refine your prompt."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI generation timed out", e)
            emit(AiGenerationState.Error("AI request timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation failed", e)
            emit(AiGenerationState.Error(e.localizedMessage ?: "AI generation failed."))
        }
    }

    private companion object {
        const val TAG = "AiToolsRepository"
    }
}

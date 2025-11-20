package uk.ac.tees.mad.reuse.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GeminiRepository @Inject constructor(
){
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyDi6cIPO8_q77RF5mg_UFmQpfP0B42Fz2M"
    )

    fun generateText(prompt: String): Flow<String?> = model.generateContentStream(prompt).map { it.text }.catch { emit(null) }
}
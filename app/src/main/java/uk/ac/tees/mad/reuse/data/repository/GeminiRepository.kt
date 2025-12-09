package uk.ac.tees.mad.reuse.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GeminiRepository @Inject constructor(
){
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyD0BAouUuaqrxnuXcFi60MA49SFrigK35s"
    )
    
    fun generateText(prompt: String): Flow<String?> = model.generateContentStream(prompt).map { it.text }.catch { emit(null) }
}
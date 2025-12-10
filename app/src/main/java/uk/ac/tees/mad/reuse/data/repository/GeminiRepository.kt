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
        modelName = "gemini-2.5-flash-lite",
        apiKey = "AIzaSyD5wN22EOEh1zpVtz7NowyytWtzqAhto7o"
    )

    suspend fun generateS(){
        val a = model.generateContent("Why are you now working?")
        a.let {
            Log.d("Geminis", a.text.toString())
        }
    }
    fun generateText(prompt: String): Flow<String?> = model.generateContentStream(prompt).map { it.text }.catch { emit(null) }
}
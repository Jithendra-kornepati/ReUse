package uk.ac.tees.mad.reuse.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject

class GeminiRepository @Inject constructor(
){
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyDi6cIPO8_q77RF5mg_UFmQpfP0B42Fz2M"
    )

    suspend fun generateText(prompt : String) : String? {
        try {
            val res = model.generateContent(prompt = prompt)
            return res.text
        }catch (e : Exception){
            return null
        }
    }
}
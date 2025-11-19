package uk.ac.tees.mad.reuse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import uk.ac.tees.mad.reuse.data.repository.GeminiRepository

@HiltViewModel
class GenericVM @Inject constructor(
    private val genericRepo: GeminiRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            val res = genericRepo.generateText("Hello Gemini how are you")
            Log.d("Gemini", res.toString())
        }
    }
}
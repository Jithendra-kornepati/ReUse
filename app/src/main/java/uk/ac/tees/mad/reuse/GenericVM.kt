package uk.ac.tees.mad.reuse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import uk.ac.tees.mad.reuse.data.repository.GeminiRepository
import javax.inject.Inject

@HiltViewModel
class GenericVM @Inject constructor(
    private val genericRepo: GeminiRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    fun generateText(prompt: String) {
        viewModelScope.launch {
            _messages.value = _messages.value + Message.User(prompt)

            _messages.value = _messages.value + Message.Assistant("")

            genericRepo.generateText(prompt = prompt).collect { chunk ->
                if (chunk != null) {
                    val updatedMessages = _messages.value.toMutableList()
                    val lastMessage = updatedMessages.last() as Message.Assistant
                    updatedMessages[updatedMessages.size - 1] = lastMessage.copy(text = lastMessage.text + chunk)
                    _messages.value = updatedMessages
                } else {
                    Log.e("GenericVM", "Error in stream")
                }
            }
        }
    }
}

// Sealed class for message types
sealed class Message {
    data class User(val text: String) : Message()
    data class Assistant(val text: String) : Message()
}
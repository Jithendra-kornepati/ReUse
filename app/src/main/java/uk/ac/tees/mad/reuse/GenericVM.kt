package uk.ac.tees.mad.reuse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uk.ac.tees.mad.reuse.data.local.ReuseIdea
import uk.ac.tees.mad.reuse.data.repository.GeminiRepository
import javax.inject.Inject

data class HomeUiState(
    val query: String = "",
    val ideas: List<ReuseIdea> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GenericVm @Inject constructor(
    private val genericRepo: GeminiRepository
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()
    // endregion

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    /** Called when user types a new query **/
    fun onQueryChanged(newQuery: String) {
        _homeUiState.value = _homeUiState.value.copy(query = newQuery)
    }

    /** Fetch reuse ideas for current query **/
    fun fetchReuseIdeas() {
        val currentQuery = _homeUiState.value.query.trim()
        if (currentQuery.isEmpty()) return

        viewModelScope.launch {
            try {
                _homeUiState.value = _homeUiState.value.copy(isLoading = true, error = null)

                val results = mutableListOf<ReuseIdea>()

                genericRepo.generateText(prompt = "Creative ways to reuse $currentQuery").collect { chunk ->
                    if (chunk != null) {
                        val idea = ReuseIdea(
                            title = "$currentQuery Idea",
                            description = chunk.take(150) + "...",
                            steps = listOf("Step 1: Analyze the object", "Step 2: Reuse it creatively")
                        )
                        results.add(idea)
                        _homeUiState.value = _homeUiState.value.copy(ideas = results)
                    }
                }

                // Cache successful response locally (Room integration later)
                // repository.saveIdeasToCache(currentQuery, results)

            } catch (e: Exception) {
                Log.e("GenericVm", "Error fetching ideas", e)
                _homeUiState.value = _homeUiState.value.copy(error = e.message ?: "Unknown error")
            } finally {
                _homeUiState.value = _homeUiState.value.copy(isLoading = false)
            }
        }
    }

    /** Fetch next idea for same query (Try Another) **/
    fun fetchNextIdea() {
        val query = _homeUiState.value.query
        if (query.isEmpty()) return
        fetchReuseIdeas()
    }

    /** Save idea locally or to Firestore **/
    fun saveIdea(idea: ReuseIdea) {
        viewModelScope.launch {
            try {
                // TODO: Save to Room + Firestore
                Log.d("GenericVm", "Saved idea: ${idea.title}")
            } catch (e: Exception) {
                Log.e("GenericVm", "Failed to save idea", e)
            }
        }
    }

    // Optional Chat Interface for AI Interactions
    fun generateText(prompt: String) {
        viewModelScope.launch {
            _messages.value = _messages.value + Message.User(prompt)
            _messages.value = _messages.value + Message.Assistant("")

            genericRepo.generateText(prompt = prompt).collect { chunk ->
                if (chunk != null) {
                    val updated = _messages.value.toMutableList()
                    val last = updated.last() as Message.Assistant
                    updated[updated.size - 1] = last.copy(text = last.text + chunk)
                    _messages.value = updated
                } else {
                    Log.e("GenericVm", "Error in stream")
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

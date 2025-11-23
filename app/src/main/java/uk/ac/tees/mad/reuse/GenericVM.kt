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

                Log.d("GenericVm", "Fetched ideas: $results")


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
}

sealed class Message {
    data class User(val text: String) : Message()
    data class Assistant(val text: String) : Message()
}

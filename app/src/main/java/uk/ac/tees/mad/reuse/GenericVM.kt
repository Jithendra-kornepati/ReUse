package uk.ac.tees.mad.reuse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import uk.ac.tees.mad.reuse.data.local.ReuseIdea
import uk.ac.tees.mad.reuse.data.repository.GeminiRepository
import uk.ac.tees.mad.reuse.data.repository.SavedIdeasRepository
import javax.inject.Inject

data class HomeUiState(
    val query: String = "",
    val ideas: List<ReuseIdea> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GenericVm @Inject constructor(
    private val genericRepo: GeminiRepository,
    val savedRepo: SavedIdeasRepository,
    val auth: FirebaseAuth
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                loadSavedIdeasOnStartup()
            } catch (e: Exception) {
                Log.w("GenericVm", "Failed to load saved ideas at startup: ${e.message}")
            }
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun onQueryChanged(newQuery: String) {
        _homeUiState.value = _homeUiState.value.copy(query = newQuery)
    }

    fun fetchReuseIdeas() {
        val query = _homeUiState.value.query.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _homeUiState.value = _homeUiState.value.copy(isLoading = true, error = null)

            try {
                val prompt = """
                    You are a sustainability assistant. 
                    Return a JSON array of creative reuse ideas for the item: "$query".
                    Each object must have:
                    {
                      "title": "Short title for the reuse idea",
                      "description": "2–3 sentence description explaining how it helps reuse the item",
                      "steps": ["Step 1...", "Step 2...", "Step 3..."]
                    }
                    Rules:
                    - Respond with ONLY valid JSON.
                    - Do not include markdown, explanations, or any text outside the JSON.
                """.trimIndent()

                var rawResponse = ""

                genericRepo.generateText(prompt).collect { chunk ->
                    if (chunk != null) rawResponse += chunk
                }

                val cleaned = rawResponse
                    .substringAfter("[")
                    .substringBeforeLast("]")
                    .let { "[$it]" }

                val ideas = json.decodeFromString<List<ReuseIdea>>(cleaned)
                _homeUiState.value = _homeUiState.value.copy(ideas = ideas, isLoading = false)
                Log.d("GenericVm", "Parsed ${ideas.size} ideas successfully")

            } catch (e: Exception) {
                Log.e("GenericVm", "JSON parse failed", e)
                _homeUiState.value = _homeUiState.value.copy(
                    error = "Failed to parse response. Try again.",
                    isLoading = false
                )
            }
        }
    }

    fun fetchNextIdea() {
        if (_homeUiState.value.query.isNotBlank()) fetchReuseIdeas()
    }

    fun saveIdea(idea: ReuseIdea) {
        viewModelScope.launch {
            try {
                val current = _homeUiState.value.ideas.toMutableList()
                val saved = savedRepo.saveIdeaForCurrentUser(idea)
                Log.d("GenericVm", "Saved idea to Firestore + Room: ${saved.id}")
            } catch (e: Exception) {
                Log.e("GenericVm", "Save failed: ${e.message}", e)
            }
        }
    }

    private suspend fun loadSavedIdeasOnStartup() {
        val user = auth.currentUser ?: return
        savedRepo.fetchAndCacheSavedIdeasForCurrentUser()
    }

    fun refreshSavedIdeas() {
        viewModelScope.launch {
            try {
                loadSavedIdeasOnStartup()
            } catch (e: Exception) {
                Log.e("GenericVm", "refreshSavedIdeas failed", e)
            }
        }
    }
}

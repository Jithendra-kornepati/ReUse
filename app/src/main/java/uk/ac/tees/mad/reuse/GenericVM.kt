// GenericVm.kt
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

    fun onQueryChanged(newQuery: String) {
        _homeUiState.value = _homeUiState.value.copy(query = newQuery)
    }

    /**
     * Stream from Gemini into a single accumulating ReuseIdea.
     * - Show a single card that is progressively filled.
     * - After streaming completes, parse steps (simple heuristics).
     * - Only then isLoading = false and TryAnother becomes visible.
     */
    fun fetchReuseIdeas() {
        val query = _homeUiState.value.query.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _homeUiState.value = _homeUiState.value.copy(isLoading = true, error = null)

            try {
                // Create an accumulating idea with a stable id (timestamp-based for now)
                val id = System.currentTimeMillis().toString()
                val accumulating = ReuseIdea(
                    id = id,
                    title = "Ideas for \"$query\"",
                    description = "",
                    steps = emptyList()
                )

                // Put the accumulating idea into state so UI shows it immediately and updates progressively
                _homeUiState.value = _homeUiState.value.copy(ideas = listOf(accumulating))

                // Accumulate chunks into a StringBuilder
                val sb = StringBuilder()

                genericRepo.generateText(prompt = "Give several creative reuse ideas for: $query. Provide numbered steps and short descriptions. Use numbered lists.")
                    .collect { chunk ->
                        if (chunk == null) return@collect
                        sb.append(chunk)

                        // update the accumulating idea while streaming so UI sees partial content
                        val current = _homeUiState.value.ideas.toMutableList()
                        if (current.isEmpty()) {
                            current.add(accumulating.copy(description = sb.toString()))
                        } else {
                            val first = current[0]
                            current[0] = first.copy(description = sb.toString())
                        }
                        _homeUiState.value = _homeUiState.value.copy(ideas = current)
                    }

                // When stream ends, parse the final text into structured steps if possible
                val finalText = sb.toString()
                val parsed = parseIdeas(finalText)

                // Replace accumulating idea with parsed ideas (prefer structured list; fallback to single)
                val finalIdeas = if (parsed.isNotEmpty()) parsed else listOf(accumulating.copy(description = finalText))

                // TODO: persist finalIdeas to Room (cache) and update popularity metrics

                _homeUiState.value = _homeUiState.value.copy(ideas = finalIdeas, isLoading = false)
            } catch (e: Exception) {
                Log.e("GenericVm", "fetchReuseIdeas error", e)
                _homeUiState.value = _homeUiState.value.copy(error = e.message ?: "Unknown error", isLoading = false)
            }
        }
    }

    fun fetchNextIdea() {
        // Simple retry / new generation for same query
        if (_homeUiState.value.query.isBlank()) return
        fetchReuseIdeas()
    }

    fun saveIdea(idea: ReuseIdea) {
        viewModelScope.launch {
            try {
                // TODO: persist to Room + Firestore via repository
                Log.d("GenericVm", "saveIdea: ${idea.id}")
            } catch (e: Exception) {
                Log.e("GenericVm", "saveIdea failed", e)
            }
        }
    }

    /**
     * Parse the raw multi-idea text into a list of ReuseIdea objects.
     * Heuristics:
     *  - Split by common separators (double newlines + headings)
     *  - For each block, take the first line as title, remaining as description
     *  - Extract numbered/dash lines as steps
     */
    private fun parseIdeas(text: String): List<ReuseIdea> {
        if (text.isBlank()) return emptyList()

        // Split into blocks by two or more newlines (commonly separate ideas)
        val blocks = text.split(Regex("\\n{2,}")).map { it.trim() }.filter { it.isNotEmpty() }

        val ideas = mutableListOf<ReuseIdea>()
        var counter = 0
        for (b in blocks) {
            counter++
            val lines = b.split(Regex("\\r?\\n")).map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) continue

            // title: first line if short, otherwise generate a title
            val first = lines.first()
            val title = if (first.length <= 60 && (first.contains(":") || first.matches(Regex("^\\d+\\.|^-|^•")) || first.contains("-"))) {
                // it might be a step or numbered header; create a synthetic title
                "Idea $counter"
            } else {
                first
            }

            // description: remaining lines joined until a numbered list starts
            val remaining = if (lines.size > 1) lines.subList(1, lines.size) else emptyList()

            // steps: extract lines that look like steps
            val steps = remaining.mapNotNull { line ->
                val numMatch = Regex("""^\s*\d+\.\s*(.+)""").find(line)
                val dashMatch = Regex("""^\s*[-•]\s*(.+)""").find(line)
                when {
                    numMatch != null -> numMatch.groupValues[1].trim()
                    dashMatch != null -> dashMatch.groupValues[1].trim()
                    line.startsWith("Step", ignoreCase = true) -> line
                    else -> null
                }
            }

            val description = remaining
                .filter { line -> !Regex("""^\s*(\d+\.|[-•]|Step)""").containsMatchIn(line) }
                .joinToString("\n")

            ideas.add(ReuseIdea(id = System.currentTimeMillis().toString() + counter, title = title, description = description, steps = steps))
        }

        return ideas
    }
}

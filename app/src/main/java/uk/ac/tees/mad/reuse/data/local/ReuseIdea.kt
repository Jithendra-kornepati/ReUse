package uk.ac.tees.mad.reuse.data.local
import kotlinx.serialization.Serializable

@Serializable
data class ReuseIdea(
    val id: String ="",
    val title: String,
    val description: String,
    val steps: List<String>
)

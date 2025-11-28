package uk.ac.tees.mad.reuse.data.local
import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "reuse_ideas")
data class ReuseIdea(
    val id: String ="",
    val title: String,
    val description: String,
    val steps: List<String>,
    val ownerUid: String? = null
)

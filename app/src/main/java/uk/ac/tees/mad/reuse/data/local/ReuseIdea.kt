package uk.ac.tees.mad.reuse.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "reuse_ideas")
data class ReuseIdea(
    @PrimaryKey
    val id: String ="",
    val title: String = "",
    val description: String = "",
    val steps: List<String> = emptyList(),
    val ownerUid: String? = null
)

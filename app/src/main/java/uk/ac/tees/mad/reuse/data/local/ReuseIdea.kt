package uk.ac.tees.mad.reuse.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "reuse_ideas")
data class ReuseIdea(
    @PrimaryKey(autoGenerate = true)
    val key : Int = 1,
    val id: String ="",
    val title: String,
    val description: String,
    val steps: List<String>,
    val ownerUid: String? = null
)

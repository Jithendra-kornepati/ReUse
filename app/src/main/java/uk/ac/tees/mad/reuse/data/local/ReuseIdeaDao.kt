package uk.ac.tees.mad.reuse.data.local


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReuseIdeaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(idea: ReuseIdea)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ideas: List<ReuseIdea>)

    @Query("SELECT * FROM reuse_ideas ORDER BY title")
    fun getIdeasByUserFlow(): Flow<List<ReuseIdea>>

    @Query("SELECT * FROM reuse_ideas WHERE ownerUid = :uid ORDER BY title")
    suspend fun getIdeasByUser(uid: String): List<ReuseIdea>

    @Query("DELETE FROM reuse_ideas WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM reuse_ideas")
    suspend fun clearAll()
}

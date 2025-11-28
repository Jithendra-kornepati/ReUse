package uk.ac.tees.mad.reuse.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.reuse.data.local.ReuseIdea
import uk.ac.tees.mad.reuse.data.local.ReuseIdeaDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedIdeasRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val dao: ReuseIdeaDao
) {

    private fun userCollectionRef(uid: String) =
        firestore.collection("users").document(uid).collection("savedIdeas")


    suspend fun saveIdeaForCurrentUser(idea: ReuseIdea): ReuseIdea {
        val user = auth.currentUser ?: throw IllegalStateException("Not authenticated")
        val uid = user.uid
        val col = userCollectionRef(uid)

        val docRef = if (idea.id.isNotBlank()) {
            col.document(idea.id)
        } else {
            col.document()
        }

        val idToUse = docRef.id
        val ideaToSave = idea.copy(id = idToUse, ownerUid = uid)

        docRef.set(ideaToSave).await()

        dao.insert(ideaToSave)

        return ideaToSave
    }


    suspend fun fetchAndCacheSavedIdeasForCurrentUser(): List<ReuseIdea> {
        val user = auth.currentUser ?: throw IllegalStateException("Not authenticated")
        val uid = user.uid
        val col = userCollectionRef(uid)

        val snapshot = col.get().await()
        val ideas = snapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject(ReuseIdea::class.java)?.copy(id = doc.id, ownerUid = uid)
            } catch (e: Exception) {
                Log.w("SavedIdeasRepo", "Failed to parse doc ${doc.id}: ${e.message}")
                null
            }
        }


        dao.insertAll(ideas)

        return ideas
    }


    fun observeLocalSavedIdeas(uid: String) = dao.getIdeasByUserFlow(uid)
}

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

    /**
     * Save to Firestore then local Room.
     * If idea.id is blank, we generate a Firestore doc id and set it on the saved object.
     */
    suspend fun saveIdeaForCurrentUser(idea: ReuseIdea): ReuseIdea {
        val user = auth.currentUser ?: throw IllegalStateException("Not authenticated")
        val uid = user.uid
        val col = userCollectionRef(uid)

        // Ensure id: use idea.id if present, otherwise create new doc id
        val docRef = if (idea.id.isNotBlank()) {
            col.document(idea.id)
        } else {
            col.document() // generates id
        }

        val idToUse = docRef.id
        val ideaToSave = idea.copy(id = idToUse, ownerUid = uid)

        // Write to Firestore
        docRef.set(ideaToSave).await()

        // Write to local DB
        dao.insert(ideaToSave)

        return ideaToSave
    }

    /**
     * Fetch all saved ideas for current user from Firestore and persist into Room.
     * Returns the list that was fetched.
     */
    suspend fun fetchAndCacheSavedIdeasForCurrentUser(): List<ReuseIdea> {
        val user = auth.currentUser ?: throw IllegalStateException("Not authenticated")
        val uid = user.uid
        val col = userCollectionRef(uid)

        val snapshot = col.get().await()
        val ideas = snapshot.documents.mapNotNull { doc ->
            // use Firestore's automatic mapping to ReuseIdea (or manual conversion)
            try {
                doc.toObject(ReuseIdea::class.java)?.copy(id = doc.id, ownerUid = uid)
            } catch (e: Exception) {
                Log.w("SavedIdeasRepo", "Failed to parse doc ${doc.id}: ${e.message}")
                null
            }
        }

        // Replace local cache for this user (optional: merge strategy)
        // For simplicity: insertAll (REPLACE) ensures local matches remote
        dao.insertAll(ideas)

        return ideas
    }

    /**
     * Observe saved ideas from Room for the current user.
     */
    fun observeLocalSavedIdeas(uid: String) = dao.getIdeasByUserFlow(uid)
}

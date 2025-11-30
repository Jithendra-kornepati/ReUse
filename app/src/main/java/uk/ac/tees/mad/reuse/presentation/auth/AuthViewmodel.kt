package uk.ac.tees.mad.reuse.presentation.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewmodel @Inject constructor(
    val auth : FirebaseAuth,
    private val db : FirebaseFirestore
) : ViewModel() {

    val loggedIn = auth.currentUser != null
    val loading = mutableStateOf(false)

    fun registerUser(context : Context, fullName : String, email : String, password : String, onSucess: () -> Unit) {
        loading.value = true
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            db.collection("users").document(it.user!!.uid).set(
                mapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "password" to password
                )
            ).addOnSuccessListener {
                loading.value = false
                onSucess()
                Toast.makeText(context, "User Registered", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                loading.value = false
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
            }.addOnFailureListener {
                loading.value = false
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()

        }
    }

    fun loginUser(context : Context, email : String, password : String, onSucess : () -> Unit) {
        loading.value = true
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            loading.value = false
            onSucess()
            Toast.makeText(context, "User Logged In", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            loading.value = false
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun updateDisplayName(context: Context, newName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("Not authenticated")
            return
        }

        loading.value = true

        // 1) Update Firestore users document
        db.collection("users").document(user.uid)
            .update("fullName", newName)
            .addOnSuccessListener {
                // 2) Update FirebaseAuth displayName (optional)
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()

                user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    loading.value = false
                    if (task.isSuccessful) {
                        onSuccess()
                        Toast.makeText(context, "Name updated", Toast.LENGTH_SHORT).show()
                    } else {
                        onFailure(task.exception?.message ?: "Failed to update profile")
                        Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                loading.value = false
                onFailure(it.message ?: "Failed to update name")
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    fun logout(context: Context, onSuccess: () -> Unit) {
        try {
            auth.signOut()
            onSuccess()
            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteAccount(context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("Not authenticated")
            return
        }

        loading.value = true
        val uid = user.uid

        val userDocRef = db.collection("users").document(uid)

        userDocRef.delete()
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener {
                        loading.value = false
                        onSuccess()
                        Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { ex ->
                        loading.value = false
                        onFailure(ex.message ?: "Failed to delete auth account")
                        Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { ex ->
                loading.value = false
                onFailure(ex.message ?: "Failed to delete user document")
                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            }
    }

}
package uk.ac.tees.mad.reuse.presentation.auth

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class AuthViewmodel @Inject constructor(
    val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    val loggedIn = auth.currentUser != null
    val loading = mutableStateOf(false)
    val uploading = mutableStateOf(false)
    val uploadProgress = mutableStateOf(0)
    val uploadError = mutableStateOf<String?>(null)

    val userName = MutableStateFlow("")

    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", "dn8ycjojw",
            "api_key", "281678982458183",
            "api_secret", "77nO2JN3hkGXB-YgGZuJOqXcA4Q"
        )
    )

    init {
        if (loggedIn) {
            getUserName()
        }
    }

    fun registerUser(
        context: Context,
        fullName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
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
                onSuccess()
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

    fun loginUser(context: Context, email: String, password: String, onSuccess: () -> Unit) {
        loading.value = true
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            loading.value = false
            onSuccess()
            Toast.makeText(context, "User Logged In", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            loading.value = false
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun updateDisplayName(
        context: Context,
        newName: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("Not authenticated")
            return
        }

        loading.value = true
        db.collection("users").document(user.uid)
            .update("fullName", newName)
            .addOnSuccessListener {
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

    fun getUserName(){
        db.collection("users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    userName.value = document.getString("fullName") ?: ""
                }
            }
            .addOnFailureListener { exception ->
                userName.value = ""
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
        db.collection("users").document(uid)
            .delete()
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

    fun bitmapToFile(context: Context, bitmap: android.graphics.Bitmap): File {
        val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
        }
        return file
    }

    fun uploadProfileImage(
        context: Context,
        imageFile: File,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser ?: return onFailure("Not authenticated")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                uploading.value = true
                val result = cloudinary.uploader().upload(
                    imageFile,
                    ObjectUtils.asMap(
                        "folder", "reuse_app/${user.uid}/profile",
                        "overwrite", true,
                        "public_id", "profile_${System.currentTimeMillis()}"
                    )
                )
                val secureUrl = result["secure_url"] as? String ?: throw Exception("No URL returned")

                // Update Firebase
                user.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(secureUrl))
                        .build()
                )
                db.collection("users").document(user.uid)
                    .update("photoUrl", secureUrl)

                uploading.value = false
                launch(Dispatchers.Main) {
                    onSuccess(secureUrl)
                    Toast.makeText(context, "Profile image updated", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                uploading.value = false
                launch(Dispatchers.Main) {
                    uploadError.value = e.message
                    onFailure(e.message ?: "Upload failed")
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

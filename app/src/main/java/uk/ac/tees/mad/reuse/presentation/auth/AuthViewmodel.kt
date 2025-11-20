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
    private val auth : FirebaseAuth,
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

}
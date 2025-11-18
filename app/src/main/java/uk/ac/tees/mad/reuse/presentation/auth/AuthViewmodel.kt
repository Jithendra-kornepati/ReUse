package uk.ac.tees.mad.reuse.presentation.auth

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

    fun register

}
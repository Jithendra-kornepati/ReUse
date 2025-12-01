package uk.ac.tees.mad.reuse.presentation.profile

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import uk.ac.tees.mad.reuse.presentation.auth.AuthViewmodel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authVm: AuthViewmodel = hiltViewModel()
) {
    val context = LocalContext.current
    val user = authVm.auth.currentUser
    val scope = rememberCoroutineScope()
    val uploading by remember { authVm.uploading }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {

    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val file = authVm.bitmapToFile(context, bitmap)
            scope.launch {
                authVm.uploadProfileImage(
                    context,
                    file,
                    onSuccess = { snackbarMessage = "Profile updated" },
                    onFailure = { snackbarMessage = it }
                )
            }
        } else {
            snackbarMessage = "Camera capture failed"
        }
    }

    val permissionGranted = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted->
        if (granted){
            cameraLauncher.launch(null)
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Profile") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { permissionGranted.launch(Manifest.permission.CAMERA) },
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = user?.photoUrl?.toString()
                    if (!photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.size(100.dp).clip(CircleShape)
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        authVm.logout(context) {
                            navController.navigate("auth_screen") {
                                popUpTo("home_screen") { inclusive = true }
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout")
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        authVm.deleteAccount(context, {
                            navController.navigate("auth_screen") {
                                popUpTo("home_screen") { inclusive = true }
                            }
                        }, { snackbarMessage = it })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Account", color = MaterialTheme.colorScheme.onError)
                }
            }

            if (uploading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            snackbarMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                snackbarMessage = null
            }
        }
    }
}



package uk.ac.tees.mad.reuse.presentation.profile

import android.Manifest
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import uk.ac.tees.mad.reuse.presentation.auth.AuthViewmodel
import uk.ac.tees.mad.reuse.ui.theme.Typography
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

    var editingName by remember { mutableStateOf(false) }
    val name by authVm.userName.collectAsState()
    var displayName by remember { mutableStateOf(name) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val uploading by remember { authVm.uploading }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            scope.launch {
                val file = authVm.bitmapToFile(context, bitmap)
                authVm.uploadProfileImage(
                    context,
                    file,
                    onSuccess = { toastMessage = "Profile image updated" },
                    onFailure = { toastMessage = it }
                )
            }
        } else toastMessage = "Camera capture failed"
    }
    LaunchedEffect(name) {
        displayName = name         
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) cameraLauncher.launch(null)
            else toastMessage = "Camera permission required to update photo"
        }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", style = Typography.titleLarge) }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = user?.photoUrl?.toString()
                    if (!photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!editingName) {
                        Text(
                            text = displayName.ifBlank { user?.email ?: "Unnamed User" },
                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { editingName = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Name")
                        }
                    } else {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                if (it.isBlank()) nameError = "Name cannot be empty"
                                else nameError = null
                            },
                            singleLine = true,
                            label = { Text("Full Name") },
                            isError = nameError != null,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = {
                            if (displayName.isNotBlank()) {
                                authVm.updateDisplayName(
                                    context,
                                    displayName,
                                    onSuccess = {
                                        editingName = false
                                        toastMessage = "Name updated"
                                    },
                                    onFailure = { toastMessage = it }
                                )
                            } else {
                                nameError = "Name cannot be empty"
                            }
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Save Name")
                        }
                    }
                }

                nameError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = Typography.bodySmall
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = user?.email ?: "",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 1.dp
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Welcome to your ReUse profile! Here you can manage your details, update your name or profile photo, and access your saved creative ideas.",
                    style = Typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(28.dp))

//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    StatChip(label = "Saved", value = "12")
//                    StatChip(label = "Ideas", value = "48")
//                    StatChip(label = "Reuse Streak", value = "3d")
//                }

                Spacer(Modifier.height(40.dp))

                OutlinedButton(
                    onClick = {
                        authVm.logout(context) {
                            navController.navigate("auth_screen") {
                                popUpTo("home_screen") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Account", color = MaterialTheme.colorScheme.onError)
                }

                Spacer(Modifier.height(50.dp))
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete Account") },
                    text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteConfirm = false
                            authVm.deleteAccount(context, {
                                navController.navigate("auth_screen") {
                                    popUpTo("home_screen") { inclusive = true }
                                }
                            }, { toastMessage = it })
                        }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            AnimatedVisibility(visible = uploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Uploading photo...", style = Typography.bodySmall)
                    }
                }
            }

            toastMessage?.let {
                LaunchedEffect(it) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    toastMessage = null
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, style = Typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text(label, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

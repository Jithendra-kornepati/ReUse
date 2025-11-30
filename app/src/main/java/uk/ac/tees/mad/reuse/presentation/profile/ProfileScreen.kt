package uk.ac.tees.mad.reuse.presentation.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import uk.ac.tees.mad.reuse.presentation.auth.AuthViewmodel
import uk.ac.tees.mad.reuse.ui.theme.Typography
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authVm: AuthViewmodel = hiltViewModel()
) {
    val user = authVm.auth.currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var editingName by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val loading by remember { authVm.loading } // state holder from viewmodel
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Example stats — you can connect to GenericVm / SavedIdeasRepository for real numbers
    var savedCount by remember { mutableStateOf(0) }

    // You may want to fetch saved count from Room / repo; placeholder for now
    LaunchedEffect(Unit) {
        // If you exposed repo or GenericVm, fetch real savedCount here.
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", style = Typography.titleLarge) }
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUrl = user?.photoUrl?.toString()
                            if (!photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profile photo",
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!editingName) {
                                    Text(
                                        text = displayName.ifBlank { user?.email ?: "Unnamed" },
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = displayName,
                                        onValueChange = { displayName = it },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateContentSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { editingName = !editingName }) {
                                    Icon(Icons.Default.Save, contentDescription = "Edit")
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = user?.email ?: "No email",
                                style = Typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // small stats row
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatChip(label = "Saved", value = savedCount.toString())
                                StatChip(label = "Ideas", value = "—")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(visible = editingName) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(onClick = {
                                scope.launch {
                                    authVm.updateDisplayName(context, displayName, {
                                        snackbarMessage = "Name updated"
                                        editingName = false
                                    }, { err ->
                                        snackbarMessage = err
                                    })
                                }
                            }) {
                                Text("Save")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Column {
                        OutlinedButton(
                            onClick = {
                                // Sign out and navigate to auth screen
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

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete account", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }

            // loading indicator overlay
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Delete confirmation dialog
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete account") },
                    text = { Text("Deleting your account will remove your profile. This action is irreversible. Are you sure?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteConfirm = false
                            authVm.deleteAccount(context, {
                                // after deletion, navigate to auth screen and clear backstack
                                navController.navigate("auth_screen") {
                                    popUpTo("home_screen") { inclusive = true }
                                }
                            }, { err ->
                                snackbarMessage = err
                            })
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

            // snackbar
            snackbarMessage?.let { message ->
                LaunchedEffect(message) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    snackbarMessage = null
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .wrapContentHeight()
            .padding(end = 4.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = Typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// HomeScreen.kt
package uk.ac.tees.mad.reuse.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.ac.tees.mad.reuse.GenericVm
import uk.ac.tees.mad.reuse.data.local.ReuseIdea
import uk.ac.tees.mad.reuse.R
import uk.ac.tees.mad.reuse.Routes
import uk.ac.tees.mad.reuse.ui.theme.Typography
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
            genericVm: GenericVm = hiltViewModel(),
) {
    val uiState by genericVm.homeUiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ReUse Ideas", style = Typography.titleLarge, color = MaterialTheme.colorScheme.primary) }
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading && uiState.ideas.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { scope.launch { genericVm.fetchNextIdea() } },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Try Another", tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Try Another", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { genericVm.onQueryChanged(it) },
                    label = { Text("Enter an item (e.g., T-shirt)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { if (uiState.query.isNotBlank()) genericVm.fetchReuseIdeas() }, modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(Icons.Rounded.Send, contentDescription = "Search")
                }
            }

            Spacer(Modifier.height(16.dp))

            when {
                uiState.isLoading && uiState.ideas.isEmpty() -> {
                    // no idea yet — full screen loader
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                uiState.isLoading && uiState.ideas.isNotEmpty() -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                        items(uiState.ideas) { idea ->
                            ReuseIdeaCard(
                                idea = idea,
                                onSave = { genericVm.saveIdea(idea) },
                                onClick = {
                                    val encoded = URLEncoder.encode(Json.encodeToString(idea), StandardCharsets.UTF_8.toString())
                                    navController?.navigate(Routes.ReuseDetail.createRoute(encoded))
                                }
                            )
                        }
                    }
                }

                uiState.error != null -> ErrorState(uiState.error!!)

                uiState.ideas.isEmpty() -> EmptyState()

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                        items(uiState.ideas) { idea ->
                            ReuseIdeaCard(
                                idea = idea,
                                onSave = { genericVm.saveIdea(idea) },
                                onClick = { navController?.navigate("reuseDetail/${idea.id}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReuseIdeaCard(idea: ReuseIdea, onSave: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(idea.title, style = Typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))

            MarkdownText(raw = idea.description)

            Spacer(Modifier.height(12.dp))

            if (idea.steps.isNotEmpty()) {
                idea.steps.forEachIndexed { i, s ->
                    Text("${i + 1}. $s", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 4.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Save", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}


@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.re_use_icon),
                contentDescription = "Empty State",
                modifier = Modifier.size(140.dp).clip(RoundedCornerShape(32.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Start by entering an item above!",
                style = Typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Discover creative ways to reuse and repurpose your everyday items.",
                style = Typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = Typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}



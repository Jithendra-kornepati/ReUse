package uk.ac.tees.mad.reuse.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.reuse.GenericVm
import uk.ac.tees.mad.reuse.data.local.ReuseIdea
import uk.ac.tees.mad.reuse.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReuseDetailScreen(
    idea: ReuseIdea,
    vm: GenericVm = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(idea.title, style = Typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(idea.description, style = Typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            idea.steps.forEachIndexed { index, step ->
                Text("${index + 1}. $step", style = Typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { vm.saveIdea(idea,context) }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }
}

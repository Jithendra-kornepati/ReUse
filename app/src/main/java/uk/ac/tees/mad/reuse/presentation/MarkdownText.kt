package uk.ac.tees.mad.reuse.presentation


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.reuse.ui.theme.Typography

@Composable
fun MarkdownText(raw: String) {
    if (raw.isBlank()) return

    // split into lines and render blocks
    val blocks = raw.split(Regex("\\n{1,}")).map { it.trim() }.filter { it.isNotEmpty() }

    Column {
        blocks.forEach { line ->
            when {
                line.matches(Regex("^\\s*\\d+\\.\\s+.*")) -> {
                    // numbered item — show as a body line
                    Text(text = line.trim(), style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
                line.matches(Regex("^\\s*[-•]\\s+.*")) -> {
                    // bullet
                    Text(text = line.trim(), style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
                else -> {
                    // paragraph / heading heuristics
                    if (line.length <= 60 && line.endsWith(":")) {
                        Text(text = line, style = Typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    } else {
                        Text(text = line, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            // small spacing
            Text("", modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

package uk.ac.tees.mad.reuse.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.reuse.GenericVM
import uk.ac.tees.mad.reuse.Message

@Composable
fun HomeScreen(genericVM: GenericVM = hiltViewModel()) {
    var prompt by remember { mutableStateOf("") }
    val messages by genericVM.messages.collectAsState()
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        // Chat history
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.Bottom
        ) {
            items(messages.size) { index ->
                val message = messages[index]
                MessageBubble(message)
            }
        }

        // Auto-scroll to bottom when new messages/chunks arrive
        LaunchedEffect(messages) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        // Input bar at bottom
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your message...") }
                )
                IconButton(onClick = {
                    if (prompt.isNotBlank()) {
                        genericVM.generateText(prompt)
                        prompt = ""
                    }
                }) {
                    Icon(Icons.Rounded.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (message is Message.User) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = when (message) {
                is Message.User -> message.text
                is Message.Assistant -> message.text.ifBlank { "..." }  // Show placeholder while streaming starts
            },
            modifier = Modifier
                .background(
                    color = if (message is Message.User) MaterialTheme.colorScheme.primary else Color.LightGray,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            color = if (message is Message.User) Color.White else Color.Black
        )
    }
}
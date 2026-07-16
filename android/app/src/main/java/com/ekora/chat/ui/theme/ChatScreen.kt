package com.ekora.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ekora.chat.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(conversationId: String, title: String, onBack: () -> Unit) {
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Polling toutes les 3 secondes
    LaunchedEffect(conversationId) {
        while (true) {
            try {
                val fresh = ApiClient.api.listMessages(conversationId)
                if (fresh.size != messages.size) {
                    messages = fresh
                    if (fresh.isNotEmpty()) listState.animateScrollToItem(fresh.size - 1)
                }
            } catch (_: Exception) {}
            delay(3000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { TextButton(onBack) { Text("←") } },
            )
        },
        bottomBar = {
            Row(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,) {
                OutlinedTextField(
                    input, { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message...") },
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = input.isNotBlank() && !sending,
                    onClick = {
                        val text = input.trim(); input = ""; sending = true
                        scope.launch {
                            try {
                                ApiClient.api.sendMessage(conversationId, SendMessageRequest(text))
                                val fresh = ApiClient.api.listMessages(conversationId)
                                messages = fresh
                                if (fresh.isNotEmpty()) listState.animateScrollToItem(fresh.size - 1)
                            } catch (_: Exception) {} finally { sending = false }
                        }
                    },
                ) { Text(if (sending) "..." else "➤") }
            }
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize().padding(horizontal = 8.dp), state = listState) {
            items(messages) { m ->
                val isMine = m.senderId == Session.userId
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
                ) {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isMine) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(10.dp)
                            .widthIn(max = 280.dp),
                    ) {
                        if (!isMine) Text(m.sender.username, style = MaterialTheme.typography.labelSmall)
                        Text(m.content ?: "")
                    }
                }
            }
        }
    }
}
package com.ekora.chat.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ekora.chat.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(onOpenChat: (id: String, title: String) -> Unit) {
    var conversations by remember { mutableStateOf<List<ConversationDto>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        try { conversations = ApiClient.api.listConversations() } catch (_: Exception) {}
    }

    LaunchedEffect(Unit) { refresh() }

    fun titleOf(c: ConversationDto): String =
        if (c.type == "AI") "🤖 Assistant IA"
        else c.participants.firstOrNull { it.user.id != Session.userId }?.user?.username ?: "Conversation"

    Scaffold(
        topBar = { TopAppBar(title = { Text("EkoraChat — ${Session.username}") }) },
        floatingActionButton = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                ExtendedFloatingActionButton(
                    onClick = {
                        scope.launch {
                            try {
                                val conv = ApiClient.api.createAI()
                                onOpenChat(conv.id, "🤖 Assistant IA")
                            } catch (_: Exception) {}
                        }
                    },
                ) { Text("🤖 IA") }
                Spacer(Modifier.height(8.dp))
                ExtendedFloatingActionButton(onClick = { showAddDialog = true }) { Text("+ Contact") }
            }
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            items(conversations) { c ->
                ListItem(
                    headlineContent = { Text(titleOf(c)) },
                    supportingContent = {
                        Text(
                            c.messages.firstOrNull()?.content ?: "Aucun message",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier.clickable { onOpenChat(c.id, titleOf(c)) },
                )
                HorizontalDivider()
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; error = null },
            title = { Text("Ajouter un contact") },
            text = {
                Column {
                    OutlinedTextField(newContactName, { newContactName = it },
                        label = { Text("Nom d'utilisateur") }, singleLine = true)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button({
                    scope.launch {
                        try {
                            val user = ApiClient.api.searchUser(newContactName.trim())
                            try { ApiClient.api.addContact(AddContactRequest(user.username)) } catch (_: Exception) {}
                            val conv = ApiClient.api.createPrivate(CreatePrivateRequest(user.id))
                            showAddDialog = false; newContactName = ""; error = null
                            refresh()
                            onOpenChat(conv.id, user.username)
                        } catch (e: Exception) {
                            error = "Cet utilisateur n'est pas sur EkoraChat"
                        }
                    }
                }) { Text("Ajouter") }
            },
            dismissButton = { TextButton({ showAddDialog = false }) { Text("Annuler") } },
        )
    }
}
package com.ekora.chat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ekora.chat.data.Session
import com.ekora.chat.ui.*
import com.ekora.chat.ui.theme.EkoraChatTheme
import java.net.URLDecoder
import java.net.URLEncoder

private data class PendingChat(val conversationId: String, val title: String, val otherUserId: String?)

private fun extractPendingChat(intent: Intent?): PendingChat? {
    val conversationId = intent?.getStringExtra("conversationId") ?: return null
    val title = intent.getStringExtra("title") ?: "Conversation"
    val otherUserId = intent.getStringExtra("otherUserId")?.takeIf { it.isNotBlank() }
    return PendingChat(conversationId, title, otherUserId)
}

private fun chatRoute(target: PendingChat): String =
    "chat/${target.conversationId}/${URLEncoder.encode(target.title, "UTF-8")}?otherUserId=${target.otherUserId ?: ""}"

class MainActivity : ComponentActivity() {
    private var pendingChat = mutableStateOf<PendingChat?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingChat.value = extractPendingChat(intent)
        requestNotificationPermission()

        setContent {
            EkoraChatTheme {
                val nav = rememberNavController()
                val pending by pendingChat

                // Notification tapée pendant que l'app tournait déjà : on
                // saute directement dans la conversation concernée.
                LaunchedEffect(pending) {
                    val target = pending ?: return@LaunchedEffect
                    if (Session.token != null) {
                        nav.navigate(chatRoute(target)) { launchSingleTop = true }
                        pendingChat.value = null
                    }
                }

                NavHost(nav, startDestination = "login") {
                    composable("login") {
                        LoginScreen(onLoggedIn = {
                            val target = pendingChat.value
                            if (target != null) {
                                nav.navigate(chatRoute(target)) { popUpTo("login") { inclusive = true } }
                                pendingChat.value = null
                            } else {
                                nav.navigate("conversations") { popUpTo("login") { inclusive = true } }
                            }
                        })
                    }
                    composable("conversations") {
                        ConversationsScreen(
                            onOpenChat = { id, title, otherUserId ->
                                nav.navigate("chat/$id/${URLEncoder.encode(title, "UTF-8")}?otherUserId=${otherUserId ?: ""}")
                            },
                            onOpenProfile = { nav.navigate("profile") },
                        )
                    }
                    composable(
                        "chat/{id}/{title}?otherUserId={otherUserId}",
                        arguments = listOf(navArgument("otherUserId") { type = NavType.StringType; defaultValue = "" }),
                    ) { entry ->
                        val id = entry.arguments?.getString("id") ?: return@composable
                        val title = URLDecoder.decode(entry.arguments?.getString("title") ?: "", "UTF-8")
                        val otherUserId = entry.arguments?.getString("otherUserId")?.takeIf { it.isNotBlank() }
                        ChatScreen(id, title, otherUserId, onBack = { nav.popBackStack() })
                    }
                    composable("profile") {
                        ProfileScreen(onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractPendingChat(intent)?.let { pendingChat.value = it }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

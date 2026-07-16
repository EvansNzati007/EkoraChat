package com.ekora.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.ekora.chat.ui.*
import com.ekora.chat.ui.theme.EkoraChatTheme
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EkoraChatTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "login") {
                    composable("login") {
                        LoginScreen(onLoggedIn = {
                            nav.navigate("conversations") { popUpTo("login") { inclusive = true } }
                        })
                    }
                    composable("conversations") {
                        ConversationsScreen(onOpenChat = { id, title ->
                            nav.navigate("chat/$id/${URLEncoder.encode(title, "UTF-8")}")
                        })
                    }
                    composable("chat/{id}/{title}") { entry ->
                        val id = entry.arguments?.getString("id") ?: return@composable
                        val title = URLDecoder.decode(entry.arguments?.getString("title") ?: "", "UTF-8")
                        ChatScreen(id, title, onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}
package com.ekora.chat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ekora.chat.data.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("EkoraChat", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(email, { email = it }, label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (isRegister) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(username, { username = it }, label = { Text("Nom d'utilisateur") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            visualTransformation = PasswordVisualTransformation())

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                loading = true; error = null
                scope.launch {
                    try {
                        val res = if (isRegister)
                            ApiClient.api.register(RegisterRequest(email.trim(), username.trim(), password))
                        else
                            ApiClient.api.login(LoginRequest(email.trim(), password))
                        Session.token = res.accessToken
                        Session.userId = res.userId
                        Session.username = res.username
                        onLoggedIn()
                    } catch (e: Exception) {
                        error = "Échec : vérifie tes identifiants"
                    } finally { loading = false }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "..." else if (isRegister) "S'inscrire" else "Se connecter") }

        TextButton({ isRegister = !isRegister }) {
            Text(if (isRegister) "Déjà un compte ? Se connecter" else "Pas de compte ? S'inscrire")
        }
    }
}
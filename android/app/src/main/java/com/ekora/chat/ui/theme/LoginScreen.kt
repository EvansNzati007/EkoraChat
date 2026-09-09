package com.ekora.chat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ekora.chat.data.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

private fun registerFcmToken() {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        val token = task.result ?: return@addOnCompleteListener
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.api.updateFcmToken(UpdateFcmTokenRequest(token))
            } catch (_: Exception) {
            }
        }
    }
}

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val trimmedEmail = email.trim()
    val trimmedUsername = username.trim()
    val emailValid = InputRules.isValidEmail(trimmedEmail)
    val usernameValid = !isRegister || InputRules.isValidUsername(trimmedUsername)
    val passwordValid = InputRules.isValidPassword(password)
    val formValid = emailValid && usernameValid && passwordValid

    fun switchMode() {
        isRegister = !isRegister
        error = null
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("EkoraChat", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            email, { email = it.take(InputRules.MAX_EMAIL_LENGTH) },
            label = { Text("Email") },
            singleLine = true,
            isError = email.isNotEmpty() && !emailValid,
            supportingText = {
                if (email.isNotEmpty() && !emailValid) Text("Adresse email invalide")
            },
            modifier = Modifier.fillMaxWidth(),
        )
        if (isRegister) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                username, { username = it.take(InputRules.MAX_USERNAME_LENGTH) },
                label = { Text("Nom d'utilisateur") },
                singleLine = true,
                isError = username.isNotEmpty() && !usernameValid,
                supportingText = {
                    if (username.isNotEmpty() && !usernameValid) {
                        Text("${InputRules.MIN_USERNAME_LENGTH} à ${InputRules.MAX_USERNAME_LENGTH} caractères : lettres, chiffres, . _ -")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(8.dp))
        PasswordField(
            password, { password = it.take(InputRules.MAX_PASSWORD_LENGTH) },
            label = "Mot de passe",
            isError = password.isNotEmpty() && !passwordValid,
            supportingText = {
                if (password.isNotEmpty() && !passwordValid) Text("${InputRules.MIN_PASSWORD_LENGTH} caractères minimum")
            },
            modifier = Modifier.fillMaxWidth(),
        )

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
                            ApiClient.api.register(RegisterRequest(trimmedEmail, trimmedUsername, password))
                        else
                            ApiClient.api.login(LoginRequest(trimmedEmail, password))
                        Session.token = res.accessToken
                        Session.userId = res.userId
                        Session.username = res.username
                        registerFcmToken()
                        onLoggedIn()
                    } catch (e: HttpException) {
                        error = when (e.code()) {
                            409 -> "Cet email ou ce nom d'utilisateur est déjà utilisé"
                            401 -> "Identifiants incorrects"
                            400 -> "Champs invalides"
                            else -> "Erreur serveur, réessaie plus tard"
                        }
                    } catch (e: Exception) {
                        error = "Impossible de contacter le serveur"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = formValid && !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "..." else if (isRegister) "S'inscrire" else "Se connecter") }

        TextButton(::switchMode) {
            Text(if (isRegister) "Déjà un compte ? Se connecter" else "Pas de compte ? S'inscrire")
        }
    }
}

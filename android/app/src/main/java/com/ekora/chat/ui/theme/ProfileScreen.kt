package com.ekora.chat.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ekora.chat.data.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var me by remember { mutableStateOf<UserDto?>(null) }
    var username by remember { mutableStateOf("") }
    var savingProfile by remember { mutableStateOf(false) }
    var profileMessage by remember { mutableStateOf<String?>(null) }
    var profileError by remember { mutableStateOf(false) }
    var uploadingAvatar by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var changingPassword by remember { mutableStateOf(false) }
    var passwordMessage by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf(false) }

    val trimmedUsername = username.trim()
    val usernameValid = InputRules.isValidUsername(trimmedUsername)
    val newPasswordValid = InputRules.isValidPassword(newPassword)
    val passwordsMatch = newPassword == confirmPassword
    val passwordFormValid = currentPassword.isNotEmpty() && newPasswordValid && passwordsMatch

    LaunchedEffect(Unit) {
        try {
            me = ApiClient.api.me()
            username = me?.username ?: ""
        } catch (_: Exception) {}
    }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploadingAvatar = true
            try {
                val mimeType = context.contentResolver.getType(uri) ?: "image/*"
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", "avatar.jpg", body)
                    me = ApiClient.api.uploadAvatar(part)
                }
            } catch (_: Exception) {
            } finally {
                uploadingAvatar = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Spacing.md),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Box {
                    Avatar(url = me?.avatar?.let { SocketManager.MEDIA_BASE_URL + it }, initials = username, size = 96.dp)
                    IconButton(
                        onClick = { avatarPicker.launch("image/*") },
                        enabled = !uploadingAvatar,
                        modifier = Modifier.align(Alignment.BottomEnd),
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Changer la photo")
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Text("Profil", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = me?.email ?: "",
                onValueChange = {},
                label = { Text("Email") },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.take(InputRules.MAX_USERNAME_LENGTH) },
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
            profileMessage?.let {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    it,
                    color = if (profileError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Button(
                onClick = {
                    scope.launch {
                        savingProfile = true; profileMessage = null
                        try {
                            me = ApiClient.api.updateProfile(UpdateProfileRequest(trimmedUsername))
                            Session.username = me?.username
                            profileError = false
                            profileMessage = "Profil mis à jour"
                        } catch (e: HttpException) {
                            profileError = true
                            profileMessage = if (e.code() == 409) "Ce nom d'utilisateur est déjà pris" else "Champ invalide"
                        } catch (e: Exception) {
                            profileError = true
                            profileMessage = "Impossible de contacter le serveur"
                        } finally {
                            savingProfile = false
                        }
                    }
                },
                enabled = usernameValid && !savingProfile,
            ) { Text(if (savingProfile) "..." else "Enregistrer") }

            Spacer(Modifier.height(Spacing.lg))
            HorizontalDivider()
            Spacer(Modifier.height(Spacing.lg))

            Text("Sécurité", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.sm))
            PasswordField(
                value = currentPassword,
                onValueChange = { currentPassword = it.take(InputRules.MAX_PASSWORD_LENGTH) },
                label = "Mot de passe actuel",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.sm))
            PasswordField(
                value = newPassword,
                onValueChange = { newPassword = it.take(InputRules.MAX_PASSWORD_LENGTH) },
                label = "Nouveau mot de passe",
                isError = newPassword.isNotEmpty() && !newPasswordValid,
                supportingText = {
                    if (newPassword.isNotEmpty() && !newPasswordValid) {
                        Text("${InputRules.MIN_PASSWORD_LENGTH} caractères minimum")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.sm))
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it.take(InputRules.MAX_PASSWORD_LENGTH) },
                label = "Confirmer le nouveau mot de passe",
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                supportingText = {
                    if (confirmPassword.isNotEmpty() && !passwordsMatch) Text("Les mots de passe ne correspondent pas")
                },
                modifier = Modifier.fillMaxWidth(),
            )
            passwordMessage?.let {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    it,
                    color = if (passwordError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Button(
                onClick = {
                    scope.launch {
                        changingPassword = true; passwordMessage = null
                        try {
                            ApiClient.api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
                            passwordError = false
                            passwordMessage = "Mot de passe modifié"
                            currentPassword = ""; newPassword = ""; confirmPassword = ""
                        } catch (e: HttpException) {
                            passwordError = true
                            passwordMessage = if (e.code() == 401) "Mot de passe actuel incorrect" else "Champ invalide"
                        } catch (e: Exception) {
                            passwordError = true
                            passwordMessage = "Impossible de contacter le serveur"
                        } finally {
                            changingPassword = false
                        }
                    }
                },
                enabled = passwordFormValid && !changingPassword,
            ) { Text(if (changingPassword) "..." else "Changer le mot de passe") }
        }
    }
}

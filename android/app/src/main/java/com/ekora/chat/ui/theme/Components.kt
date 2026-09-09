package com.ekora.chat.ui

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
}

/**
 * Règles de validation partagées entre les écrans login/register et profil,
 * alignées sur les DTOs backend (RegisterDto, UpdateProfileDto, ChangePasswordDto)
 * pour donner un retour immédiat côté client — le serveur reste la source de
 * vérité en cas de contournement du client.
 */
object InputRules {
    const val MAX_EMAIL_LENGTH = 255
    const val MIN_USERNAME_LENGTH = 3
    const val MAX_USERNAME_LENGTH = 30
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 72
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_.-]+$")

    fun isValidEmail(email: String) =
        email.isNotEmpty() && email.length <= MAX_EMAIL_LENGTH && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidUsername(username: String) =
        username.length in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH && USERNAME_REGEX.matches(username)

    fun isValidPassword(password: String) = password.length >= MIN_PASSWORD_LENGTH
}

/**
 * Champ mot de passe avec bouton pour révéler/masquer la saisie.
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Masquer le mot de passe" else "Afficher le mot de passe",
                )
            }
        },
        modifier = modifier,
    )
}

private val OnlineDotColor = Color(0xFF31A24C)

/**
 * Avatar circulaire : photo si disponible, sinon initiales sur fond teinté.
 * `isOnline = null` masque le point de présence (ex : bot IA).
 */
@Composable
fun Avatar(url: String?, initials: String, isOnline: Boolean? = null, size: Dp = 40.dp) {
    Box {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.size(size).clip(CircleShape),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initials.take(2).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        if (isOnline == true) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-1).dp, y = (-1).dp)
                    .clip(CircleShape)
                    .background(OnlineDotColor)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
            )
        }
    }
}

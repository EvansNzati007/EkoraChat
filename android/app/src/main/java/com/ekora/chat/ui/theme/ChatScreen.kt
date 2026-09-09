package com.ekora.chat.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.ekora.chat.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

private fun mediaUrlOf(path: String?): String? =
    path?.let { SocketManager.MEDIA_BASE_URL + it }

private fun queryFileName(context: android.content.Context, uri: Uri): String {
    var name = "fichier"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && cursor.moveToFirst()) name = cursor.getString(idx) ?: name
    }
    return name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(conversationId: String, title: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    fun mergeIncoming(incoming: MessageDto) {
        messages = if (messages.any { it.id == incoming.id }) messages else messages + incoming
    }

    // WebSocket : réception instantanée des nouveaux messages (le serveur ne
    // diffuse qu'aux membres de la room "conversation:<id>" rejointe ci-dessous)
    DisposableEffect(conversationId) {
        SocketManager.connect { incoming -> mergeIncoming(incoming) }
        SocketManager.joinConversation(conversationId)
        onDispose { }
    }

    // Polling toutes les 3 secondes (filet de sécurité si le socket est indisponible)
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

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    suspend fun uploadFilePart(part: MultipartBody.Part, type: String, caption: String? = null) {
        sending = true
        try {
            val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
            val contentBody = caption?.toRequestBody("text/plain".toMediaTypeOrNull())
            val res = ApiClient.api.sendMedia(conversationId, part, typeBody, contentBody)
            mergeIncoming(res.message)
        } catch (_: Exception) {
        } finally {
            sending = false
        }
    }

    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val name = queryFileName(context, uri)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
            val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", name, body)
            val type = if (mimeType.startsWith("image/")) "IMAGE" else "FILE"
            uploadFilePart(part, type, if (type == "FILE") name else null)
        }
    }

    fun startRecording() {
        val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
        val rec = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        try {
            rec.setAudioSource(MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            rec.setOutputFile(file.absolutePath)
            rec.prepare()
            rec.start()
            recorder = rec
            audioFile = file
            isRecording = true
        } catch (_: Exception) {
            isRecording = false
        }
    }

    fun stopRecordingAndSend() {
        val rec = recorder ?: return
        val file = audioFile
        try {
            rec.stop()
        } catch (_: Exception) {}
        rec.release()
        recorder = null
        isRecording = false
        if (file != null && file.exists()) {
            scope.launch {
                val body = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, body)
                uploadFilePart(part, "AUDIO")
            }
        }
    }

    val recordPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecording()
    }

    fun onMicClick() {
        if (isRecording) {
            stopRecordingAndSend()
        } else {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (granted) startRecording() else recordPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
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
                IconButton(onClick = { pickLauncher.launch("*/*") }, enabled = !sending && !isRecording) {
                    Icon(Icons.Filled.AttachFile, contentDescription = "Joindre un fichier")
                }
                IconButton(onClick = { onMicClick() }, enabled = !sending) {
                    Icon(
                        if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (isRecording) "Arrêter l'enregistrement" else "Message vocal",
                        tint = if (isRecording) MaterialTheme.colorScheme.error else LocalContentColor.current,
                    )
                }
                OutlinedTextField(
                    input, { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message...") },
                    enabled = !isRecording,
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = input.isNotBlank() && !sending && !isRecording,
                    onClick = {
                        val text = input.trim(); input = ""; sending = true
                        scope.launch {
                            try {
                                val res = ApiClient.api.sendMessage(conversationId, SendMessageRequest(text))
                                mergeIncoming(res.message)
                                res.aiMessage?.let { mergeIncoming(it) }
                            } catch (_: Exception) {} finally { sending = false }
                        }
                    },
                ) { Text(if (sending) "..." else "➤") }
            }
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize().padding(horizontal = 8.dp), state = listState) {
            items(messages, key = { it.id }) { m ->
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
                        MessageBody(m)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBody(m: MessageDto) {
    val context = LocalContext.current
    when (m.type) {
        "IMAGE" -> AsyncImage(
            model = mediaUrlOf(m.mediaUrl),
            contentDescription = "Image",
            modifier = Modifier.widthIn(max = 240.dp),
        )
        "AUDIO" -> {
            var player by remember { mutableStateOf<MediaPlayer?>(null) }
            var playing by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val url = mediaUrlOf(m.mediaUrl) ?: return@IconButton
                    if (playing) {
                        player?.stop(); player?.release(); player = null; playing = false
                    } else {
                        val mp = MediaPlayer()
                        mp.setDataSource(url)
                        mp.setOnCompletionListener { playing = false; mp.release(); player = null }
                        mp.prepareAsync()
                        mp.setOnPreparedListener { it.start() }
                        player = mp
                        playing = true
                    }
                }) {
                    Icon(if (playing) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = "Lire l'audio")
                }
                Text("Message vocal")
            }
        }
        "FILE" -> Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            TextButton(onClick = {
                val url = mediaUrlOf(m.mediaUrl) ?: return@TextButton
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }) { Text(m.content ?: "Fichier") }
        }
        else -> Text(m.content ?: "")
    }
}

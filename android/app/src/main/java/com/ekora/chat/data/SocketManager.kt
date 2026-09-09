package com.ekora.chat.data

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {
    val MEDIA_BASE_URL = ApiClient.BASE_URL.trimEnd('/')
    private val BASE_URL = ApiClient.BASE_URL
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var socket: Socket? = null

    // Callbacks mutables : la connexion socket est partagée entre écrans, mais
    // l'écran actif change (ex. navigation entre deux conversations), donc on
    // met à jour la cible plutôt que de ré-enregistrer les listeners socket.io.
    private var onNewMessage: (MessageDto) -> Unit = {}
    private var onMessagesRead: (MessagesReadEvent) -> Unit = {}

    fun connect(
        onNewMessage: (MessageDto) -> Unit,
        onMessagesRead: (MessagesReadEvent) -> Unit = {},
    ) {
        this.onNewMessage = onNewMessage
        this.onMessagesRead = onMessagesRead
        if (socket?.connected() == true) return
        val token = Session.token ?: return

        val options = IO.Options().apply {
            auth = mapOf("token" to token)
            reconnection = true
        }
        val s = IO.socket(BASE_URL, options)
        s.on(Socket.EVENT_CONNECT) { Log.d("SocketManager", "connected") }
        s.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.w("SocketManager", "connect_error: ${args.firstOrNull()}")
        }
        s.on("newMessage") { args ->
            try {
                val json = args.firstOrNull() as? JSONObject ?: return@on
                val message = gson.fromJson(json.toString(), MessageDto::class.java)
                mainHandler.post { this.onNewMessage(message) }
            } catch (e: Exception) {
                Log.w("SocketManager", "bad newMessage payload", e)
            }
        }
        s.on("presence") { args ->
            try {
                val json = args.firstOrNull() as? JSONObject ?: return@on
                val event = gson.fromJson(json.toString(), PresenceEvent::class.java)
                mainHandler.post { PresenceStore.update(event) }
            } catch (e: Exception) {
                Log.w("SocketManager", "bad presence payload", e)
            }
        }
        s.on("messagesRead") { args ->
            try {
                val json = args.firstOrNull() as? JSONObject ?: return@on
                val event = gson.fromJson(json.toString(), MessagesReadEvent::class.java)
                mainHandler.post { this.onMessagesRead(event) }
            } catch (e: Exception) {
                Log.w("SocketManager", "bad messagesRead payload", e)
            }
        }
        s.connect()
        socket = s
    }

    fun joinConversation(conversationId: String) {
        socket?.emit("joinConversation", conversationId)
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }
}

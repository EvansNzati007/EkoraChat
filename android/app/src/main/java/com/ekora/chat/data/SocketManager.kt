package com.ekora.chat.data

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {
    const val MEDIA_BASE_URL = "https://ekorachat-production.up.railway.app"
    private const val BASE_URL = "$MEDIA_BASE_URL/"
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var socket: Socket? = null

    fun connect(onNewMessage: (MessageDto) -> Unit) {
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
                mainHandler.post { onNewMessage(message) }
            } catch (e: Exception) {
                Log.w("SocketManager", "bad newMessage payload", e)
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

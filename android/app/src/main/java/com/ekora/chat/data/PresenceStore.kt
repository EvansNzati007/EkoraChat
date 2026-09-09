package com.ekora.chat.data

import androidx.compose.runtime.mutableStateMapOf

object PresenceStore {
    private val states = mutableStateMapOf<String, PresenceEvent>()

    fun seed(user: UserDto) {
        // Ne pas écraser un état plus récent reçu en direct par le socket
        // avec un état potentiellement périmé venu d'un fetch REST.
        if (states.containsKey(user.id)) return
        states[user.id] = PresenceEvent(user.id, user.isOnline, user.lastSeenAt)
    }

    fun update(event: PresenceEvent) {
        states[event.userId] = event
    }

    fun get(userId: String): PresenceEvent? = states[userId]
}

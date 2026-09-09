package com.ekora.chat.data

data class RegisterRequest(val email: String, val username: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val accessToken: String, val userId: String, val username: String)

data class UserDto(
    val id: String,
    val username: String,
    val email: String? = null,
    val avatar: String? = null,
    val isBot: Boolean = false,
    val isOnline: Boolean = false,
    val lastSeenAt: String? = null,
)

data class ContactDto(val id: String, val nickname: String?, val contact: UserDto)
data class AddContactRequest(val username: String, val nickname: String? = null)

data class ParticipantDto(val user: UserDto)
data class ConversationDto(
    val id: String,
    val type: String,
    val participants: List<ParticipantDto> = emptyList(),
    val messages: List<MessageDto> = emptyList(),
)

data class MessageDto(
    val id: String,
    val content: String?,
    val type: String,
    val mediaUrl: String? = null,
    val status: String = "SENT",
    val createdAt: String,
    val senderId: String,
    val sender: UserDto,
)

data class SendMessageRequest(val content: String)
data class SendMessageResponse(val message: MessageDto, val aiMessage: MessageDto? = null)
data class SendMediaResponse(val message: MessageDto)
data class CreatePrivateRequest(val participantId: String)

data class UpdateProfileRequest(val username: String)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
data class PresenceEvent(val userId: String, val isOnline: Boolean, val lastSeenAt: String?)
data class MessagesReadEvent(val conversationId: String, val readerId: String)
data class UpdateFcmTokenRequest(val token: String)
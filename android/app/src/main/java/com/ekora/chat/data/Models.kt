package com.ekora.chat.data

data class RegisterRequest(val email: String, val username: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val accessToken: String, val userId: String, val username: String)

data class UserDto(
    val id: String,
    val username: String,
    val avatar: String? = null,
    val isBot: Boolean = false,
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
    val createdAt: String,
    val senderId: String,
    val sender: UserDto,
)

data class SendMessageRequest(val content: String)
data class SendMessageResponse(val message: MessageDto, val aiMessage: MessageDto? = null)
data class CreatePrivateRequest(val participantId: String)
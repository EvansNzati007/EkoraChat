package com.ekora.chat.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("users/search")
    suspend fun searchUser(@Query("username") username: String): UserDto

    @POST("contacts")
    suspend fun addContact(@Body body: AddContactRequest): ContactDto

    @GET("contacts")
    suspend fun listContacts(): List<ContactDto>

    @POST("conversations")
    suspend fun createPrivate(@Body body: CreatePrivateRequest): ConversationDto

    @POST("conversations/ai")
    suspend fun createAI(): ConversationDto

    @GET("conversations")
    suspend fun listConversations(): List<ConversationDto>

    @GET("conversations/{id}/messages")
    suspend fun listMessages(@Path("id") conversationId: String): List<MessageDto>

    @POST("conversations/{id}/messages")
    suspend fun sendMessage(
        @Path("id") conversationId: String,
        @Body body: SendMessageRequest,
    ): SendMessageResponse

    @Multipart
    @POST("conversations/{id}/messages/media")
    suspend fun sendMedia(
        @Path("id") conversationId: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody,
        @Part("content") content: RequestBody?,
    ): SendMediaResponse
}
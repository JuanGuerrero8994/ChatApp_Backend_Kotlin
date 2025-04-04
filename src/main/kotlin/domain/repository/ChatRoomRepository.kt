package com.ktor.domain.repository

import com.ktor.domain.model.Message
import kotlinx.coroutines.flow.Flow
import com.ktor.core.Resource
import com.ktor.data.model.chat.ChatRoomRequestDto
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.domain.model.ChatRoom


interface ChatRoomRepository {
    suspend fun createChatRoom(chatRoom: ChatRoomRequestDto): Flow<Resource<ChatRoom>>
    fun getChatRoomById(id: String): Flow<Resource<ChatRoom?>>
    fun getAllChatRooms(): Flow<Resource<List<ChatRoom>>>
    suspend fun addUserToChatRoom(chatRoomId: String, userId: String): Flow<Resource<Boolean>>
    suspend fun removeChatRoom(id: String): Flow<Resource<Boolean>>
}
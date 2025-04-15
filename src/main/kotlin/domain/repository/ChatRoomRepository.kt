package com.ktor.domain.repository

import com.ktor.core.Resource
import com.ktor.domain.model.ChatRoom
import kotlinx.coroutines.flow.Flow

interface ChatRoomRepository {
    suspend fun createChatRoom(chatRoom: ChatRoom): Flow<Resource<ChatRoom>>
    suspend fun getChatRoomById(id: String): Flow<Resource<ChatRoom?>>
    suspend fun getAllChatRooms(): Flow<Resource<List<ChatRoom>>>
    suspend fun addUserToChatRoom(chatRoomId: String, userId: String): Flow<Resource<Boolean>>
    suspend fun removeUserFromChatRoom(chatRoomId: String, userId: String): Flow<Resource<Boolean>>
    suspend fun removeChatRoom(id: String): Flow<Resource<Boolean>>
}

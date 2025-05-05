package com.ktor.domain.repository

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.domain.model.ChatRoom
import kotlinx.coroutines.flow.Flow

interface ChatRoomRepository {
    suspend fun createChatRoom(chatRoom: ChatRoom): Flow<ApiResponse<ChatRoom>>
    suspend fun getChatRoomById(id: String): Flow<ApiResponse<ChatRoom>>
    suspend fun getAllChatRooms(): Flow<ApiResponse<List<ChatRoom>>>
    suspend fun addUserToChatRoom(chatRoomId: String, userId: String): Flow<ApiResponse<Boolean>>
    suspend fun removeUserFromChatRoom(chatRoomId: String, userId: String): Flow<ApiResponse<Boolean>>
    suspend fun removeChatRoom(id: String): Flow<ApiResponse<Boolean>>
}

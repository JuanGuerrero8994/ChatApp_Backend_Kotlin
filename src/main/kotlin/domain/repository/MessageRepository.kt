package com.ktor.domain.repository

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun sendMessage(message: Message):Flow<ApiResponse<String>>
    suspend fun getAllMessages(): Flow<ApiResponse<List<Message>>>
    suspend fun getMessagesByChatRoomId(chatRoomId:String):Flow<ApiResponse<List<Message>>>
}
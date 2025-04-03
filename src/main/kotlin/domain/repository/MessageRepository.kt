package com.ktor.domain.repository

import com.ktor.core.Resource
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun insertMessage(message: MessageRequestDto,senderUserName:String):Flow<Resource<String>>
    fun getAllMessages(): Flow<Resource<List<Message>>>
}
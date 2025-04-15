package com.ktor.domain.usecases.message

import com.ktor.core.Resource
import com.ktor.domain.model.Message
import com.ktor.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow

class GetMessagesByChatRoomIdUseCase(private val repository: MessageRepository) {
    suspend operator fun invoke(chatRoomId:String): Flow<Resource<List<Message>>> = repository.getMessagesByChatRoomId(chatRoomId)
}
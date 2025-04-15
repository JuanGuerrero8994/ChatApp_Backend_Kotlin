package com.ktor.domain.usecases.chat

import com.ktor.core.Resource
import com.ktor.domain.model.ChatRoom
import com.ktor.domain.repository.ChatRoomRepository
import kotlinx.coroutines.flow.Flow

class GetAllChatRoomUseCase(private val repository: ChatRoomRepository) {
    suspend operator fun invoke(): Flow<Resource<List<ChatRoom>>> = repository.getAllChatRooms()
}
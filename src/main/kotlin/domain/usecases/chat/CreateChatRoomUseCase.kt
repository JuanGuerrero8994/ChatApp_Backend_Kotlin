package com.ktor.domain.usecases.chat

import com.ktor.core.Resource
import com.ktor.data.model.chat.ChatRoomRequestDto
import com.ktor.domain.model.ChatRoom
import com.ktor.domain.repository.ChatRoomRepository
import kotlinx.coroutines.flow.Flow

class CreateChatRoomUseCase(private val repository: ChatRoomRepository) {
    suspend operator fun invoke(chatRoom: ChatRoomRequestDto): Flow<Resource<ChatRoom>> = repository.createChatRoom(chatRoom)
}
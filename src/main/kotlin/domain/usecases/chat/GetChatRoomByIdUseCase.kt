package com.ktor.domain.usecases.chat


import com.ktor.core.Resource
import com.ktor.domain.model.ChatRoom
import com.ktor.domain.repository.ChatRoomRepository
import kotlinx.coroutines.flow.Flow

class GetChatRoomByIdUseCase(private val repository: ChatRoomRepository) {
     operator fun invoke(chatRoomId: String): Flow<Resource<ChatRoom?>> = repository.getChatRoomById(chatRoomId)

}

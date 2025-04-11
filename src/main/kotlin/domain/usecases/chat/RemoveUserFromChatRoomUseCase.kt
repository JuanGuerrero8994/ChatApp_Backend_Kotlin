package com.ktor.domain.usecases.chat

import com.ktor.core.Resource
import com.ktor.domain.repository.ChatRoomRepository
import kotlinx.coroutines.flow.Flow

class RemoveUserFromChatRoomUseCase(private val repository: ChatRoomRepository) {
    suspend operator fun invoke(chatRoomId:String,userId:String): Flow<Resource<Boolean>> = repository.removeUserFromChatRoom(chatRoomId,userId)
}
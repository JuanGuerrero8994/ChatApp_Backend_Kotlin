package com.ktor.domain.usecases.message

import com.ktor.core.Resource
import com.ktor.domain.repository.ChatRoomRepository
import kotlinx.coroutines.flow.Flow

class RemoveChatRoomUseCase(private val repository: ChatRoomRepository) {
    suspend operator fun invoke(chatRoomId:String): Flow<Resource<Boolean>> = repository.removeChatRoom(chatRoomId)
}
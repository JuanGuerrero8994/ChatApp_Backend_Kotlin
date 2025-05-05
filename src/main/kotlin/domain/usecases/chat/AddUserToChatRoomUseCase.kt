package com.ktor.domain.usecases.chat

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.domain.repository.ChatRoomRepository
import kotlinx.coroutines.flow.Flow

class AddUserToChatRoomUseCase(private val repository: ChatRoomRepository) {
    suspend operator fun invoke(chatRoomId:String,userId:String): Flow<ApiResponse<Boolean>> = repository.addUserToChatRoom(chatRoomId,userId)
}
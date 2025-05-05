package com.ktor.domain.usecases.message

import com.ktor.core.ApiResponse
import com.ktor.domain.model.Message
import com.ktor.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow

class MessageUseCases(private val repository: MessageRepository) {
    suspend operator fun invoke(message: Message? = null,roomId:String?=null, action: MessageAction): Flow<ApiResponse<out Any>> =
        when (action) {
            MessageAction.SEND_MESSAGE -> {
                repository.sendMessage(message = message!!)
            }

            MessageAction.GET_ALL_MESSAGES -> {
                repository.getAllMessages()
            }

            MessageAction.GET_MESSAGES_BY_CHAT_ROOM_ID -> {
                repository.getMessagesByChatRoomId(roomId!!)
            }
        }
}
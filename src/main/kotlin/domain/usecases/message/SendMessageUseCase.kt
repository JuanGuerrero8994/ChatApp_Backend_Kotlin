package com.ktor.domain.usecases.message

import com.ktor.core.Resource
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow


class SendMessageUseCase(private val repository: MessageRepository) {
    suspend operator fun invoke(request: MessageRequestDto,senderUserName:String):Flow<Resource<String>> = repository.insertMessage(request,senderUserName)
}
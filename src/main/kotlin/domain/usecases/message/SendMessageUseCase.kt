package com.ktor.domain.usecases.message

import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.repository.MessageRepository


class SendMessageUseCase(private val repository: MessageRepository) {
    suspend operator fun invoke(request: MessageRequestDto) {
        repository.insertMessage(request)
    }
}
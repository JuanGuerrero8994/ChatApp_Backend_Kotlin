package com.ktor.domain.usecases

import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.repository.MessageRepository


class SendMessageUseCase(private val repository: MessageRepository) {
    suspend operator fun invoke(requestDto: MessageRequestDto) {
        repository.insertMessage(requestDto)
    }
}
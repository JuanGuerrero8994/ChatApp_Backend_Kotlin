package com.ktor.domain.usecases.message

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.domain.model.Message
import com.ktor.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow


class SendMessageUseCase(private val repository: MessageRepository) {
    suspend operator fun invoke(message: Message): Flow<ApiResponse<String>> = repository.sendMessage(message)
}
package com.ktor.domain.usecases.message

import com.ktor.core.Resource
import com.ktor.domain.repository.MessageRepository

class GetFileMessageUseCase(private val repository: MessageRepository) {
    suspend operator fun invoke(fileId:String): Resource<ByteArray> = repository.getFileFromGridFS(fileId)
}


package com.ktor.domain.usecases.message

import com.ktor.core.Resource
import com.ktor.domain.repository.MessageRepository

class UploadFileMessageUseCase(private val repository: MessageRepository) {
    suspend operator fun invoke(bytes: ByteArray, fileName: String, contentType: String): Resource<String> = repository.uploadFileToGridFS(bytes,fileName,contentType)
}
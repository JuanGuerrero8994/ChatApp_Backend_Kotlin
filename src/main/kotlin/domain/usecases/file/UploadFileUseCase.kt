package com.ktor.domain.usecases.file

import com.ktor.core.Resource
import com.ktor.domain.model.File
import com.ktor.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow

class UploadFileUseCase(private val repository: FileRepository) {
    suspend operator fun invoke(file:File): Flow<Resource<File>> = repository.uploadFileToGridFS(file)
}
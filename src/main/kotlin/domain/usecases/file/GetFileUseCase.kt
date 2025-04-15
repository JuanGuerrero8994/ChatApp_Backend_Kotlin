package com.ktor.domain.usecases.file

import com.ktor.core.Resource
import com.ktor.domain.model.File
import com.ktor.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow

class GetFileUseCase(private val repository: FileRepository) {
    suspend operator fun invoke(fileId:String): Flow<Resource<File>> = repository.getFileFromGridFS(fileId)
}


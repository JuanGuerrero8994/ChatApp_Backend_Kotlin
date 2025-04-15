package com.ktor.domain.repository

import com.ktor.core.Resource
import com.ktor.domain.model.File
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    suspend fun uploadFileToGridFS(file: File): Flow<Resource<File>>
    suspend fun getFileFromGridFS(fileId: String): Flow<Resource<File>>
}
package com.ktor.data.repository

import com.ktor.core.Resource
import com.ktor.data.service.GridFSService
import com.ktor.domain.model.File
import com.ktor.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayInputStream


class FileRepositoryImpl(
    private val gridFSService: GridFSService
) : FileRepository {

    override suspend fun uploadFileToGridFS(
        file: File
    ): Flow<Resource<File>> = flow {
        emit(Resource.Loading())

        try {
            // 1. Subir archivo al bucket con metadata
            val inputStream = ByteArrayInputStream(file.bytes)
            val fileId = gridFSService.uploadFile(inputStream, file.name, file.contentType)

            // 2. Retornar el archivo con el id asignado
            val savedFile = file.copy(id = fileId)
            emit(Resource.Success(savedFile))

        } catch (e: Exception) {
            emit(Resource.Error("Error uploading file: ${e.message}"))
        }
    }

    override suspend fun getFileFromGridFS(fileId: String): Flow<Resource<File>> = flow {
        emit(Resource.Loading())

        try {
            val file = gridFSService.getFile(fileId)

            if (file != null) {
                emit(Resource.Success(file))
            } else {
                emit(Resource.Error("File not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error getting file: ${e.message}"))
        }
    }
}


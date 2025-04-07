package com.ktor.data.repository

import com.ktor.domain.model.Message
import com.ktor.domain.repository.MessageRepository
import com.ktor.core.Resource
import com.ktor.data.mapper.MessageMapper.toDomain
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.data.service.GridFSService
import com.mongodb.client.MongoCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.mongodb.client.MongoDatabase
import org.bson.Document
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class MessageRepositoryImpl(
    private val database: MongoDatabase,
    private val gridService: GridFSService) : MessageRepository {

    private val collection: MongoCollection<Document> = database.getCollection("messages")

    override suspend fun sendMessage(message: Message): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val document = Document().apply {
                put("sender", message.sender)
                put("message", message.message)
                put("timestamp", message.timestamp)
                put("fileUrl", message.fileUrl)
            }

            collection.insertOne(document)

            emit(Resource.Success("Message sent successfully"))

        } catch (e: Exception) {
            throw Exception("Error inserting the message: ${e.message}")
        }
    }


    override suspend fun getAllMessages(): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading())

        try {
            // Convert the Document from MongoDB to MessageDto
            val doc = collection.find().map { document ->
                MessageResponseDto(
                    id = document.getObjectId("_id").toString(),
                    sender = document.getString("sender"),
                    message = document.getString("message"),
                    timestamp = document.getLong("timestamp"),
                    fileUrl = document.getString("fileUrl")
                )
            }.toList()


            val docToMessage = doc.map { it.toDomain() }
            emit(Resource.Success(docToMessage))
        } catch (e: Exception) {
            emit(Resource.Error("Error getting messages ${e.message}"))
        }
    }


    override suspend fun uploadFileToGridFS(bytes: ByteArray, fileName: String, contentType: String): Resource<String> {
        return try {
            val inputStream = ByteArrayInputStream(bytes)
            val fileId = gridService.uploadFile(inputStream, fileName, contentType)
            Resource.Success(fileId)
        } catch (e: Exception) {
            Resource.Error("Error upload file: ${e.message}")
        }
    }

    override suspend fun getFileFromGridFS(fileId: String): Resource<ByteArray> {
        return try {
            val outputStream = ByteArrayOutputStream()
            val success = gridService.downloadFile(fileId, outputStream)
            if (success) {
                Resource.Success(outputStream.toByteArray())
            } else {
                Resource.Error("No se pudo descargar el archivo")
            }
        } catch (e: Exception) {
            Resource.Error("Error get file : ${e.message}")
        }
    }
}

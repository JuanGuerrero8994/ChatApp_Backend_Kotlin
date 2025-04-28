package com.ktor.data.repository

import com.ktor.core.ApiResponse
import com.ktor.data.mapper.toDomain
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.domain.model.Message
import com.ktor.domain.repository.MessageRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.bson.Document

class MessageRepositoryImpl(database: MongoDatabase) : MessageRepository {

    private val collection: MongoCollection<Document> = database.getCollection("messages")

    override suspend fun sendMessage(message: Message): Flow<ApiResponse<String>> = flow {
        try {
            emit(ApiResponse(status = "Success", messages = listOf("Sending message..."), code = 200))

            // Crear el documento a insertar
            val document = Document().apply {
                put("sender", message.sender)
                put("message", message.message)
                put("timestamp", message.timestamp)

                // Guardar fileId si no es nulo o vacío
                message.fileId?.takeIf { it.isNotBlank() }?.let {
                    put("fileId", it)
                }

                // Agregar chatRoomId
                put("chatRoomId", message.chatRoomId)
            }

            collection.insertOne(document)
            emit(ApiResponse(status = "Success", messages = listOf("Message sent successfully"), code = 200))

        } catch (e: Exception) {
            emit(ApiResponse(status = "Error", messages = listOf("Error inserting the message: ${e.message}"), code = 500))
        }
    }

    override suspend fun getAllMessages(): Flow<ApiResponse<List<Message>>> = flow {
        emit(ApiResponse(status = "Loading", messages = listOf("Loading messages..."), code = 200))

        try {
            // Obtener los documentos de MongoDB y mapearlos a DTO
            val docList = collection.find().map { document ->
                MessageResponseDto(
                    id = document.getObjectId("_id").toString(),
                    sender = document.getString("sender"),
                    message = document.getString("message"),
                    timestamp = document.getLong("timestamp"),
                    fileId = document.getString("fileId"),
                    chatRoomId = document.getString("chatRoomId")
                )
            }.toList()

            val messageList = docList.map { it.toDomain() }
            emit(ApiResponse(data = messageList, status = "Success", messages = listOf("Messages retrieved successfully"), code = 200))

        } catch (e: Exception) {
            emit(ApiResponse(status = "Error", messages = listOf("Error getting messages: ${e.message}"), code = 500))
        }
    }

    override suspend fun getMessagesByChatRoomId(chatRoomId: String): Flow<ApiResponse<List<Message>>> = flow {
        emit(ApiResponse(status = "Loading", messages = listOf("Loading messages..."), code = 200))
        try {
            val docList = collection.find(Document("chatRoomId", chatRoomId)).map { document ->
                MessageResponseDto(
                    id = document.getObjectId("_id").toString(),
                    sender = document.getString("sender"),
                    message = document.getString("message"),
                    timestamp = document.getLong("timestamp"),
                    fileId = document.getString("fileId"),
                    chatRoomId = document.getString("chatRoomId")
                )
            }.toList()

            val messageList = docList.map { it.toDomain() }
            emit(ApiResponse(data = messageList, status = "Success", messages = listOf("Messages retrieved successfully for chatRoomId=$chatRoomId"), code = 200))

        } catch (e: Exception) {
            emit(ApiResponse(status = "Error", messages = listOf("Error getting messages for chatRoomId=$chatRoomId: ${e.message}"), code = 500))
        }
    }
}

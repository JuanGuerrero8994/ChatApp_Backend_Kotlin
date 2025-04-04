package com.ktor.data.repository

import com.ktor.data.mapper.MessageMapper.toDomain
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.model.Message
import com.ktor.domain.repository.MessageRepository
import com.ktor.core.Resource
import com.ktor.data.model.message.MessageResponseDto
import com.mongodb.client.MongoCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.mongodb.client.MongoDatabase
import org.bson.Document

class MessageRepositoryImpl(database: MongoDatabase) : MessageRepository {

    private val collection: MongoCollection<Document> = database.getCollection("messages")

    override suspend fun insertMessage(request: MessageRequestDto):Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            // 🔹 Validar que el mensaje no esté vacío
            if (request.message.isBlank()) {
                emit(Resource.Error("El mensaje no puede estar vacío"))
                return@flow
            }

            // 🔹 Validar formato de fileUrl si se envía un archivo
            if (!request.fileUrl.isNullOrBlank() && !request.fileUrl.startsWith("http")) {
                emit(Resource.Error("URL del archivo inválida"))
                return@flow
            }

            val message = request.toDomain()

            val document = Document().apply {
                put("sender", message.sender)
                put("chatRoomId", message.chatRoomId)
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


    override fun getAllMessages(): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading())

        try {
            // Convert the Document from MongoDB to MessageDto
            val messageDto = collection.find().map { document ->
                MessageResponseDto(
                    id = document.getObjectId("_id").toString(),
                    sender = document.getString("sender"),
                    chatRoomId = document.getString("chatRoomId"),
                    message = document.getString("message"),
                    timestamp = document.getString("timestamp"),
                    fileUrl = document.getString("fileUrl")
                )
            }.toList()


            val message = messageDto.map { it.toDomain() }
            emit(Resource.Success(message))  // Emit the result to the domain layer
        } catch (e: Exception) {
            emit(Resource.Error("Error getting messages", e))
        }
    }
    override fun getMessagesByChatRoomId(chatRoomId: String): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading())

        try {
            val messages = collection.find(Document("chatRoomId", chatRoomId)).map { document ->
                MessageResponseDto(
                    id = document.getObjectId("_id").toString(),
                    sender = document.getString("sender"),
                    chatRoomId = document.getString("chatRoomId"),
                    message = document.getString("message"),
                    timestamp = document.getString("timestamp"),
                    fileUrl = document.getString("fileUrl")
                ).toDomain()
            }.toList()

            emit(Resource.Success(messages))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al obtener mensajes por sala de chat: ${e.localizedMessage}"))
        }
    }
}

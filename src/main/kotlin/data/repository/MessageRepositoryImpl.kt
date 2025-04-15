package com.ktor.data.repository

import com.ktor.core.Resource
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

    override suspend fun sendMessage(message: Message): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            // Crear el documento a insertar
            val document = Document().apply {
                put("sender", message.sender)
                put("message", message.message)
                put("timestamp", message.timestamp)

                // Guardar fileId si no es nulo o vacío
                message.fileId?.takeIf { it.isNotBlank() }?.let {
                    put("fileId", it)
                }

                // ❗ Agregá esta línea para guardar el chatRoomId
                put("chatRoomId", message.chatRoomId)

            }

            collection.insertOne(document)
            emit(Resource.Success("Message sent successfully"))

        } catch (e: Exception) {
            emit(Resource.Error("Error inserting the message: ${e.message}"))
        }
    }

    override suspend fun getAllMessages(): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading())

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
            emit(Resource.Success(messageList))

        } catch (e: Exception) {
            emit(Resource.Error("Error getting messages: ${e.message}"))
        }
    }


    override suspend fun getMessagesByChatRoomId(chatRoomId: String): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading())
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
            emit(Resource.Success(messageList))

        } catch (e: Exception) {
            emit(Resource.Error("Error getting messages for chatRoomId=$chatRoomId: ${e.message}"))
        }

    }

}

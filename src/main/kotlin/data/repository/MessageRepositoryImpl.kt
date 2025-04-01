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

    override suspend fun insertMessage(request: MessageRequestDto) {
        try {
            val message = request.toDomain()

            // Convert MessageDto to Document
            val document = Document().apply {
                put("sender", message.sender)
                put("message", message.message)
                put("timestamp", message.timestamp)
                put("fileUrl", message.fileUrl)
            }

            collection.insertOne(document)  // Insert Document into MongoDB
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
}

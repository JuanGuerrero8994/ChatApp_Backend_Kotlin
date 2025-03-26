package com.ktor.data.repository

import com.ktor.data.mapper.MessageMapper.toDomain
import com.ktor.data.mapper.MessageMapper.toDto
import com.ktor.data.model.message.MessageDto
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.model.Message
import com.ktor.domain.repository.MessageRepository
import com.ktor.core.Resource
import com.mongodb.client.MongoCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.mongodb.client.MongoDatabase
import org.bson.Document

class MessageRepositoryImpl(database: MongoDatabase) : MessageRepository {

    private val collection: MongoCollection<Document> = database.getCollection("messages")

    override suspend fun insertMessage(requestDto: MessageRequestDto) {
        try {
            val message = requestDto.toDomain()  // Convert RequestDto → Message (Domain)
            val messageDto = message.toDto()  // Convert Message (Domain) → MessageDto

            // Convert MessageDto to Document
            val document = Document().apply {
                put("sender", messageDto.sender)
                put("message", messageDto.message)
                put("timestamp", messageDto.timestamp)
                put("fileUrl", messageDto.fileUrl)
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
            val messagesDto = collection.find().map { document ->
                // Manually convert Document to MessageDto
                MessageDto(
                    id = document.getObjectId("_id"),
                    sender = document.getString("sender"),
                    message = document.getString("message"),
                    timestamp = document.getString("timestamp").toLong(),
                    fileUrl = document.getString("fileUrl")
                )
            }.toList()  // Collect the results into a list

            // Convert MessageDto to Message (Domain)
            val messages = messagesDto.map { it.toDomain() }
            emit(Resource.Success(messages))  // Emit the result to the domain layer
        } catch (e: Exception) {
            emit(Resource.Error("Error getting messages", e))
        }
    }
}

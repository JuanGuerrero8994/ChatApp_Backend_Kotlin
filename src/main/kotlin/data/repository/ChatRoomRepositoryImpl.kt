package com.ktor.data.repository

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.data.mapper.toDocument
import com.ktor.data.mapper.toDomain
import com.ktor.domain.model.ChatRoom
import com.ktor.domain.repository.ChatRoomRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.bson.Document
import org.bson.types.ObjectId



class ChatRoomRepositoryImpl(database: MongoDatabase) : ChatRoomRepository {

    private val collectionChatRooms: MongoCollection<Document> = database.getCollection("chat_rooms")

    override suspend fun createChatRoom(chatRoom: ChatRoom): Flow<ApiResponse<ChatRoom>> = flow {
        try {
            val existingRoom = collectionChatRooms.find(Document("name", chatRoom.name)).firstOrNull()
            if (existingRoom != null) {
                emit(ApiResponse(data = existingRoom.toDomain(), status = "Success", messages = listOf("Sala ya existente"), code = 200))
                return@flow
            }

            val result = collectionChatRooms.insertOne(chatRoom.toDocument())
            if (result.wasAcknowledged()) {
                val insertedRoom = collectionChatRooms.find(Document("_id", ObjectId(chatRoom.id))).firstOrNull()
                if (insertedRoom != null) {
                    emit(ApiResponse(data = insertedRoom.toDomain(), status = "Success", messages = listOf("Sala creada exitosamente"), code = 200))
                } else {
                    emit(ApiResponse(status = "Error", messages = listOf("No se pudo recuperar la sala insertada"), code = 500))
                }
            } else {
                emit(ApiResponse(status = "Error", messages = listOf("No se pudo crear la sala de chat"), code = 500))
            }
        } catch (e: Exception) {
            emit(ApiResponse(status = "Error", messages = listOf("Error al crear la sala: ${e.localizedMessage}"), code = 500))
        }
    }

    override suspend fun getChatRoomById(id: String): Flow<ApiResponse<ChatRoom>> = flow {
        try {
            if (!ObjectId.isValid(id)) {
                emit(ApiResponse(status = "Error", messages = listOf("ID inválido"), code = 400))
                return@flow
            }

            val room = collectionChatRooms.find(Document("_id", ObjectId(id))).firstOrNull()

            if (room != null) {
                emit(ApiResponse(data = room.toDomain(), status = "Success", messages = listOf("Sala encontrada"), code = 200))
            } else {
                emit(ApiResponse(status = "Error", messages = listOf("Sala no encontrada"), code = 404))
            }
        } catch (e: Exception) {
            emit(ApiResponse(status = "Error", messages = listOf("Error al buscar la sala: ${e.localizedMessage}"), code = 500))
        }
    }

    override suspend fun getAllChatRooms(): Flow<ApiResponse<List<ChatRoom>>> = flow {
        try {
            val rooms = collectionChatRooms.find().map { it.toDomain() }.toList()
            emit(ApiResponse(data = rooms, status = "Success", messages = listOf("Salas obtenidas exitosamente"), code = 200))
        } catch (e: Exception) {
            emit(ApiResponse(status = "Error", messages = listOf("Error al obtener salas: ${e.localizedMessage}"), code = 500))
        }
    }

    override suspend fun addUserToChatRoom(chatRoomId: String, userId: String): Flow<ApiResponse<Boolean>> = flow {
        try {
            val room = collectionChatRooms.find(Document("_id", ObjectId(chatRoomId))).firstOrNull()
            if (room == null) {
                emit(ApiResponse(status = "Error", messages = listOf("La sala no existe"), code = 404))
                return@flow
            }

            val currentUsers = room.getList("users", String::class.java)
            if (userId in currentUsers) {
                emit(ApiResponse(status = "Error", messages = listOf("El usuario ya pertenece a la sala"), code = 409))
                return@flow
            }

            val update = collectionChatRooms.updateOne(
                Document("_id", ObjectId(chatRoomId)),
                Document("\$addToSet", Document("users", userId))
            )

            if (update.modifiedCount > 0) {
                emit(ApiResponse(data = true, status = "Success", messages = listOf("Usuario agregado a la sala"), code = 200))
            } else {
                emit(ApiResponse(status = "Error", messages = listOf("No se pudo agregar el usuario a la sala"), code = 500))
            }
        } catch (e: Exception) {
            emit(ApiResponse(status = "Error", messages = listOf("Error al agregar usuario: ${e.localizedMessage}"), code = 500))
        }
    }

    override suspend fun removeChatRoom(id: String): Flow<ApiResponse<Boolean>> = flow {
        try {
            val result = collectionChatRooms.deleteOne(Document("_id", ObjectId(id)))
            if (result.deletedCount > 0) {
                emit(ApiResponse(data = true, status = "Success", messages = listOf("Sala eliminada exitosamente"), code = 200))
            } else {
                emit(ApiResponse(status = "Error", messages = listOf("Sala no encontrada para eliminar"), code = 404))
            }
        } catch (e: Exception) {
            emit(ApiResponse(status = "Error", messages = listOf("Error al eliminar la sala: ${e.localizedMessage}"), code = 500))
        }
    }

    override suspend fun removeUserFromChatRoom(chatRoomId: String, userId: String): Flow<ApiResponse<Boolean>> = flow {
        try {
            val chatRoom = collectionChatRooms.find(Document("_id", ObjectId(chatRoomId))).firstOrNull()
            if (chatRoom == null) {
                emit(ApiResponse(status = "Error", messages = listOf("La sala con ID $chatRoomId no existe"), code = 404))
                return@flow
            }

            val users = chatRoom.getList("users", String::class.java)
            if (!users.contains(userId)) {
                emit(ApiResponse(status = "Error", messages = listOf("El usuario con ID $userId no está en la sala"), code = 404))
                return@flow
            }

            val update = Updates.pull("users", userId)
            val result = collectionChatRooms.updateOne(Document("_id", ObjectId(chatRoomId)), update)

            if (result.modifiedCount > 0) {
                emit(ApiResponse(data = true, status = "Success", messages = listOf("Usuario eliminado de la sala"), code = 200))
            } else {
                emit(ApiResponse(status = "Error", messages = listOf("No se pudo eliminar el usuario de la sala"), code = 500))
            }
        } catch (e: Exception) {
            emit(ApiResponse(status = "Error", messages = listOf("Error al eliminar usuario de la sala: ${e.localizedMessage}"), code = 500))
        }
    }
}

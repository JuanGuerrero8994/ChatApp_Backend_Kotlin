package com.ktor.data.repository

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

    override suspend fun createChatRoom(chatRoom: ChatRoom): Flow<Resource<ChatRoom>> = flow {
        emit(Resource.Loading())

        try {
            val existingRoom = collectionChatRooms.find(
                Document("name", chatRoom.name)
            ).firstOrNull()

            if (existingRoom != null) {
                emit(Resource.Success(existingRoom.toDomain()))
                return@flow
            }

            val result = collectionChatRooms.insertOne(chatRoom.toDocument())
            if (result.wasAcknowledged()) {
                val insertedRoom = collectionChatRooms.find(
                    Document("_id", ObjectId(chatRoom.id))
                ).firstOrNull()

                if (insertedRoom != null) {
                    emit(Resource.Success(insertedRoom.toDomain()))
                } else {
                    emit(Resource.Error("No se pudo recuperar la sala insertada"))
                }
            } else {
                emit(Resource.Error("No se pudo crear la sala de chat"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al crear la sala: ${e.localizedMessage}"))
        }
    }

    override suspend fun getChatRoomById(id: String): Flow<Resource<ChatRoom?>> = flow {
        emit(Resource.Loading())

        try {
            val room = collectionChatRooms.find(Document("_id", ObjectId(id))).firstOrNull()
            if (room != null) {
                emit(Resource.Success(room.toDomain()))
            } else {
                emit(Resource.Error("Sala no encontrada"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al buscar la sala: ${e.localizedMessage}"))
        }
    }

    override suspend fun getAllChatRooms(): Flow<Resource<List<ChatRoom>>> = flow {
        emit(Resource.Loading())

        try {
            val rooms = collectionChatRooms.find().map { it.toDomain() }.toList()
            emit(Resource.Success(rooms))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al obtener salas: ${e.localizedMessage}"))
        }
    }

    override suspend fun addUserToChatRoom(chatRoomId: String, userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val room = collectionChatRooms.find(Document("_id", ObjectId(chatRoomId))).firstOrNull()

            if (room == null) {
                emit(Resource.Error("La sala no existe"))
                return@flow
            }

            val currentUsers = room.getList("users", String::class.java)
            if (userId in currentUsers) {
                emit(Resource.Error("El usuario ya pertenece a la sala"))
                return@flow
            }

            val update = collectionChatRooms.updateOne(
                Document("_id", ObjectId(chatRoomId)),
                Document("\$addToSet", Document("users", userId))
            )

            when {
                update.matchedCount == 0L ->
                    emit(Resource.Error("La sala no existe"))

                update.modifiedCount == 0L ->
                    emit(Resource.Error("El usuario ya pertenece a la sala"))

                else ->
                    emit(Resource.Success(true))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al agregar usuario: ${e.localizedMessage}"))
        }
    }

    override suspend fun removeChatRoom(id: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val result = collectionChatRooms.deleteOne(Document("_id", ObjectId(id)))
            if (result.deletedCount > 0) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Sala no encontrada para eliminar"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al eliminar la sala: ${e.localizedMessage}"))
        }
    }

    override suspend fun removeUserFromChatRoom(chatRoomId: String, userId: String): Flow<Resource<Boolean>> = flow{
        emit(Resource.Loading())

        try {
            // 1. Verificar si la sala existe
            val chatRoom = collectionChatRooms.find(Document("_id", ObjectId(chatRoomId))).firstOrNull()
            if (chatRoom == null) {
                emit(Resource.Error("La sala con ID $chatRoomId no existe"))
                return@flow
            }

            // 2. Verificar si el usuario está en la sala
            val users = chatRoom.getList("users", String::class.java)
            if (!users.contains(userId)) {
                emit(Resource.Error("El usuario con ID $userId no está en la sala"))
                return@flow
            }

            // 3. Eliminar el usuario del array
            val filter = Document("_id", ObjectId(chatRoomId))
            val update = Updates.pull("users", userId)
            val result = collectionChatRooms.updateOne(filter, update)

            if (result.modifiedCount > 0) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("No se pudo eliminar el usuario de la sala"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al eliminar usuario de la sala: ${e.localizedMessage}"))
        }
    }
}

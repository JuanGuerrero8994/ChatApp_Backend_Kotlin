package com.ktor.data.repository
/*
import com.ktor.core.Resource
import com.ktor.data.mapper.ChatRoomMapper.toDocument
import com.ktor.data.mapper.ChatRoomMapper.toDomain
import com.ktor.data.model.chat.ChatRoomRequestDto
import com.ktor.domain.model.ChatRoom
import com.ktor.domain.repository.ChatRoomRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.bson.Document
import org.bson.types.ObjectId

class ChatRoomRepositoryImpl(database: MongoDatabase) : ChatRoomRepository {

    private val collectionChatRooms: MongoCollection<Document> = database.getCollection("chat_rooms")
    private val collectionUsers: MongoCollection<Document> = database.getCollection("users")

    override suspend fun createChatRoom(chatRoom: ChatRoomRequestDto): Flow<Resource<ChatRoom>> = flow {
        emit(Resource.Loading())

        try {
            // 🔍 Buscar si ya existe un canal con ese nombre (o lógica personalizada)
            val existingChatRoom = collectionChatRooms.find(
                Document("name", chatRoom.name)
            ).firstOrNull()

            if (existingChatRoom != null) {
                // ✅ Ya existe, devolvemos el existente
                emit(Resource.Success(existingChatRoom.toDomain()))
                return@flow
            }

            // 🛠 Convertir a dominio y guardar nuevo
            val chatRoomDomain = chatRoom.toDomain()
            val result = collectionChatRooms.insertOne(chatRoomDomain.toDocument())

            if (result.wasAcknowledged()) {
                val insertedId = result.insertedId?.asObjectId()?.value
                val chatRoomInserted = insertedId?.let {
                    collectionChatRooms.find(Document("_id", it)).firstOrNull()
                }

                if (chatRoomInserted != null) {
                    emit(Resource.Success(chatRoomInserted.toDomain()))
                } else {
                    emit(Resource.Error("Error al recuperar la sala de chat después de la inserción"))
                }
            } else {
                emit(Resource.Error("No se pudo insertar la sala de chat"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al crear la sala de chat: ${e.localizedMessage}"))
        }
    }
    override fun getChatRoomById(id: String): Flow<Resource<ChatRoom?>> = flow {
        emit(Resource.Loading())
        try {
            val document = collectionChatRooms.find(Document("_id", ObjectId(id))).firstOrNull()
            if (document != null) {
                emit(Resource.Success(document.toDomain()))
            } else {
                emit(Resource.Error("Sala no encontrada"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al obtener la sala de chat: ${e.localizedMessage}"))
        }
    }
    override fun getAllChatRooms(): Flow<Resource<List<ChatRoom>>> = flow {
        emit(Resource.Loading())
        try {
            val chatRooms = collectionChatRooms.find().map { it.toDomain() }.toList()
            emit(Resource.Success(chatRooms))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al obtener las salas de chat: ${e.localizedMessage}"))
        }
    }

    override suspend fun addUserToChatRoom(chatRoomId: String, userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val userDoc = collectionUsers.find(Document("_id", userId)).firstOrNull()
                ?: return@flow emit(Resource.Error("Usuario no encontrado"))

            val updateResult = collectionChatRooms.updateOne(
                Document("_id", chatRoomId),
                Document("\$push", Document("users", userDoc))
            )

            if (updateResult.modifiedCount > 0) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("No se pudo agregar el usuario"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al agregar usuario a la sala: ${e.localizedMessage}"))
        }
    }

    override suspend fun removeChatRoom(id: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val result = collectionChatRooms.deleteOne(Document("_id", ObjectId(id)))
            if (result.deletedCount > 0) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("No se encontró la sala"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error("Error al eliminar la sala: ${e.localizedMessage}"))
        }
    }
}*/

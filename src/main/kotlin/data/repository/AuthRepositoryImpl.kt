package com.ktor.data.repository

import com.ktor.core.JWTUtil
import com.ktor.core.Resource
import com.ktor.data.mapper.UserMapper.toDomain
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.domain.model.User
import com.ktor.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.mongodb.client.MongoCollection
import org.bson.Document
import com.mongodb.client.MongoDatabase
import org.mindrot.jbcrypt.BCrypt // 🔒 Importamos BCrypt

class UserRepositoryImpl(database: MongoDatabase) : UserRepository {

    private val collection: MongoCollection<Document> = database.getCollection("users")

    override suspend fun registerUser(userRequestDTO: UserRequestDTO): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val user = userRequestDTO.toDomain() // 🔄 Convertimos UserRequestDTO a User

            val document = Document().apply {
                put("username", user.username)
                put("email", user.email)
                put("passwordHash", user.passwordHash) // 🔒 Guardamos la contraseña encriptada
            }
            collection.insertOne(document)

            // Obtenemos el ID generado por MongoDB
            val createdUser = user.copy(id = document.getObjectId("_id").toString())

            emit(Resource.Success(createdUser))
        } catch (e: Exception) {
            emit(Resource.Error("Error registering user", e))
        }
    }


    override suspend fun findUser(username: String): Flow<Resource<User?>> = flow {
        emit(Resource.Loading())
        try {
            val document = collection.find(Document("username", username)).firstOrNull()
            if (document != null) {
                val user = User(
                    id = document.getObjectId("_id").toString(),
                    username = document.getString("username"),
                    email = document.getString("email"),
                    passwordHash = document.getString("passwordHash")
                )
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error retrieving user", e))
        }
    }

    override suspend fun authenticateUser(username: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val document = collection.find(Document("username", username)).firstOrNull()
            if (document != null) {
                val storedPasswordHash = document.getString("passwordHash")

                if (BCrypt.checkpw(password, storedPasswordHash)) { // 🔒 Verificación segura
                    // ✅ Generar JWT
                    val token = JWTUtil.generateToken(username)
                    emit(Resource.Success(token))
                } else {
                    emit(Resource.Error("Invalid password"))
                }
            } else {
                emit(Resource.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error during authentication", e))
        }
    }
}

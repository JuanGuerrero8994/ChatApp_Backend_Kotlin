package com.ktor.data.repository

import UserMapper.toDomain
import com.ktor.core.JWTUtil
import com.ktor.core.Resource
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.domain.model.Message
import com.ktor.domain.model.User
import com.ktor.domain.repository.UserRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt

class UserRepositoryImpl(database: MongoDatabase) : UserRepository {

    private val collection: MongoCollection<Document> = database.getCollection("users")

    override suspend fun registerUser(userRequestDTO: UserRequestDTO): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val (user, hashedPassword) = userRequestDTO.toDomain() // ✅ Obtenemos usuario sin password y el hash

            val document = Document().apply {
                put("username", user.username)
                put("email", user.email)
                put("passwordHash", hashedPassword) // 🔒 Guardamos el hash en la DB
            }
            collection.insertOne(document)

            val createdUser = user.copy(id = document.getObjectId("_id").toString())

            emit(Resource.Success(createdUser)) // ✅ Devuelve solo el modelo del dominio
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

    override suspend fun validateToken(token: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val username = JWTUtil.validateToken(token) // 🔄 Extraer username del token

            if (username != null) {
                val document = collection.find(Document("username", username)).firstOrNull()
                if (document != null) {


                    val user = User(
                        id = document.getObjectId("_id").toString(),
                        username = document.getString("username"),
                        email = document.getString("email"),
                    )
                    emit(Resource.Success(user)) // ✅ Usuario validado correctamente
                } else {
                    emit(Resource.Error("User not found"))
                }
            } else {
                emit(Resource.Error("Invalid token"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error validating token", e))
        }
    }

}

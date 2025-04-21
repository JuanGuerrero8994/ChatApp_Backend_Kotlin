package com.ktor.data.repository

import com.ktor.core.ApiResponse
import com.ktor.core.JWTUtil
import com.ktor.domain.model.User
import com.ktor.domain.repository.UserRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt
import toUser

class UserRepositoryImpl(database: MongoDatabase) : UserRepository {

    private val collection: MongoCollection<Document> = database.getCollection("users")

    override suspend fun registerUser(user: User): Flow<ApiResponse<User>> = flow {
        try {
            val doc = Document().apply {
                put("username", user.username)
                put("email", user.email)
                put("passwordHash", BCrypt.hashpw(user.password, BCrypt.gensalt()))
            }
            collection.insertOne(doc)

            // Emitir respuesta exitosa con el usuario registrado
            emit(ApiResponse(data = doc.toUser(), status = "success", messages = listOf("User registered successfully"), code = 200))
        } catch (e: Exception) {
            // Emitir error si algo sale mal
            emit(ApiResponse(status = "error", messages = listOf("Error registering user: ${e.message}"), code = 500))
        }
    }

    override suspend fun findUser(user: User): Flow<ApiResponse<User>> = flow {
        try {
            // Emitir estado de loading

            val document = collection.find(Document("username", user.username)).firstOrNull()

            if (document != null) {
                // Emitir respuesta exitosa si se encuentra el usuario
                emit(ApiResponse(data = user, status = "success", messages = listOf("User found"), code = 200))
            } else {
                // Emitir error si el usuario no es encontrado
                emit(ApiResponse<User>(status = "error", messages = listOf("User not found"), code = 404))
            }
        } catch (e: Exception) {
            // Emitir error en caso de fallo
            emit(ApiResponse<User>(status = "error", messages = listOf("Error retrieving user: ${e.message}"), code = 500))
        }
    }

    override suspend fun authenticateUser(user: User): Flow<ApiResponse<String>> = flow {
        try {
            // Emitir estado de loading

            val document = collection.find(Document("email", user.email)).firstOrNull()

            if (document != null) {
                val storedPasswordHash = document.getString("passwordHash") ?: ""
                if (BCrypt.checkpw(user.password, storedPasswordHash)) {
                    val username = document.getString("email")

                    val token = JWTUtil.generateToken(username)

                    // Emitir respuesta exitosa con el token
                    emit(ApiResponse(data = token, status = "success", messages = listOf("Authentication successful"), code = 200))
                } else {
                    // Emitir error de credenciales inválidas
                    emit(ApiResponse<String>(status = "error", messages = listOf("Invalid credentials"), code = 401))
                }
            } else {
                // Emitir error si el usuario no existe
                emit(ApiResponse<String>(status = "error", messages = listOf("User not found"), code = 404))
            }
        } catch (e: Exception) {
            // Emitir error en caso de fallo
            emit(ApiResponse<String>(status = "error", messages = listOf("Error during authentication: ${e.message}"), code = 500))
        }
    }

    override suspend fun validateToken(token: String): Flow<ApiResponse<User>> = flow {
        try {
            // Emitir estado de loading

            val email = JWTUtil.validateToken(token) // Extraer el email del token

            if (email != null) {
                val document = collection.find(Document("email", email)).firstOrNull()
                if (document != null) {
                    val user = User(
                        id = document.getObjectId("_id").toString(),
                        username = document.getString("username"),
                        email = document.getString("email"),
                        password = document.getString("passwordHash")
                    )

                    // Emitir respuesta exitosa con el usuario
                    emit(ApiResponse(data = user, status = "success", messages = listOf("Token validated"), code = 200))
                } else {
                    // Emitir error si el usuario no es encontrado
                    emit(ApiResponse<User>(status = "error", messages = listOf("User not found"), code = 404))
                }
            } else {
                // Emitir error si el token no es válido
                emit(ApiResponse<User>(status = "error", messages = listOf("Invalid token"), code = 401))
            }
        } catch (e: Exception) {
            // Emitir error en caso de fallo
            emit(ApiResponse<User>(status = "error", messages = listOf("Error validating token: ${e.message}"), code = 500))
        }
    }
}

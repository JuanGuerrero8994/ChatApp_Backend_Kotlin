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

class UserRepositoryImpl(database: MongoDatabase) : UserRepository {

    private val collection: MongoCollection<Document> = database.getCollection("users")

    override suspend fun registerUser(user: User): Flow<ApiResponse<String>> = flow {
        try {
            val doc = Document().apply {
                put("username", user.username)
                put("email", user.email)
                put("passwordHash", BCrypt.hashpw(user.password, BCrypt.gensalt()))
            }
            collection.insertOne(doc)

            // Emitir respuesta exitosa con el usuario registrado
            emit(ApiResponse(status = "Success", messages = listOf("User registered successfully"), code = 200))
        } catch (e: Exception) {
            // Emitir error si algo sale mal
            emit(ApiResponse(status = "Error", messages = listOf("Error registering user: ${e.message}"), code = 500))
        }
    }


    override suspend fun authenticateUser(user: User): Flow<ApiResponse<String>> = flow {
        try {
            // Emitir estado de loading

            val document = collection.find(Document("email", user.email)).firstOrNull()

            if (document != null) {
                val storedPasswordHash = document.getString("passwordHash") ?: ""
                if (BCrypt.checkpw(user.password, storedPasswordHash)) {

                    val username = document.getString("username")

                    val token = JWTUtil.generateToken(username)

                    // Emitir respuesta exitosa con el token
                    emit(ApiResponse(data = token, status = "Success", messages = listOf("Authentication successful"), code = 200))
                } else {
                    // Emitir error de credenciales inválidas
                    emit(ApiResponse(status = "Error", messages = listOf("Invalid credentials"), code = 401))
                }
            } else {
                // Emitir error si el usuario no existe
                emit(ApiResponse(status = "Error", messages = listOf("User not found"), code = 404))
            }
        } catch (e: Exception) {
            // Emitir error en caso de fallo
            emit(ApiResponse(status = "Error", messages = listOf("Error during authentication: ${e.message}"), code = 500))
        }
    }

    override suspend fun changePassword(user: User, newPassword: String): Flow<ApiResponse<String>> = flow {
        try {
            val userDoc = collection.find(Document("email", user.email)).firstOrNull()


            if(userDoc==null){
                emit(
                    ApiResponse(
                        status = "Error",
                        messages = listOf("User Not Found"),
                        code = 404
                    )
                )
                return@flow
            }

            val storedHash = userDoc.getString("passwordHash")

            if (storedHash == null || !BCrypt.checkpw(user.password, storedHash)) {
                emit(
                    ApiResponse(
                        status = "Error",
                        messages = listOf("Incorrect email or password."),
                        code = 401
                    )
                )
                return@flow
            }


            val newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())

            val updateResult = collection.updateOne(
                Document("email", user.email),
                Document("\$set", Document("passwordHash", newHash))
            )

            if (updateResult.modifiedCount > 0) {
                emit(
                    ApiResponse(
                        status = "Success",
                        messages = listOf("Password changed successfully."),
                        code = 200
                    )
                )
            } else {
                emit(
                    ApiResponse(
                        status = "Success",
                        messages = listOf("Password update failed. Try again."),
                        code = 500
                    )
                )
            }

        } catch (e: Exception) {
            emit(
                ApiResponse(
                    status = "Error",
                    messages = listOf("Unexpected error: ${e.localizedMessage ?: "Unknown error"}"),
                    code = 500
                )
            )
        }
    }

}

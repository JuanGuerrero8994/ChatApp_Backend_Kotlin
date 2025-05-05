package com.ktor.data.repository

import com.ktor.core.ApiResponse
import com.ktor.core.JWTUtil
import com.ktor.domain.model.User
import com.ktor.domain.repository.ValidateTokenRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.bson.Document

class ValidateTokenImpl(mongoDatabase: MongoDatabase) : ValidateTokenRepository {

    private val collection: MongoCollection<Document> = mongoDatabase.getCollection("users")

    override suspend fun validateToken(token: String): Flow<User> = flow {
        try {

            val username = JWTUtil.validateToken(token) // Extraer el email del token

            if (username != null) {
                val document = collection.find(Document("username", username)).firstOrNull()
                if (document != null) {
                    val user = User(
                        id = document.getObjectId("_id").toString(),
                        username = document.getString("username"),
                        email = document.getString("email"),
                        password = document.getString("passwordHash")
                    )
                    emit(user)
                } else {
                    // Emitir error si el usuario no es encontrado
                    emit(User())
                }
            }
        } catch (e: Exception) {
            // Emitir error en caso de fallo
            e.printStackTrace()
        }
    }
}
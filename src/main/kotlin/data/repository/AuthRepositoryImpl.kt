package com.ktor.data.repository

import com.ktor.core.Resource
import com.ktor.domain.model.User
import com.ktor.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.mongodb.client.MongoCollection
import org.bson.Document
import com.mongodb.client.MongoDatabase

class UserRepositoryImpl(database: MongoDatabase) : UserRepository {

    private val collection: MongoCollection<Document> = database.getCollection("users")

    override suspend fun registerUser(user: User): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val document = Document().apply {
                put("username", user.username)
                put("email", user.email)
                put("passwordHash", user.passwordHash) // Asegúrate de encriptarlo antes
            }
            collection.insertOne(document)
            emit(Resource.Success(user))
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

    override suspend fun authenticateUser(username: String, password: String): Flow<Resource<User?>> = flow {
        emit(Resource.Loading())
        try {
            val document = collection.find(Document("username", username)).firstOrNull()
            if (document != null) {
                val storedPasswordHash = document.getString("passwordHash")
                if (storedPasswordHash == password) {  // ¡Mejor usa BCrypt o SHA256!
                    val user = User(
                        id = document.getObjectId("_id").toString(),
                        username = document.getString("username"),
                        email = document.getString("email"),
                        passwordHash = storedPasswordHash
                    )
                    emit(Resource.Success(user))
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

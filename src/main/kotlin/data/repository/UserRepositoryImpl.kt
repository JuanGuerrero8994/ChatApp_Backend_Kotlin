package com.ktor.data.repository

 import com.ktor.core.JWTUtil
import com.ktor.core.Resource
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

    override suspend fun registerUser(user: User): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val doc = Document().apply {
                put("username", user.username)
                put("email", user.email)
                put("passwordHash", BCrypt.hashpw(user.password, BCrypt.gensalt()))
            }
            collection.insertOne(doc)
            emit(Resource.Success(doc.toUser()))
        } catch (e: Exception) {
            emit(Resource.Error("Error registering user", e))
        }
    }


    override suspend fun findUser(user: User): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val document = collection.find(Document("username", user.username)).firstOrNull()

            if (document != null) {
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error retrieving user", e))
        }
    }

    override suspend fun authenticateUser(user:User): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val document = collection.find(Document("email", user.email)).firstOrNull()

            if (document != null) {
                val storedPasswordHash = document.getString("passwordHash") ?: ""
                if (BCrypt.checkpw(user.password, storedPasswordHash)) {
                    val username = document.getString("email")

                    val token = JWTUtil.generateToken(username)
                    emit(Resource.Success(token))
                } else {
                    emit(Resource.Error("Invalid credentials"))
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
            val email = JWTUtil.validateToken(token) // 🔄 Extraer username del token

            if (email != null) {
                val document = collection.find(Document("email", email)).firstOrNull()
                if (document != null) {

                    val user = User(
                        id = document.getObjectId("_id").toString(),
                        username = document.getString("username"),
                        email = document.getString("email"),
                        password = document.getString("passwordHash")
                    )
                    emit(Resource.Success(user))
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

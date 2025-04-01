package com.ktor.domain.repository

import com.ktor.core.Resource
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun findUser(username: String): Flow<Resource<User?>>
    suspend fun registerUser(user: User): Flow<Resource<User>>  // Usamos Flow para emitir los estados
    suspend fun authenticateUser(username: String, password: String): Flow<Resource<User?>>

}
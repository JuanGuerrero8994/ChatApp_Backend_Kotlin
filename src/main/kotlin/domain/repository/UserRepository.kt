package com.ktor.domain.repository

import com.ktor.core.Resource
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.data.model.user.UserResponseDTO
import com.ktor.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun findUser(username: String): Flow<Resource<User?>>
    suspend fun registerUser(userRequestDTO: UserRequestDTO): Flow<Resource<User>>
    suspend fun authenticateUser(username: String, password: String): Flow<Resource<String>>
    suspend fun validateToken(token:String) :Flow<Resource<User>>
}
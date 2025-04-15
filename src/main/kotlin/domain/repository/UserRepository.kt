package com.ktor.domain.repository

import com.ktor.core.Resource
import com.ktor.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun findUser(user:User): Flow<Resource<User>>
    suspend fun registerUser(user:User): Flow<Resource<User>>
    suspend fun authenticateUser(user:User): Flow<Resource<String>>
    suspend fun validateToken(token:String) :Flow<Resource<User>>
}
package com.ktor.domain.repository

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun findUser(user:User): Flow<ApiResponse<User>>
    suspend fun registerUser(user:User): Flow<ApiResponse<String>>
    suspend fun authenticateUser(user:User): Flow<ApiResponse<String>>
    suspend fun changePassword(user:User,newPassword:String): Flow<ApiResponse<String>>
    suspend fun validateToken(token:String) :Flow<ApiResponse<User>>
}
package com.ktor.domain.repository

import com.ktor.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ValidateTokenRepository {
    suspend fun validateToken(token:String) : Flow<User>
}
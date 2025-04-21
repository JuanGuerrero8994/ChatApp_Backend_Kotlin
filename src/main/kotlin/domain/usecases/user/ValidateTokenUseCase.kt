package com.ktor.domain.usecases.user

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.domain.model.User
import com.ktor.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class ValidateTokenUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(token: String): Flow<ApiResponse<User>> = repository.validateToken(token)
}
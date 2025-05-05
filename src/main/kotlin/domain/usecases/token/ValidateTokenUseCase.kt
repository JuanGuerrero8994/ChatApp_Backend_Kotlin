package com.ktor.domain.usecases.token

import com.ktor.domain.model.User
import com.ktor.domain.repository.ValidateTokenRepository
import kotlinx.coroutines.flow.Flow

class ValidateTokenUseCase(private val repository: ValidateTokenRepository) {
    suspend operator fun invoke(token: String): Flow<User> = repository.validateToken(token)
}
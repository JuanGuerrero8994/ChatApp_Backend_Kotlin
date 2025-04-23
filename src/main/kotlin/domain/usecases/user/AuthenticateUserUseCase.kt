package com.ktor.domain.usecases.user

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.domain.model.User
import com.ktor.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class AuthenticateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: User, newPassword: String? = null, action: AuthAction) = when (action) {
        AuthAction.LOGIN -> repository.authenticateUser(user)
        AuthAction.REGISTER -> repository.registerUser(user)
        AuthAction.FORGET_PASSWORD -> repository.changePassword(user, newPassword!!)
    }
}
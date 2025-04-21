package com.ktor.domain.usecases.user


import com.ktor.core.ApiResponse
import com.ktor.domain.model.User
import com.ktor.domain.repository.UserRepository
import com.ktor.core.Resource
import com.ktor.data.model.user.UserRequestDTO
import kotlinx.coroutines.flow.Flow

class RegisterUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user:User): Flow<ApiResponse<User>> = repository.registerUser(user)

}

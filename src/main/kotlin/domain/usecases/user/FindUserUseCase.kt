package com.ktor.domain.usecases.user

import com.ktor.core.Resource
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.data.model.user.UserResponseDTO
import com.ktor.domain.model.User
import com.ktor.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class FindUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user:User): Flow<Resource<User>> = repository.findUser(user)

}
package com.ktor.data.mapper

import com.ktor.data.model.user.UserDto
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.data.model.user.UserResponseDTO
import com.ktor.domain.model.User
import org.bson.types.ObjectId


object UserMapper {

    // Mapea un UserRequestDTO a un User
    fun UserResponseDTO.toDomain(hashedPassword: String): User {
        return User(
            id = this.id,  // Usamos el id ya que es parte de UserResponseDTO
            username = this.username,
            email = this.email,
            passwordHash = hashedPassword  // Este es el hash que obtuviste al momento de autenticación o creación
        )
    }

    // Mapea un User a un UserResponseDTO
    fun toResponse(user: User, token: String? = null): UserResponseDTO {
        return UserResponseDTO(
            id = user.id,  // Asumimos que el id ya es un String
            username = user.username,
            email = user.email,
            passwordHash = user.passwordHash  // Si lo necesitas en la respuesta, mantén este campo
        )
    }
}
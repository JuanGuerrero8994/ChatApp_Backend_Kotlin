package com.ktor.data.mapper

import com.ktor.data.model.user.UserRequestDTO
import com.ktor.data.model.user.UserResponseDTO
import com.ktor.domain.model.User
import org.mindrot.jbcrypt.BCrypt

object UserMapper {

    // Mapea un UserRequestDTO a un User (hasheando la contraseña)
    fun UserRequestDTO.toDomain(): User {
        return User(
            id = "",  // El ID será asignado por MongoDB
            username = this.username,
            email = this.email,
            passwordHash = BCrypt.hashpw(this.password, BCrypt.gensalt()) // 🔒 Hasheamos la contraseña
        )
    }

    // Mapea un User a un UserResponseDTO (sin exponer la contraseña)
    fun User.toResponse(): UserResponseDTO {
        return UserResponseDTO(
            id = this.id,
            username = this.username,
            email = this.email,
            passwordHash = "" // No exponer la contraseña en la respuesta
        )
    }
}

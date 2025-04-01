package com.ktor.data.model.user

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class UserResponseDTO(
    @BsonId @Contextual val id: String,
    val username: String,
    val email: String,  // Agregar el campo email aquí
    val passwordHash: String  // El hash de la contraseña, nunca debe almacenarse en texto claro
)
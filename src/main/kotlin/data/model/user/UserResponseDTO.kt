package com.ktor.data.model.user

import com.ktor.data.model.message.MessageResponseDto
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class UserResponseDTO(
    @BsonId
    @Contextual
    val id: String,
    val username: String,
    val email: String,  // Agregar el campo email aquí
)
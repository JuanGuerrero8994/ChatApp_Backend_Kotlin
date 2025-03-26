package com.ktor.data.model.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponseDto(
    val id: String,       // ID del mensaje generado por MongoDB
    val sender: String,   // Remitente
    val message: String,  // Contenido del mensaje
    val timestamp: Long,  // Momento en que se envió el mensaje
    val fileUrl: String? = null // URL de un archivo adjunto (opcional)
)
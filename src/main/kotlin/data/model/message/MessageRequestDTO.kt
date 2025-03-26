package com.ktor.data.model.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageRequestDto(
    val sender: String,    // Remitente del mensaje
    val message: String,   // Contenido del mensaje
    val timestamp: Long,   // Momento en que se envió el mensaje
    val fileUrl: String? = null // URL de un archivo adjunto (opcional)
)
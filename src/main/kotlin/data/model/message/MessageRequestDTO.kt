package com.ktor.data.model.message

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class MessageRequestDto(
    val sender: String, // Remitente del mensaje
    val chatRoomId: String,       // ID de la sala
    val message: String,   // Contenido del mensaje
    val timestamp: String = getCurrentDate(),   // Momento en que se envió el mensaje
    val fileUrl: String? = null // URL de un archivo adjunto (opcional)
)

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return dateFormat.format(Date())  // Obtiene la fecha actual formateada
}
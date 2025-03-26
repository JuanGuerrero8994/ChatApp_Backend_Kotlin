package com.ktor.data.mapper

import com.ktor.data.model.message.MessageDto
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.domain.model.Message
import org.bson.types.ObjectId


object MessageMapper {

    // 🔹 Convierte MessageDto (DB) → Message (Dominio)
    fun MessageDto.toDomain(): Message {
        return Message(
            id = id.toHexString(),  // Convierte ObjectId a String
            sender = sender,
            message = message,
            timestamp = timestamp,
            fileUrl = fileUrl
        )
    }

    // 🔹 Convierte Message (Dominio) → MessageDto (Para la DB)
    fun Message.toDto(): MessageDto {
        return MessageDto(
            id = ObjectId(id),  // Convierte String a ObjectId
            sender = sender,
            message = message,
            timestamp = timestamp,
            fileUrl = fileUrl
        )
    }

    // 🔹 Convierte MessageRequestDto (Petición API) → Message (Dominio)
    fun MessageRequestDto.toDomain(): Message {
        return Message(
            id = ObjectId().toHexString(),  // Genera un nuevo ID
            sender = sender,
            message = message,
            timestamp = timestamp,
            fileUrl = fileUrl
        )
    }

    // 🔹 Convierte Message (Dominio) → MessageResponseDto (Para API)
    fun Message.toResponseDto(): MessageResponseDto {
        return MessageResponseDto(
            id = id,
            sender = sender,
            message = message,
            timestamp = timestamp,
            fileUrl = fileUrl
        )
    }

    // 🔹 Convierte Lista de Message → Lista de MessageResponseDto
    fun toResponseDtoList(messages: List<Message>): List<MessageResponseDto> {
        return messages.map { it.toResponseDto() }
    }
}

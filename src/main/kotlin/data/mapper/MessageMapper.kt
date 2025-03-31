package com.ktor.data.mapper

import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.domain.model.Message
import org.bson.types.ObjectId
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.util.idValue


object MessageMapper {

    // 🔹 Convierte MessageRequestDto (Petición API) → Message (Dominio)
    fun MessageRequestDto.toDomain(): Message {
        return Message(
            id = ObjectId().toHexString(),
            sender = sender,
            message = message,
            timestamp = timestamp,
            fileUrl = fileUrl
        )
    }


    // 🔹 Convierte Message (Dominio) → MessageResponseDto (Para API)
    fun List<MessageResponseDto>.toDomainList(): List<Message> {
        return this.map { it.toDomain() }
    }


}

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
            chatRoomId = chatRoomId ,
            message = message,
            timestamp = timestamp,
            fileUrl = fileUrl
        )
    }
    fun MessageResponseDto.toDomain(): Message {
        return Message(
            id = id,
            sender = sender,
            chatRoomId=chatRoomId,
            message = message,
            timestamp = timestamp,
            fileUrl = fileUrl
        )
    }

}

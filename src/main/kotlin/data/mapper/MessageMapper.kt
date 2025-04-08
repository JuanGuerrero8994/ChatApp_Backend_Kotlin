package com.ktor.data.mapper

import com.ktor.data.mapper.MessageMapper.toMessage
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.data.model.user.UserResponseDTO
import com.ktor.domain.model.Message
import com.ktor.domain.model.User
import org.bson.Document
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
            fileId = fileId
        )
    }

    fun MessageResponseDto.toDomain(): Message {
        return Message(
            id = id,
            sender = sender,
            message = message,
            timestamp = timestamp,
            fileId = fileId
        )
    }

    fun Document.toMessage(): Message = Message(
        id = this.getObjectId("_id").toString(),
        sender = this.getString("sender"),
        message = this.getString("message"),
        timestamp = this.getLong("timestamp"),
        fileId = this.getString("fileUrl")

    )




}
fun Message.toMessageResponseDTO(): MessageResponseDto = MessageResponseDto(
    id = this.id ?: "",
    sender = this.sender ?: "",
    message = this.message ?: "",
    timestamp = this.timestamp ?: 0,
    fileId = this.fileId ?: ""

)

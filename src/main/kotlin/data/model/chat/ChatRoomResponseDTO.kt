package com.ktor.data.model.chat

import com.ktor.data.model.user.UserResponseDTO
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class ChatRoomResponseDto(
    @BsonId @Contextual
    val id: String,
    val name: String,
    val users: List<String>,
    val createdAt:Long
)
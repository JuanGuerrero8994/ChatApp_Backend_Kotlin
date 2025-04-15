package com.ktor.data.model.message

import com.ktor.domain.model.Message
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


@Serializable
data class MessageResponseDto(
    @BsonId
    @Contextual
    val id: String,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val fileId: String? = null,
    val chatRoomId:String?=null,
)
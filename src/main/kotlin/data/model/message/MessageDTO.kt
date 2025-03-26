package com.ktor.data.model.message

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MessageDto(
    @BsonId @Contextual val id: ObjectId = ObjectId(),  // Usa @Contextual para ObjectId
    val sender: String,
    val message: String,
    val timestamp: Long,
    val fileUrl: String? = null
)
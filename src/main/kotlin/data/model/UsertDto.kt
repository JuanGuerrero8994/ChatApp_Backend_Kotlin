package com.ktor.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class UserDto(
    @BsonId @Contextual val id: ObjectId = ObjectId(),  // Usa ObjectId en la capa de data
    val username: String,
    val passwordHash: String
)
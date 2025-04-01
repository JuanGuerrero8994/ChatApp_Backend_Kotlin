package com.ktor.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class Message(
    val id:String,
    val sender: String? = null,
    val message: String? = null,
    val timestamp: String? = null,
    val fileUrl: String? = null
)
package com.ktor.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

data class Message(
    val id: String? = null,
    val sender: String? = null,
    val message: String? = null,
    val timestamp: Long? = null,
    val fileUrl: String? = null
)
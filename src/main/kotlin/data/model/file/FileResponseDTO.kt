package com.ktor.data.model.file

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId


@Serializable
data class FileResponseDTO(
    @BsonId
    @Contextual
    val id: String,
    val name: String,
    val contentType: String
)

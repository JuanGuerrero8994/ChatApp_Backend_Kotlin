package com.ktor.data.model.file

import kotlinx.serialization.Serializable


@Serializable
data class FileRequestDTO(
    var name: String,
    var contentType: String,
    var bytesBase64: String
)
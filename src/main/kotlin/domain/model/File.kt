package com.ktor.domain.model

data class File(
    val id: String? = null,
    val name: String,
    val contentType: String,
    val bytes: ByteArray
)
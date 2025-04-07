package com.ktor.data.model.message

import kotlinx.serialization.Serializable


@Serializable
data class MessageRequestDto(
    val sender: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fileUrl: String? = null
)

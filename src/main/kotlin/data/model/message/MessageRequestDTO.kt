package com.ktor.data.model.message

import kotlinx.serialization.Serializable


@Serializable
data class MessageRequestDto(
    val sender: String,
    var message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fileId: String? = null,
    val chatRoomId:String?=null,
)

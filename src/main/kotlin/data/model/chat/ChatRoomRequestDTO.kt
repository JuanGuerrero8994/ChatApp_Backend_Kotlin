package com.ktor.data.model.chat

import com.ktor.data.model.user.UserRequestDTO
import kotlinx.serialization.Serializable

@Serializable
data class ChatRoomRequestDto(
    val id:String?= null,
    val name: String? = null,
    val users: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)
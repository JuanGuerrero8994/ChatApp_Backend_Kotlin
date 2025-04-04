package com.ktor.data.model.chat

import com.ktor.data.model.user.UserRequestDTO
import kotlinx.serialization.Serializable

@Serializable
data class ChatRoomRequestDto(
    val name: String,
    val users: List<UserRequestDTO>
)
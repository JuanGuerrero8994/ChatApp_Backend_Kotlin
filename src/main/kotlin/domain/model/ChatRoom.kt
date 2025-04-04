package com.ktor.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class ChatRoom(
    val id: String,
    val name: String,
    val users: List<User>,
)
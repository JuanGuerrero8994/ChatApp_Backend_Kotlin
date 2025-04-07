package com.ktor.domain.model


data class ChatRoom(
    val id: String,
    val name: String,
    val users: List<User>,
)
package com.ktor.domain.model


data class ChatRoom(
    val id: String,
    val users: List<String>? = null,
    val name: String? = null,
    val createdAt: Long? = System.currentTimeMillis()
)
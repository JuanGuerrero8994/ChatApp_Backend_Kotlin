package com.ktor.domain.model


data class ChatRoom(
    val id: String,
    val users: List<String>, // lista de user IDs
    val name: String? = null, // solo en caso de grupos
    val createdAt: Long = System.currentTimeMillis()
)
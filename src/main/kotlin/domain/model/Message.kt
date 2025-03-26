package com.ktor.domain.model



data class Message(
    val id: String,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val fileUrl: String? = null
)
package com.ktor.domain.model


data class Message(
    val id: String? = null,
    val sender: String? = null,
    val message: String? = null,
    val timestamp: Long? = null,
    val fileId: String? = null,
    val chatRoomId:String?=null
)
package com.ktor.domain.model



data class User(
    val id: String,
    val username: String,
    val passwordHash: String
)
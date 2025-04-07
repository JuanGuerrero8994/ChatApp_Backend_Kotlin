package com.ktor.domain.model



data class User(
    val id: String? = null,
    val username: String?= null,
    val email: String?= null,
    val password: String? = null
)
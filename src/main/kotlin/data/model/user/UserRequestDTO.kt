package com.ktor.data.model.user

import kotlinx.serialization.Serializable

@Serializable
data class UserRequestDTO(
    val username: String,
    val email: String,
    val password: String
)
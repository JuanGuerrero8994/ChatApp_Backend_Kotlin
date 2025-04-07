package com.ktor.data.model.user

import kotlinx.serialization.Serializable

@Serializable
data class UserRequestDTO(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null
)
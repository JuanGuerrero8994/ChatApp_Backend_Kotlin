package com.ktor.plugins

import com.ktor.core.Resource
import com.ktor.domain.model.User
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import io.ktor.server.application.*
import kotlinx.coroutines.flow.last


suspend fun ApplicationCall.getAuthenticatedUser(validateTokenUseCase: ValidateTokenUseCase): User? {
    val authHeader = request.headers["Authorization"] ?: return null
    if (!authHeader.startsWith("Bearer ")) return null

    val token = authHeader.removePrefix("Bearer ")
    return when (val response = validateTokenUseCase(token).last()) {
        is Resource.Success -> response.data
        else -> null
    }
}
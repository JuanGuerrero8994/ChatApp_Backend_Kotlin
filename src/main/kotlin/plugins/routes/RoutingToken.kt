package com.ktor.plugins.routes

import com.ktor.core.Resource
import com.ktor.domain.model.User
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import io.ktor.server.application.*
import kotlinx.coroutines.flow.last


suspend fun ApplicationCall.getAuthenticatedUser(validateTokenUseCase: ValidateTokenUseCase): User? {
    val authHeader = request.headers["Authorization"]
    val token = when {
        !authHeader.isNullOrBlank() && authHeader.startsWith("Bearer ") -> authHeader.removePrefix("Bearer ")
        else -> request.queryParameters["token"]?.removePrefix("Bearer ")
    } ?: return null

    val response = validateTokenUseCase(token).last()

    return if (response.status == "success" && response.data != null) {
        response.data
    } else {
        null
    }
}
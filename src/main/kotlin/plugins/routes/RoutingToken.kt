package com.ktor.plugins.routes

import com.ktor.core.Resource
import com.ktor.domain.model.User
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import io.ktor.server.application.*
import kotlinx.coroutines.flow.last


suspend fun ApplicationCall.getAuthenticatedUser(validateTokenUseCase: ValidateTokenUseCase): User? {
    // 1. Intenta obtener el token del header Authorization
    val authHeader = request.headers["Authorization"]
    val token = when {
        !authHeader.isNullOrBlank() && authHeader.startsWith("Bearer ") -> authHeader.removePrefix("Bearer ")
        else -> request.queryParameters["token"]?.removePrefix("Bearer ") // 2. Si no está, busca en query params
    } ?: return null

    // 3. Valida el token
    return when (val response = validateTokenUseCase(token).last()) {
        is Resource.Success -> response.data
        else -> null
    }
}
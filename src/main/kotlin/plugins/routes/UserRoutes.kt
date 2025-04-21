package com.ktor.plugins.routes

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.domain.usecases.user.AuthenticateUserUseCase
import com.ktor.domain.usecases.user.FindUserUseCase
import com.ktor.domain.usecases.user.RegisterUserUseCase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.last
import toDomain
import toUserResponseDTO

fun Route.userRoutes(
    registerUserUseCase: RegisterUserUseCase,
    findUserUseCase: FindUserUseCase,
    authenticateUserUseCase: AuthenticateUserUseCase
) {
    route("/users") {
        post("/register") {
            val userDTO = call.receive<UserRequestDTO>()
            val domainUser = userDTO.toDomain()

            val result = registerUserUseCase(domainUser).last()

            if (result.status == "success") {
                call.respond(HttpStatusCode.Created, result)
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 500), result)
            }
        }


        post("/authenticate") {
            val userDTO = call.receive<UserRequestDTO>()
            val user = userDTO.toDomain()

            val result = authenticateUserUseCase(user).last()

            if (result.status == "success") {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 401), result)
            }
        }

        get("/{username}") {
            val userDTO = call.receive<UserRequestDTO>()
            val domainUser = userDTO.toDomain()

            if (userDTO.username!!.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(
                    data = null,
                    messages = listOf("Username text cannot be empty."),
                    status = "error",
                    code = 400
                ))
                return@get
            }

            val result = findUserUseCase(domainUser).last()

            if (result.status == "success") {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 500), result)
            }
        }

    }
}
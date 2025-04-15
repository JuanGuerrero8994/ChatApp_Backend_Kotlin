package com.ktor.plugins.routes

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

            when (val result = registerUserUseCase(domainUser).last()) {
                is Resource.Success -> {
                    val response = result.data?.toUserResponseDTO()
                    call.respond(HttpStatusCode.Created, response ?: "User registered")
                }

                is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, result.message ?: "Error")
                else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
            }
        }

        get("/{username}") {

            val userDTO = call.receive<UserRequestDTO>()
            val domainUser = userDTO.toDomain()

            if (userDTO.username!!.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Username text cannot be empty.")
                return@get
            }

            when (val response = findUserUseCase(domainUser).last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: "User not found")
                is Resource.Error -> call.respond(
                    HttpStatusCode.InternalServerError,
                    response.message ?: "Unknown error"
                )

                else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
            }
        }

        post("/authenticate") {
            val userDTO = call.receive<UserRequestDTO>()

            val user = userDTO.toDomain()

            when (val response = authenticateUserUseCase(user).last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: "")
                is Resource.Error -> call.respond(HttpStatusCode.Unauthorized, response.message ?: "")
                else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
            }
        }
    }
}
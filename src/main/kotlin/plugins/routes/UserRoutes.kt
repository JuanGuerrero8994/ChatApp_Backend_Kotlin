package com.ktor.plugins.routes

import com.ktor.core.ApiResponse
import com.ktor.core.Resource
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.domain.model.User
import com.ktor.domain.usecases.user.AuthAction
import com.ktor.domain.usecases.user.AuthenticateUserUseCase
import com.ktor.domain.usecases.user.FindUserUseCase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.last
import toDomain
import toUserResponseDTO

fun Route.userRoutes(
    findUserUseCase: FindUserUseCase,
    authenticateUserUseCase: AuthenticateUserUseCase,
) {
    route("/users") {
        post("/register") {
            val userDTO = call.receive<UserRequestDTO>()
            val user = userDTO.toDomain()

            val result = authenticateUserUseCase.invoke(user = user, action = AuthAction.REGISTER).last()

            if (result.status == "Success") {
                call.respond(HttpStatusCode.Created, result)
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 500), result)
            }
        }


        post("/authenticate") {
            val userDTO = call.receive<UserRequestDTO>()
            val user = userDTO.toDomain()

            val result = authenticateUserUseCase.invoke(user = user, action = AuthAction.LOGIN).last()

            if (result.status == "Success") {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 401), result)
            }
        }

        get("/{username}") {
            val userDTO = call.receive<UserRequestDTO>()
            val domainUser = userDTO.toDomain()

            if (userDTO.username!!.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest, ApiResponse<Unit>(
                        data = null,
                        messages = listOf("Username text cannot be empty."),
                        status = "Error",
                        code = 400
                    )
                )
                return@get
            }

            val result = findUserUseCase(domainUser).last()

            if (result.status == "Success") {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 500), result)
            }
        }

        post("/change-password") {

            val userDTO = call.receive<UserRequestDTO>()
            val user = userDTO.toDomain()

            val result = authenticateUserUseCase.invoke(user, userDTO.newPassword, action = AuthAction.FORGET_PASSWORD).last()

            if (userDTO.email!!.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest, ApiResponse<Unit>(
                        messages = listOf("email text cannot be empty."),
                        status = "Error",
                        code = 400
                    )
                )
                return@post
            }
            if (userDTO.password!!.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest, ApiResponse<Unit>(
                        messages = listOf("Password text cannot be empty."),
                        status = "Error",
                        code = 400
                    )
                )
                return@post
            }

            if (userDTO.password.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest, ApiResponse<Unit>(
                        messages = listOf("Password text cannot be empty."),
                        status = "Error",
                        code = 400
                    )
                )
                return@post
            }

            if (userDTO.newPassword!!.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest, ApiResponse<Unit>(
                        messages = listOf("New password text cannot be empty."),
                        status = "Error",
                        code = 400
                    )
                )
                return@post
            }


            when (result.status) {
                "Success" -> {
                    call.respond(HttpStatusCode.OK, result)
                }
                "Error" -> {
                    call.respond(HttpStatusCode.fromValue(result.code ?: 500),result)
                }
                else -> {
                    call.respond(HttpStatusCode.fromValue(result.code ?: 500), result)
                }
            }
        }

    }
}
package com.ktor.plugins

import com.ktor.core.Resource
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.domain.model.Message
import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.user.AuthenticateUserUseCase
import com.ktor.domain.usecases.user.FindUserUseCase
import com.ktor.domain.usecases.user.RegisterUserUseCase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.last
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    val registerUserUseCase:RegisterUserUseCase by inject()
    val findUserUseCase:FindUserUseCase by inject()
    val authenticateUserUseCase:AuthenticateUserUseCase by inject()

    val getAllMessagesUseCase: GetAllMessagesUseCase by inject()
    val sendMessageUseCase: SendMessageUseCase by inject()

    routing {

        route("/users") {
            post("/register") {
                val userRequest = call.receive<UserRequestDTO>()
                when (val response = registerUserUseCase(userRequest).last()) {
                    is Resource.Success -> call.respond(HttpStatusCode.Created, response.data ?: "User registered successfully")
                    is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, response.message ?: "Unknown error")
                    else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
                }
            }

            get("/{username}") {
                val username = call.parameters["username"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Username required")
                when (val response = findUserUseCase(username).last()) {
                    is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: "User not found")
                    is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, response.message ?: "Unknown error")
                    else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
                }
            }

            post("/authenticate") {
                val credentials = call.receive<Map<String, String>>()
                val username = credentials["username"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Username required")
                val password = credentials["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Password required")

                when (val response = authenticateUserUseCase(username, password).last()) {
                    is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: "Invalid credentials")
                    is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, response.message ?: "Unknown error")
                    else -> call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                }
            }
        }

        route("/messages") {
            get {

                val response = getAllMessagesUseCase()
                    .last() // 🟢 Espera hasta que el flujo termine y obtiene el último valor emitido

                when (response) {
                    is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: emptyList<Message>())
                    is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, response.message ?: "Unknown error")
                    else -> call.respond(HttpStatusCode.OK, emptyList<Message>()) // Si hay un estado inesperado
                }
            }

            post {
                val newMessage = call.receive<MessageRequestDto>()

                if (newMessage.message.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Message text cannot be empty.")
                    return@post
                }

                sendMessageUseCase(newMessage)
                call.respond(HttpStatusCode.Created, "Message created successfully")
            }
        }
    }
}
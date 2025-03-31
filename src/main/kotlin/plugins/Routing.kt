package com.ktor.plugins

import com.ktor.core.Resource
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.model.Message
import com.ktor.domain.usecases.GetAllMessagesUseCase
import com.ktor.domain.usecases.SendMessageUseCase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.last
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val getAllMessagesUseCase: GetAllMessagesUseCase by inject()
    val sendMessageUseCase: SendMessageUseCase by inject()

    routing {
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
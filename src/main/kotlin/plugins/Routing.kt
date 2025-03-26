package com.ktor.plugins

import com.ktor.core.Resource
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.usecases.GetAllMessagesUseCase
import com.ktor.domain.usecases.SendMessageUseCase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val getAllMessagesUseCase: GetAllMessagesUseCase by inject()
    val sendMessageUseCase: SendMessageUseCase by inject()

    routing {
        //authenticate("auth-jwt") {
        route("/messages") {
            get {
                when (val response = getAllMessagesUseCase().first()) { // Obtener los mensajes
                    is Resource.Success -> {
                        call.respond(HttpStatusCode.OK, response)
                    }

                    is Resource.Error ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            response.message ?: "Error desconocido"
                        )

                    is Resource.Loading -> call.respond(HttpStatusCode.Accepted) // Indicar que aún está cargando
                }
            }

            post {
                val newMessage = call.receive<MessageRequestDto>()

                // Validate the incoming message
                if (newMessage.message.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Message text cannot be empty.")
                    return@post
                }

                sendMessageUseCase(newMessage)
                call.respond(HttpStatusCode.Created, "Message created successfully")
            }
        }
    }
    //  }
}

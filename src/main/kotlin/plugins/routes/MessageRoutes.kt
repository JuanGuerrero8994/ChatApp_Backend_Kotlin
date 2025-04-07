package com.ktor.plugins.routes


import com.ktor.core.Resource
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.model.Message
import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.last

fun Route.messagesRoutes(
    validateTokenUseCase: ValidateTokenUseCase,
    sendMessageUseCase: SendMessageUseCase,
    getAllMessagesUseCase: GetAllMessagesUseCase,
) {

    route("/messages") {
        get {
            val user = call.getAuthenticatedUser(validateTokenUseCase)
            
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
                return@get
            }

            when (val response = getAllMessagesUseCase().last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: emptyList<Message>())
                is Resource.Error -> call.respond(
                    HttpStatusCode.InternalServerError,
                    response.message ?: "Unknown error"
                )

                else -> call.respond(HttpStatusCode.OK, emptyList<Message>())
            }
        }

        post {
            val user = call.getAuthenticatedUser(validateTokenUseCase)

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
                return@post
            }

            val newMessage = call.receive<MessageRequestDto>()

            when (val response = sendMessageUseCase(newMessage).last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: emptyList<Message>())
                is Resource.Error -> call.respond(
                    HttpStatusCode.InternalServerError,
                    response.message ?: "Unknown error"
                )

                else -> call.respond(HttpStatusCode.OK, emptyList<Message>())
            }
        }


    }
}
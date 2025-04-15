package com.ktor.plugins.routes


import com.ktor.core.Resource
import com.ktor.data.mapper.toDomain
import com.ktor.data.mapper.toMessageResponseDTO
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.model.Message
import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.message.GetMessagesByChatRoomIdUseCase
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
    getMessagesByChatRoomIdUseCase: GetMessagesByChatRoomIdUseCase
) {

    route("/messages") {
        get {
            val user = call.getAuthenticatedUser(validateTokenUseCase)

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
                return@get
            }

            when (val result = getAllMessagesUseCase().last()) {
                is Resource.Success -> {
                    val response = result.data?.toList()?.map { it.toMessageResponseDTO() }
                    call.respond(HttpStatusCode.OK, response ?: emptyList<Message>())
                }

                is Resource.Error -> call.respond(
                    HttpStatusCode.InternalServerError,
                    result.message ?: "Unknown error"
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

            val messageDTO = call.receive<MessageRequestDto>()

            if (messageDTO.message.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Message text cannot be empty.")
                return@post
            }


            val message = messageDTO.toDomain()

            when (val response = sendMessageUseCase(message).last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: emptyList<Message>())
                is Resource.Error -> call.respond(
                    HttpStatusCode.InternalServerError,
                    response.message ?: "Unknown error"
                )

                else -> call.respond(HttpStatusCode.OK, emptyList<Message>())
            }
        }

        // 📥 Obtener mensajes de una sala específica
        get("{id}/messages") {
            val chatRoomId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Falta el ID de la sala")

            when (val result = getMessagesByChatRoomIdUseCase(chatRoomId).last()) {
                is Resource.Success -> {
                    val response = result.data?.map { it.toMessageResponseDTO() } ?: emptyList()
                    call.respond(HttpStatusCode.OK, response)
                }
                is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, result.message ?: "Error al obtener los mensajes")
                else -> call.respond(HttpStatusCode.InternalServerError, "")
            }
        }

    }
}
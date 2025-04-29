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
    sendMessageUseCase: SendMessageUseCase,
    getAllMessagesUseCase: GetAllMessagesUseCase,
    getMessagesByChatRoomIdUseCase: GetMessagesByChatRoomIdUseCase
) {

    route("/messages") {
        get {
            val result = getAllMessagesUseCase().last()
            if (result.status == "Success") {
                call.respond(HttpStatusCode.OK, result.data?.map {  it.toMessageResponseDTO() } ?: emptyList())
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 500), result.messages)
            }

        }

        post {
            val messageDTO = call.receive<MessageRequestDto>()

            if (messageDTO.message.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Message text cannot be empty.")
                return@post
            }


            val message = messageDTO.toDomain()

            val result = sendMessageUseCase(message).last()
            if (result.status == "Success") {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 500), result.messages)
            }
        }

        // 📥 Obtener mensajes de una sala específica
        get("{id}/messages") {
            val chatRoomId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Falta el ID de la sala")

            val result = getMessagesByChatRoomIdUseCase(chatRoomId).last()
            if (result.status == "Success") {
                call.respond(HttpStatusCode.OK, result.data?.map {  it.toMessageResponseDTO() } ?: emptyList())
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 500), result.messages)
            }
        }

    }
}
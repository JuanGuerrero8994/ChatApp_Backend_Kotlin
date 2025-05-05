package com.ktor.plugins.routes


import com.ktor.data.mapper.toDomain
import com.ktor.data.mapper.toMessageResponseDTO
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.domain.usecases.chat.GetChatRoomByIdUseCase
import com.ktor.domain.usecases.message.MessageAction
import com.ktor.domain.usecases.message.MessageUseCases
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.last

fun Route.messagesRoutes(
    messageUseCases: MessageUseCases
) {

    route("/messages") {
        get {
            val result = messageUseCases.invoke(action = MessageAction.GET_ALL_MESSAGES).last()
            if (result.status == "Success") {
                call.respond(HttpStatusCode.OK, result.messages)
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

            val result = messageUseCases.invoke(message, action = MessageAction.SEND_MESSAGE).last()
            if (result.status == "Success") {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 500), result.messages)
            }
        }
        get("{roomId}") {
            val roomId = call.parameters["roomId"]

            if (roomId.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing roomId")
                return@get
            }

            val result = messageUseCases.invoke(roomId = roomId, action = MessageAction.GET_MESSAGES_BY_CHAT_ROOM_ID).last()

            if (result.status == "Success") {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.fromValue(result.code ?: 500), result.messages)
            }
        }


    }
}
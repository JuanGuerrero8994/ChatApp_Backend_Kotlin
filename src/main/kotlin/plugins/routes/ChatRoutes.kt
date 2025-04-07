package com.ktor.plugins.routes

import com.ktor.core.Resource
import com.ktor.data.model.chat.ChatRoomRequestDto
import com.ktor.domain.model.ChatRoom
import com.ktor.domain.usecases.chat.CreateChatRoomUseCase
import com.ktor.domain.usecases.chat.GetAllChatRoomUseCase
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.last

fun Route.chatRoutes(
    validateTokenUseCase: ValidateTokenUseCase,
    createChatRoomUseCase: CreateChatRoomUseCase,
    getAllChatRoomUseCase: GetAllChatRoomUseCase
) {
    val chatRooms = mutableMapOf<String, MutableList<DefaultWebSocketServerSession>>()

    route("/chat") {

        post {
            val user = call.getAuthenticatedUser(validateTokenUseCase)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid or missing token")
                return@post
            }

            val chatRoomRequest = call.receive<ChatRoomRequestDto>()

            println("Prueba ${chatRoomRequest}")

            when (val response = createChatRoomUseCase(chatRoomRequest).last()) {
                is Resource.Success -> call.respond(
                    HttpStatusCode.Created,
                    response.data ?: "Chat room created"
                )

                is Resource.Error -> call.respond(
                    HttpStatusCode.InternalServerError,
                    response.message ?: "Unknown error"
                )

                else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
            }
        }

        get {
            when (val response = getAllChatRoomUseCase().last()) {
                is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: emptyList<ChatRoom>())
                is Resource.Error -> call.respond(
                    HttpStatusCode.InternalServerError,
                    response.message ?: "Unknown error"
                )

                else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
            }
        }
    }


    webSocket("/chat/{room}") {
        val room = call.parameters["room"]
        if (room.isNullOrBlank()) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room ID required"))
            return@webSocket
        }

        val user = call.getAuthenticatedUser(validateTokenUseCase)
        if (user == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
            return@webSocket
        }

        println("Usuario ${user.username} conectado a la sala: $room")

        val roomConnections = chatRooms.getOrPut(room) { mutableListOf() }
        roomConnections.add(this)

        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val receivedMessage = frame.readText()
                        println("[Sala $room] ${user.username}: $receivedMessage")

                        // Enviar mensaje solo a los usuarios de la misma sala
                        roomConnections.forEach {
                            try {
                                it.send(Frame.Text("[Sala $room] ${user.username}: $receivedMessage"))
                            } catch (e: Exception) {
                                println("Error enviando mensaje: ${e.message}")
                            }
                        }
                    }

                    else -> println("Tipo de mensaje no soportado")
                }
            }
        } catch (e: Exception) {
            println("Error en WebSocket en sala $room: ${e.message}")
        } finally {
            roomConnections.remove(this)
            println("Usuario ${user.username} salió de la sala: $room")

            // Eliminar la sala si no hay más usuarios
            if (roomConnections.isEmpty()) {
                chatRooms.remove(room)
            }
        }
    }
}




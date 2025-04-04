package com.ktor.plugins

import com.ktor.core.Resource
import com.ktor.data.model.chat.ChatRoomRequestDto
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.domain.model.ChatRoom
import com.ktor.domain.model.Message
import com.ktor.domain.model.User
import com.ktor.domain.usecases.chat.CreateChatRoomUseCase
import com.ktor.domain.usecases.chat.GetAllChatRoomUseCase
import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.user.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    val registerUserUseCase: RegisterUserUseCase by inject()
    val findUserUseCase: FindUserUseCase by inject()
    val authenticateUserUseCase: AuthenticateUserUseCase by inject()
    val validateTokenUseCase: ValidateTokenUseCase by inject()


    val getAllMessagesUseCase: GetAllMessagesUseCase by inject()
    val sendMessageUseCase: SendMessageUseCase by inject()

    val createChatRoomUseCase: CreateChatRoomUseCase by inject()
    val getAllChatRoomsUseCase: GetAllChatRoomUseCase by inject()

    val connections = mutableListOf<DefaultWebSocketServerSession>()

    val chatRooms = mutableMapOf<String, MutableList<DefaultWebSocketServerSession>>()


    routing {

        /**
         * Authentication
         * */

        route("/users") {
            post("/register") {
                val userRequest = call.receive<UserRequestDTO>()
                when (val response = registerUserUseCase(userRequest).last()) {
                    is Resource.Success -> call.respond(
                        HttpStatusCode.Created,
                        response.data ?: "User registered successfully"
                    )

                    is Resource.Error -> call.respond(
                        HttpStatusCode.InternalServerError,
                        response.message ?: "Unknown error"
                    )

                    else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
                }
            }

            get("/{username}") {
                val username = call.parameters["username"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Username required"
                )
                when (val response = findUserUseCase(username).last()) {
                    is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: "User not found")
                    is Resource.Error -> call.respond(
                        HttpStatusCode.InternalServerError,
                        response.message ?: "Unknown error"
                    )

                    else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
                }
            }

            post("/authenticate") {
                val credentials = call.receive<Map<String, String>>()
                val username =
                    credentials["username"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Username required")
                val password =
                    credentials["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Password required")

                when (val response = authenticateUserUseCase(username, password).last()) {
                    is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: "Invalid credentials")
                    is Resource.Error -> call.respond(
                        HttpStatusCode.InternalServerError,
                        response.message ?: "Unknown error"
                    )

                    else -> call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                }
            }
        }


        /**
         * MESSAGES
         * */
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
                    is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, response.message ?: "Unknown error")
                    else -> call.respond(HttpStatusCode.OK, emptyList<Message>())
                }
            }



        }

//        webSocket("/chat") {
//
//            val user = call.getAuthenticatedUser(validateTokenUseCase)
//            if (user == null) {
//                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No autorizado"))
//                return@webSocket
//            }
//
//            println("Usuario conectado: ${user.username}")
//            connections.add(this)
//
//            try {
//                for (frame in incoming) {
//                    when (frame) {
//                        is Frame.Text -> {
//                            val receivedMessage = frame.readText()
//                            println("Mensaje de ${user.username}: $receivedMessage")
//
//                            // Enviar el mensaje a todos los clientes conectados
//                            connections.forEach {
//                                try {
//                                    it.send(Frame.Text("${user.username}: $receivedMessage"))
//                                } catch (e: Exception) {
//                                    println("Error enviando mensaje: ${e.message}")
//                                }
//                            }
//                        }
//
//                        else -> println("Tipo de mensaje no soportado")
//                    }
//                }
//            } catch (e: Exception) {
//                println("Error en WebSocket: ${e.message}")
//            } finally {
//                connections.remove(this)
//                println("Usuario desconectado: ${user.username}")
//            }
//        }

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
                    is Resource.Success -> call.respond(HttpStatusCode.Created, response.data ?: "Chat room created")
                    is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, response.message ?: "Unknown error")
                    else -> call.respond(HttpStatusCode.InternalServerError, "Unexpected error")
                }
            }

            get {
                when (val response = getAllChatRoomsUseCase().last()) {
                    is Resource.Success -> call.respond(HttpStatusCode.OK, response.data ?: emptyList<ChatRoom>())
                    is Resource.Error -> call.respond(HttpStatusCode.InternalServerError, response.message ?: "Unknown error")
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

}


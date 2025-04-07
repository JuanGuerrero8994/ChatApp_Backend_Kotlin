package com.ktor.plugins.routes

import com.ktor.domain.usecases.chat.CreateChatRoomUseCase
import com.ktor.domain.usecases.chat.GetAllChatRoomUseCase
import com.ktor.domain.usecases.message.GetAllMessagesUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.user.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
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





    routing {
        userRoutes(registerUserUseCase, findUserUseCase, authenticateUserUseCase)
        messagesRoutes(validateTokenUseCase, sendMessageUseCase, getAllMessagesUseCase)
        chatRoutes(validateTokenUseCase, createChatRoomUseCase, getAllChatRoomsUseCase)

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
    }
}


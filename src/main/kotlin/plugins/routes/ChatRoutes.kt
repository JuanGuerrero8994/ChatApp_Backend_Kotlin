package com.ktor.plugins.routes

import com.ktor.domain.usecases.user.ValidateTokenUseCase
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.CopyOnWriteArrayList

fun Route.chatRoutes(validateTokenUseCase: ValidateTokenUseCase) {

    val connections = CopyOnWriteArrayList<DefaultWebSocketServerSession>()

    webSocket("/chat") {
        val user = call.getAuthenticatedUser(validateTokenUseCase)
        println("Token recibido: ${call.request.headers["Authorization"]}")

        if (user == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No autorizado"))
            return@webSocket
        }

        println("Usuario conectado: ${user.username}")
        connections.add(this)

        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val receivedMessage = frame.readText()
                        println("Mensaje de ${user.username}: $receivedMessage")

                        // Enviar el mensaje a todos los clientes conectados
                        connections.forEach {
                            try {
                                it.send(Frame.Text("${user.username}: $receivedMessage"))
                            } catch (e: Exception) {
                                println("Error enviando mensaje: ${e.message}")
                            }
                        }
                    }

                    else -> println("Tipo de mensaje no soportado")
                }
            }
        } catch (e: Exception) {
            println("Error en WebSocket: ${e.message}")
        } finally {
            connections.remove(this)
            println("Usuario desconectado: ${user.username}")
        }
    }
}
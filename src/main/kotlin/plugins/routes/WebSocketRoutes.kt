package com.ktor.plugins.routes

import com.ktor.data.mapper.MessageMapper.toDomain
import com.ktor.data.mapper.toMessageResponseDTO
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import com.ktor.plugins.ChatConnectionManager
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Route.webSocketRoutes(validateTokenUseCase: ValidateTokenUseCase, chatConnectionManager: ChatConnectionManager) {

    webSocket("/chat") {
        val user = call.getAuthenticatedUser(validateTokenUseCase)


        if (user == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid Token"))
            return@webSocket
        }

        println("✅ Usuario conectado: ${user.username}")
        chatConnectionManager.addConnection(user.username!!, this)

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val incomingText = frame.readText()

                    // 🔹 Convertimos JSON entrante → MessageRequestDto
                    val requestDto = Json.decodeFromString<MessageRequestDto>(incomingText)

                    // 🔹 Convertimos a dominio y asignamos ID generado
                    val domainMessage = requestDto.toDomain()

                    // ✅ Aquí podrías guardar el mensaje en la base de datos si lo necesitás

                    // 🔹 Convertimos a DTO de respuesta
                    val responseDto: MessageResponseDto = domainMessage.toMessageResponseDTO()

                    // 🔹 Serializamos y lo enviamos a todos los clientes conectados
                    val responseJson = Json.encodeToString(responseDto)
                    chatConnectionManager.broadcastRawJson(responseJson)

                    println("📨 Mensaje broadcasted de ${responseDto.sender}: ${responseDto.message}")
                }
            }
        } catch (e: Exception) {
            println("❌ WebSocket error: ${e.localizedMessage}")
        } finally {
            chatConnectionManager.removeConnection(user.username)
            println("👋 Usuario desconectado: ${user.username}")
        }
    }
}


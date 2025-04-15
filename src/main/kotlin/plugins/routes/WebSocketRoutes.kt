import com.ktor.core.Resource
import com.ktor.data.mapper.toDomain
import com.ktor.data.mapper.toMessageResponseDTO
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.domain.usecases.file.GetFileUseCase
import com.ktor.domain.usecases.message.GetMessagesByChatRoomIdUseCase
import com.ktor.domain.usecases.message.SendMessageUseCase
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import com.ktor.plugins.routes.getAuthenticatedUser
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

fun Route.webSocketRoutes(
    validateTokenUseCase: ValidateTokenUseCase,
    sendMessageUseCase: SendMessageUseCase,
    getMessagesByChatRoomIdUseCase: GetMessagesByChatRoomIdUseCase,
    getFileUseCase: GetFileUseCase,
    chatConnectionManager: ChatConnectionManager,
) {
    route("/chat/{roomId}") {
        webSocket {
            println("📡 WebSocket iniciado")

            val roomId = call.parameters["roomId"]
            if (roomId.isNullOrBlank()) {
                closeWithReason("Missing roomId")
                return@webSocket
            }

            val user = call.getAuthenticatedUser(validateTokenUseCase)
            if (user == null || user.username.isNullOrBlank()) {
                closeWithReason("Invalid Token")
                return@webSocket
            }

            println("✅ Usuario autenticado: ${user.username}")
            chatConnectionManager.addConnection(roomId, user.username, this)

            sendPreviousMessages(roomId, getMessagesByChatRoomIdUseCase)

            println("👥 Usuarios conectados: ${chatConnectionManager.getConnectedUsers(roomId)}")

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        processIncomingMessage(
                            frame.readText(),
                            user.username,
                            roomId,
                            getFileUseCase,
                            sendMessageUseCase,
                            chatConnectionManager
                        )
                    }
                }
            } catch (e: Exception) {
                println("❌ Error WebSocket: ${e.localizedMessage}")
            } finally {
                chatConnectionManager.removeConnection(roomId, user.username)
                println("👋 Conexión cerrada para ${user.username}")
            }
        }
    }
}


private suspend fun DefaultWebSocketServerSession.sendPreviousMessages(
    roomId: String,
    getMessagesByChatRoomIdUseCase: GetMessagesByChatRoomIdUseCase
) {
    try {
        val messages = getMessagesByChatRoomIdUseCase(roomId).last()
        if (messages is Resource.Success && !messages.data.isNullOrEmpty()) {
            println("💬 Enviando ${messages.data.size} mensajes previos")
            messages.data.forEach { message ->
                val json = Json.encodeToString(
                    MessageResponseDto.serializer(),
                    message.toMessageResponseDTO()
                )
                send(Frame.Text(json))
            }
        } else {
            println("⚠️ No se encontraron mensajes previos o hubo error: ${(messages as? Resource.Error)?.message}")
        }
    } catch (e: Exception) {
        println("❌ Error al cargar mensajes previos: ${e.localizedMessage}")
    }
}

private suspend fun DefaultWebSocketServerSession.closeWithReason(reason: String) {
    println("❌ $reason")
    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, reason))
}

private fun CoroutineScope.processIncomingMessage(
    messageText: String,
    username: String,
    roomId: String,
    getFileUseCase: GetFileUseCase,
    sendMessageUseCase: SendMessageUseCase,
    chatConnectionManager: ChatConnectionManager
) {
    val messageRequest = try {
        Json.decodeFromString<MessageRequestDto>(messageText)
    } catch (e: Exception) {
        println("❌ Error al deserializar el mensaje: ${e.localizedMessage}")
        return
    }

    val domainMessage = messageRequest.toDomain().copy(sender = username)

    launch {
        messageRequest.fileId?.let { fileId ->
            val fileResult = getFileUseCase(fileId).last()
            if (fileResult is Resource.Success) {
                println("📁 Archivo recibido: ${fileResult.data?.name}")
            } else {
                println("⚠️ Error obteniendo archivo con id $fileId")
            }
        }

        val result = sendMessageUseCase(domainMessage).last()
        if (result is Resource.Success<*>) {
            println("💾 Mensaje guardado")
            val responseJson = Json.encodeToString(
                MessageResponseDto.serializer(),
                domainMessage.toMessageResponseDTO()
            )
            chatConnectionManager.broadcastToRoom(roomId, responseJson)
        } else {
            println("⚠️ No se pudo guardar el mensaje: ${result.message ?: "Desconocido"}")
        }
    }
}

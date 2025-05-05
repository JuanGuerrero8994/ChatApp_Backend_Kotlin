import com.ktor.core.Resource
import com.ktor.data.mapper.toDomain
import com.ktor.data.mapper.toMessageResponseDTO
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.domain.model.Message
import com.ktor.domain.usecases.file.GetFileUseCase
import com.ktor.domain.usecases.message.MessageAction
import com.ktor.domain.usecases.message.MessageUseCases
import com.ktor.domain.usecases.token.ValidateTokenUseCase
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
    messageUseCases: MessageUseCases,
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

            val token = call.request.queryParameters["token"]


            val result = validateTokenUseCase(token!!).last()

            if (result != null) {
                val user = result

                println("✅ Usuario autenticado: ${user.username}")
                chatConnectionManager.addConnection(roomId, user.username!!, this)

                sendPreviousMessages(roomId, messageUseCases)

                println("👥 Usuarios conectados: ${chatConnectionManager.getConnectedUsers(roomId)}")

                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            processIncomingMessage(
                                frame.readText(),
                                user.username,
                                roomId,
                                getFileUseCase,
                                messageUseCases,
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
            } else {
                closeWithReason("Invalid Token")
                return@webSocket
            }
        }
    }
}


private suspend fun DefaultWebSocketServerSession.sendPreviousMessages(
    roomId: String,
    messageUseCases: MessageUseCases,
) {
    try {
        val messages = messageUseCases.invoke(roomId = roomId, action = MessageAction.GET_MESSAGES_BY_CHAT_ROOM_ID).last()

        if ((messages.data as List<Message>).isNotEmpty()) {
            println("💬 Enviando ${messages.data.size} mensajes previos")
            messages.data.forEach { message ->
                val json = Json.encodeToString(
                    MessageResponseDto.serializer(),
                    message.toMessageResponseDTO()
                )
                send(Frame.Text(json))
            }
        } else {
            println("⚠️ No se encontraron mensajes previos o hubo error: ${(messages.messages)}")
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
    messageUseCases: MessageUseCases,
    chatConnectionManager: ChatConnectionManager,
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

        val result = messageUseCases.invoke(message = domainMessage, action = MessageAction.SEND_MESSAGE).last()
        println("💾 Mensaje guardado")
        val responseJson = Json.encodeToString(
            MessageResponseDto.serializer(),
            domainMessage.toMessageResponseDTO()
        )
        chatConnectionManager.broadcastToRoom(roomId, responseJson)
    }
}

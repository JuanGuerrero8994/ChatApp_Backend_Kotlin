import com.ktor.data.mapper.MessageMapper.toDomain
import com.ktor.data.mapper.toMessageResponseDTO
import com.ktor.data.model.message.MessageRequestDto
import com.ktor.data.model.message.MessageResponseDto
import com.ktor.domain.usecases.user.ValidateTokenUseCase
import com.ktor.plugins.routes.getAuthenticatedUser
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json



fun Route.webSocketRoutes(
    validateTokenUseCase: ValidateTokenUseCase,
    chatConnectionManager: ChatConnectionManager,
) {
    route("/chat/{roomId}") {
        webSocket {
            println("📡 WebSocket iniciado")

            val roomId = call.parameters["roomId"]
            if (roomId.isNullOrBlank()) {
                println("❌ roomId faltante")
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing roomId"))
                return@webSocket
            }
            println("✅ roomId válido: $roomId")

            val user = call.getAuthenticatedUser(validateTokenUseCase)


            if (user == null || user.username.isNullOrBlank()) {
                println("❌ Token inválido o usuario sin nombre")
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid Token"))
                return@webSocket
            }
            println("✅ Token válido")
            println("👤 Usuario autenticado: ${user.username}")

            chatConnectionManager.addConnection(roomId, user.username, this)

            val connectedUsers = chatConnectionManager.getConnectedUsers(roomId)
            println("👥 Usuarios conectados en la sala: $connectedUsers")

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val incomingText = frame.readText()
                        println("📨 Mensaje recibido: $incomingText")

                        val messageRequestDto = Json.decodeFromString<MessageRequestDto>(incomingText)
                        val domainMessage = messageRequestDto.toDomain()
                        val responseDto = domainMessage.toMessageResponseDTO()
                        val responseJson = Json.encodeToString(MessageResponseDto.serializer(), responseDto)

                        chatConnectionManager.broadcastToRoom(roomId, responseJson)
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


import io.ktor.websocket.*

class ChatConnectionManager {

    private val roomConnections = mutableMapOf<String, MutableMap<String, WebSocketSession>>()

    fun addConnection(roomId: String, username: String, session: WebSocketSession) {
        val room = roomConnections.getOrPut(roomId) { mutableMapOf() }
        room[username] = session
        println("✅ [$roomId] $username se unió a la sala")
    }

    fun removeConnection(roomId: String, username: String) {
        roomConnections[roomId]?.remove(username)
        println("👋 [$roomId] $username salió de la sala")

        if (roomConnections[roomId]?.isEmpty() == true) {
            roomConnections.remove(roomId)
        }
    }


    suspend fun broadcastToRoom(roomId: String, message: String) {
        roomConnections[roomId]?.values?.forEach { session ->
            session.send(Frame.Text(message))
        }
        println("📤 [$roomId] Mensaje enviado a ${roomConnections[roomId]?.size ?: 0} usuario(s)")
    }
    // 🔍 NUEVO: obtener usuarios conectados en un room
    fun getConnectedUsers(roomId: String): List<String> {
        return roomConnections[roomId]?.keys?.toList() ?: emptyList()
    }
}

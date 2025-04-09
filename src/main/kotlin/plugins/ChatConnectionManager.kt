package com.ktor.plugins

import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

class ChatConnectionManager {

    private val connections = ConcurrentHashMap<String, WebSocketSession>()

    fun addConnection(username: String, session: WebSocketSession) {
        connections[username] = session
    }

    fun removeConnection(username: String) {
        connections.remove(username)
    }

    suspend fun broadcastMessage(sender: String, message: String) {
        val textFrame = Frame.Text("$sender: $message")
        connections.forEach { (user, session) ->
            session.send(textFrame)
        }
    }

    suspend fun broadcastRawJson(messageJson: String) {
        val textFrame = Frame.Text(messageJson)
        connections.forEach { (_, session) ->
            session.send(textFrame)
        }
    }
}

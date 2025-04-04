package com.ktor.data.mapper

import UserMapper.toUserDomain
import com.ktor.data.model.chat.ChatRoomRequestDto
import com.ktor.domain.model.ChatRoom
import com.ktor.domain.model.Message
import com.ktor.domain.model.User
import org.bson.Document
import org.bson.types.ObjectId

object ChatRoomMapper {

    // 🔹 Convierte ChatRoomRequestDto → ChatRoom (Dominio)
    fun ChatRoomRequestDto.toDomain(): ChatRoom {
        return ChatRoom(
            id = ObjectId().toHexString(),
            name = this.name,
            users = this.users.map { userDto ->
                User(
                    id = userDto.id,
                    username = userDto.username,
                    email = userDto.email,
                )
            }
        )
    }

    // 🔹 Convierte ChatRoom (Dominio) → Document (MongoDB)
    fun Document.toDomain(): ChatRoom {
        return ChatRoom(
            id = this["_id"].toString(),
            name = this["name"] as String,
            users = (this["users"] as? List<Document>)?.map { it.toUserDomain() } ?: emptyList()
        )
    }


    fun ChatRoom.toDocument(): Document {
        val usersList = users.map { user ->
            Document().apply {
                put("id", user.id)
                put("username", user.username)
                put("email", user.email)
            }
        }

        return Document().apply {
            put("_id", ObjectId()) // Mongo lo genera automáticamente, pero podés asignarlo vos si querés usarlo después
            put("name", name)
            put("users", usersList)
        }
    }

}

package com.ktor.data.mapper

import com.ktor.data.model.chat.ChatRoomRequestDto
import com.ktor.data.model.chat.ChatRoomResponseDto
import com.ktor.domain.model.ChatRoom
import org.bson.Document
import org.bson.types.ObjectId


fun ChatRoomRequestDto.toDomain(): ChatRoom = ChatRoom(
    id = ObjectId().toHexString(),
    name = this.name,
    users = this.users,
)

fun ChatRoom.toDocument(): Document = Document().apply {
    append("_id", ObjectId(id))
    append("name", name)
    append("users", users)
    append("createdAt", createdAt)
}

fun Document.toDomain(): ChatRoom = ChatRoom(
    id = getObjectId("_id").toHexString(),
    name = getString("name"),
    users = getList("users", String::class.java),
    createdAt = getLong("createdAt")
)

fun ChatRoom.toResponseDTO(): ChatRoomResponseDto {
    return ChatRoomResponseDto(
        id = this.id,
        name = this.name ?:"",
        users = this.users ?: emptyList(),
        createdAt = this.createdAt ?: 0L
    )
}

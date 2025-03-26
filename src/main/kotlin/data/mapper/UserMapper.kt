package com.ktor.data.mapper

import com.ktor.data.model.UserDto
import com.ktor.domain.model.User
import org.bson.types.ObjectId


fun UserDto.toDomain(): User {
    return User(
        id = id.toHexString(),  // Convierte ObjectId a String
        username = username,
        passwordHash = passwordHash
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = ObjectId(id),  // Convierte String a ObjectId
        username = username,
        passwordHash = passwordHash
    )
}

package com.ktor.domain.repository

import com.ktor.domain.model.User

interface UserRepository {
    suspend fun findUser(username: String): User?
    suspend fun createUser(user: User)
}
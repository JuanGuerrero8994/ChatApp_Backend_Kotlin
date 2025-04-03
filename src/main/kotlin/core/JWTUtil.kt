package com.ktor.core

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JWTUtil {
    private const val SECRET = "mySuperSecretKey"  // 🛑 Cambia esto en producción
    private const val ISSUER = "ktor-app"
    private const val EXPIRATION_TIME = 3_600_000 // 1 hora en milisegundos

    fun generateToken(userId: String): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withSubject(userId)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .sign(Algorithm.HMAC256(SECRET))
    }

    fun validateToken(token: String): String? {
        return try {
            JWT.require(Algorithm.HMAC256(SECRET))
                .withIssuer(ISSUER)
                .build()
                .verify(token)
                .subject
        } catch (e: Exception) {
            null
        }
    }
}

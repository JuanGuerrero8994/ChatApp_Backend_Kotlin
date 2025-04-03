package com.ktor.core

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import java.util.*

object JWTUtil {
    private const val SECRET = "chatSecretKey"  // 🛑 Cambia esto en producción
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
            val verifier = JWT.require(Algorithm.HMAC256(SECRET))
                .withIssuer(ISSUER)
                .build()

            val decodedJWT = verifier.verify(token) // ✅ Verifica el token
            decodedJWT.subject // ✅ Retorna el usuario si el token es válido
        } catch (e: TokenExpiredException) {
            println("❌ Token expirado")
            null
        } catch (e: JWTVerificationException) {
            println("❌ Token inválido")
            null
        }
    }
}
